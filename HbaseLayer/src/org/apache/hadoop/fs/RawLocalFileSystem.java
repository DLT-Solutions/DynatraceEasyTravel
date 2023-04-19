/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.fs;

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.Shell;
import org.apache.hadoop.util.StringUtils;

import com.dynatrace.easytravel.hbase.util.OS;

/****************************************************************
 * Implement the FileSystem API for the raw local filesystem.
 *
 *****************************************************************/
public class RawLocalFileSystem extends FileSystem {
  static final URI NAME = URI.create("file:///");
  private Path workingDir;
  
  public RawLocalFileSystem() {
    workingDir = new Path(System.getProperty("user.dir")).makeQualified(this);
  }
  
  private Path makeAbsolute(Path f) {
    if (f.isAbsolute()) {
      return f;
    } else {
      return new Path(workingDir, f);
    }
  }
  
  /** Convert a path to a File. */
  public File pathToFile(Path path) {
    checkPath(path);
    if (!path.isAbsolute()) {
      path = new Path(getWorkingDirectory(), path);
    }
    return new File(path.toUri().getPath());
  }

  @Override
public URI getUri() { return NAME; }
  
  @Override
public void initialize(URI uri, Configuration conf) throws IOException {
    super.initialize(uri, conf);
    setConf(conf);
  }
  
  class TrackingFileInputStream extends FileInputStream {
    public TrackingFileInputStream(File f) throws IOException {
      super(f);
    }
    
    @Override
	public int read() throws IOException {
      int result = super.read();
      if (result != -1) {
        statistics.incrementBytesRead(1);
      }
      return result;
    }
    
    @Override
	public int read(byte[] data) throws IOException {
      int result = super.read(data);
      if (result != -1) {
        statistics.incrementBytesRead(result);
      }
      return result;
    }
    
    @Override
	public int read(byte[] data, int offset, int length) throws IOException {
      int result = super.read(data, offset, length);
      if (result != -1) {
        statistics.incrementBytesRead(result);
      }
      return result;
    }
  }

  /*******************************************************
   * For open()'s FSInputStream
   *******************************************************/
  class LocalFSFileInputStream extends FSInputStream {
    FileInputStream fis;
    private long position;

    public LocalFSFileInputStream(Path f) throws IOException {
      this.fis = new TrackingFileInputStream(pathToFile(f));
    }
    
    @Override
	public void seek(long pos) throws IOException {
      fis.getChannel().position(pos);
      this.position = pos;
    }
    
    @Override
	public long getPos() throws IOException {
      return this.position;
    }
    
    @Override
	public boolean seekToNewSource(long targetPos) throws IOException {
      return false;
    }
    
    /*
     * Just forward to the fis
     */
    @Override
	public int available() throws IOException { return fis.available(); }
    @Override
	public void close() throws IOException { fis.close(); }
    public boolean markSupport() { return false; }
    
    @Override
	public int read() throws IOException {
      try {
        int value = fis.read();
        if (value >= 0) {
          this.position++;
        }
        return value;
      } catch (IOException e) {                 // unexpected exception
        throw new FSError(e);                   // assume native fs error
      }
    }
    
    @Override
	public int read(byte[] b, int off, int len) throws IOException {
      try {
        int value = fis.read(b, off, len);
        if (value > 0) {
          this.position += value;
        }
        return value;
      } catch (IOException e) {                 // unexpected exception
        throw new FSError(e);                   // assume native fs error
      }
    }
    
    @Override
	public int read(long position, byte[] b, int off, int len)
      throws IOException {
      ByteBuffer bb = ByteBuffer.wrap(b, off, len);
      try {
        return fis.getChannel().read(bb, position);
      } catch (IOException e) {
        throw new FSError(e);
      }
    }
    
    @Override
	public long skip(long n) throws IOException {
      long value = fis.skip(n);
      if (value > 0) {
        this.position += value;
      }
      return value;
    }
  }
  
  @Override
public FSDataInputStream open(Path f, int bufferSize) throws IOException {
    if (!exists(f)) {
      throw new FileNotFoundException(f.toString());
    }
    return new FSDataInputStream(new BufferedFSInputStream(
        new LocalFSFileInputStream(f), bufferSize));
  }
  
  /*********************************************************
   * For create()'s FSOutputStream.
   *********************************************************/
  class LocalFSFileOutputStream extends OutputStream implements Syncable {
    FileOutputStream fos;
    
    private LocalFSFileOutputStream(Path f, boolean append) throws IOException {
      this.fos = new FileOutputStream(pathToFile(f), append);
    }
    
