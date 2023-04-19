This CouchDB distribution has been prepared to run stand-alone without prior
installation, based on the Apache CouchDB binary distribution for 
Windows, version 3.2.2.
The following steps have been used to prepare it:

1. The distribution is a copy of the CouchDB installation folders of the
Windows Apache CouchDB binary distribution for Windows, version 3.2.2.

2. The following has been removed as it was deemed unnecessary:
	erts-11.2.2.12/doc
	share/www
	share/server

3. The following has been modified:
	etc/couchdb/local.ini - the path to the database area and the path to CouchDB
		logfile have been replaced by placeholders, to be later replaced by actual
		paths, when CouchDB is being started from easyTravel.

4. The following has been added:
	in Windows, to provide a way of controlling, starting and stopping:
		couchDB_ET.beam (compiled from couchDB_ET.erl)