    /*
     * Just forward to the fos
     */
    @Override
	public void close() throws IOException { fos.close(); }
    @Override
	public void flush() throws IOException { fos.flush(); }
    @Override
	public void write(byte[] b, int off, int len) throws IOException {
      try {
        fos.write(b, off, len);
      } catch (IOException e) {                // unexpected exception
        throw new FSError(e);                  // assume native fs error
      }
    }
    
    @Override
	public void write(int b) throws IOException {
      try {
        fos.write(b);
      } catch (IOException e) {              // unexpected exception
        throw new FSError(e);                // assume native fs error
      }
    }

    /** {@inheritDoc} */
    @Override
	public void sync() throws IOException {
      fos.getFD().sync();      
    }
  }
  
  /** {@inheritDoc} */
  @Override
public FSDataOutputStream append(Path f, int bufferSize,
      Progressable progress) throws IOException {
    if (!exists(f)) {
      throw new FileNotFoundException("File " + f + " not found.");
    }
    if (getFileStatus(f).isDir()) {
      throw new IOException("Cannot append to a diretory (=" + f + " ).");
    }
    return new FSDataOutputStream(new BufferedOutputStream(
        new LocalFSFileOutputStream(f, true), bufferSize), statistics);
  }

  /** {@inheritDoc} */
  @Override
  public FSDataOutputStream create(Path f, boolean overwrite, int bufferSize,
                                   short replication, long blockSize, Progressable progress)
  throws IOException {
    return create(f, overwrite, true, bufferSize, replication, blockSize, progress);
  }

  private FSDataOutputStream create(Path f, boolean overwrite, 
      boolean createParent, int bufferSize,
      short replication, long blockSize, Progressable progress)
    throws IOException {
    if (exists(f) && !overwrite) {
      throw new IOException("File already exists:"+f);
    }
    Path parent = f.getParent();
    if (parent != null) {
      if (!createParent && !exists(parent)) {
        throw new FileNotFoundException("Parent directory doesn't exist: "
            + parent);
      } else if (!mkdirs(parent)) {
        throw new IOException("Mkdirs failed to create " + parent);
      }
    }
    return new FSDataOutputStream(new BufferedOutputStream(
        new LocalFSFileOutputStream(f, false), bufferSize), statistics);
  }

  /** {@inheritDoc} */
  @Override
  public FSDataOutputStream create(Path f, FsPermission permission,
      boolean overwrite, int bufferSize, short replication, long blockSize,
      Progressable progress) throws IOException {
    FSDataOutputStream out = create(f,
        overwrite, bufferSize, replication, blockSize, progress);
    setPermission(f, permission);
    return out;
  }

  /** {@inheritDoc} */
  @Override
  public FSDataOutputStream createNonRecursive(Path f, FsPermission permission,
      boolean overwrite,
      int bufferSize, short replication, long blockSize,
      Progressable progress) throws IOException {
    FSDataOutputStream out = create(f,
        overwrite, false, bufferSize, replication, blockSize, progress);
    setPermission(f, permission);
    return out;
  }

  @Override
public boolean rename(Path src, Path dst) throws IOException {
    if (pathToFile(src).renameTo(pathToFile(dst))) {
      return true;
    }
    LOG.debug("Falling through to a copy of " + src + " to " + dst);
    return FileUtil.copy(this, src, this, dst, true, getConf());
  }
  
  @Override
@Deprecated
  public boolean delete(Path p) throws IOException {
    return delete(p, true);
  }
  
  @Override
public boolean delete(Path p, boolean recursive) throws IOException {
    File f = pathToFile(p);
    if (f.isFile()) {
      return f.delete();
    } else if ((!recursive) && f.isDirectory() && 
        (FileUtil.listFiles(f).length != 0)) {
      throw new IOException("Directory " + f.toString() + " is not empty");
    }
    return FileUtil.fullyDelete(f);
  }
 
  @Override
public FileStatus[] listStatus(Path f) throws IOException {
    File localf = pathToFile(f);
    FileStatus[] results;

    if (!localf.exists()) {
      return null;
    }
    if (localf.isFile()) {
      return new FileStatus[] {
          new RawLocalFileStatus(localf, getDefaultBlockSize(), this) };
    }

    String[] names = localf.list();
    if (names == null) {
      return null;
    }
    results = new FileStatus[names.length];
    for (int i = 0; i < names.length; i++) {
      results[i] = getFileStatus(new Path(f, names[i]));
    }
    return results;
  }

  /**
   * Creates the specified directory hierarchy. Does not
   * treat existence as an error.
   */
  @Override
public boolean mkdirs(Path f) throws IOException {
    Path parent = f.getParent();
    File p2f = pathToFile(f);
    return (parent == null || mkdirs(parent)) &&
      (p2f.mkdir() || p2f.isDirectory());
  }

  /** {@inheritDoc} */
  @Override
  public boolean mkdirs(Path f, FsPermission permission) throws IOException {
    boolean b = mkdirs(f);
    setPermission(f, permission);
    return b;
  }
  
  @Override
  public Path getHomeDirectory() {
    return new Path(System.getProperty("user.home")).makeQualified(this);
  }

  /**
   * Set the working directory to the given directory.
   */
  @Override
  public void setWorkingDirectory(Path newDir) {
    workingDir = makeAbsolute(newDir);
    checkPath(workingDir);
    
  }
  
  @Override
  public Path getWorkingDirectory() {
    return workingDir;
  }
  
  // In the case of the local filesystem, we can just rename the file.
  @Override
public void moveFromLocalFile(Path src, Path dst) throws IOException {
    rename(src, dst);
  }
  
  // We can write output directly to the final location
  @Override
public Path startLocalOutput(Path fsOutputFile, Path tmpLocalFile)
    throws IOException {
    return fsOutputFile;
  }
  
  // It's in the right place - nothing to do.
  @Override
public void completeLocalOutput(Path fsWorkingFile, Path tmpLocalFile)
    throws IOException {
  }
  
  @Override
public void close() throws IOException {
    super.close();
  }
  
  @Override
public String toString() {
    return "LocalFS";
  }
  
  @Override
public FileStatus getFileStatus(Path f) throws IOException {
    File path = pathToFile(f);
    if (path.exists()) {
      return new RawLocalFileStatus(pathToFile(f), getDefaultBlockSize(), this);
    } else {
      throw new FileNotFoundException( "File " + f + " does not exist.");
    }
  }

  static class RawLocalFileStatus extends FileStatus {
    /* We can add extra fields here. It breaks at least CopyFiles.FilePair().
     * We recognize if the information is already loaded by check if
     * onwer.equals("").
     */
    private boolean isPermissionLoaded() {
      return !super.getOwner().equals(""); 
    }
    
    RawLocalFileStatus(File f, long defaultBlockSize, FileSystem fs) {
      super(f.length(), f.isDirectory(), 1, defaultBlockSize,
            f.lastModified(), new Path(f.getPath()).makeQualified(fs));
    }
    
    @Override
    public FsPermission getPermission() {
			if (!OS.isWinOs() && !isPermissionLoaded()) {
				loadPermissionInfo();
			}
      return super.getPermission();
    }

    @Override
    public String getOwner() {
      if (!isPermissionLoaded()) {
        loadPermissionInfo();
      }
      return super.getOwner();
    }

    @Override
    public String getGroup() {
      if (!isPermissionLoaded()) {
        loadPermissionInfo();
      }
      return super.getGroup();
    }

    /// loads permissions, owner, and group from `ls -ld`
    private void loadPermissionInfo() {
      IOException e = null;
      try {
        StringTokenizer t = new StringTokenizer(
            FileUtil.execCommand(new File(getPath().toUri()), 
                                 Shell.getGET_PERMISSION_COMMAND()));
        //expected format
        //-rw-------    1 username groupname ...
        String permission = t.nextToken();
        if (permission.length() > 10) { //files with ACLs might have a '+'
          permission = permission.substring(0, 10);
        }
        setPermission(FsPermission.valueOf(permission));
        t.nextToken();
        setOwner(t.nextToken());
        setGroup(t.nextToken());
      } catch (Shell.ExitCodeException ioe) {
        if (ioe.getExitCode() != 1) {
          e = ioe;
        } else {
          setPermission(null);
          setOwner(null);
          setGroup(null);
        }
      } catch (IOException ioe) {
        e = ioe;
      } finally {
        if (e != null) {
          throw new RuntimeException("Error while running command to get " +
                                     "file permissions : " + 
                                     StringUtils.stringifyException(e));
        }
      }
    }

    @Override
    public void write(DataOutput out) throws IOException {
      if (!isPermissionLoaded()) {
        loadPermissionInfo();
      }
      super.write(out);
    }
  }

  /**
   * Use the command chown to set owner.
   */
  @Override
  public void setOwner(Path p, String username, String groupname
      ) throws IOException {
    if (username == null && groupname == null) {
      throw new IOException("username == null && groupname == null");
    }

    if (username == null) {
      FileUtil.execCommand(pathToFile(p), Shell.SET_GROUP_COMMAND, groupname); 
    } else {
      //OWNER[:[GROUP]]
      String s = username + (groupname == null? "": ":" + groupname);
      FileUtil.execCommand(pathToFile(p), Shell.SET_OWNER_COMMAND, s);
    }
  }

  /**
   * Use the command chmod to set permission.
   */
  @Override
  public void setPermission(Path p, FsPermission permission
                            ) throws IOException {
		if (OS.isWinOs())
			return; // we cannot set permission in windows
		FileUtil.setPermission(pathToFile(p), permission);
  }
}
