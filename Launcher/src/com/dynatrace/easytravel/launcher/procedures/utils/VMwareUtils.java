package com.dynatrace.easytravel.launcher.procedures.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import com.dynatrace.easytravel.net.UrlUtils;
import com.vmware.vim25.*;

/**
 * <pre>
 * VMotion
 *
 * Used to validate if VMotion is feasible between two hosts or not,
 * It is also used to perform migrate/relocate task depending on the data given
 *
 * <b>Parameters:</b>
 * url            [required] : url of the web service
 * username       [required] : username for the authentication
 * password       [required] : password for the authentication
 * vmname         [required] : name of the virtual machine
 * targethost     [required] : Name of the target host
 * sourcehost     [required] : Name of the host containing the virtual machine
 * targetpool     [required] : Name of the target resource pool
 * targetdatastore [required] : Name of the target datastore
 * priority       [required] : The priority of the migration task:-
 *                             defaultPriority, highPriority,lowPriority
 * state          [optional]
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.vm.VMotion --url [URLString] --username [User] --password [Password]
 * --vmname [VMName] --targethost [Target host] --sourcehost [Source host] --targetpool [Target resource pool]
 * --targetdatastore [Target datastore] --priority [Migration task priority] --state
 * </pre>
 */
public final class VMwareUtils {

	private static Logger log = Logger.getLogger(VMwareUtils.class.getName());

	private final static String[] meTree = { "ManagedEntity", "ComputeResource", "ClusterComputeResource", "Datacenter", "Folder", "HostSystem", "ResourcePool", "VirtualMachine" };
	private final static String[] crTree = { "ComputeResource",	"ClusterComputeResource" };
	private static String[] hcTree = { "HistoryCollector", "EventHistoryCollector", "TaskHistoryCollector" };

	/* Start Server Connection and common code */
	private final VimService vimService;
	private final VimService vimServiceRSC;
	private final VimPortType vimPort;
	private final VimPortType vimPortRSC;
	
	private ServiceContent serviceContent = null;
	private ManagedObjectReference propCollectorRef = null;

	private boolean isConnected = false;

	private static final String SVC_INST_NAME = "ServiceInstance";
	private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
	static {
		SVC_INST_REF.setType(SVC_INST_NAME);
		SVC_INST_REF.setValue(SVC_INST_NAME);
		
	}
	
	private volatile static VMwareUtils instance = null;
	
	public static VMwareUtils getInstance() {
		if (instance == null) {
			synchronized (VMwareUtils.class) {
				if (instance == null) {
					UrlUtils.trustAllHttpsCertificates();
					instance = new VMwareUtils();
				}
			}
		}
		return instance;
	}
		
	private VMwareUtils() {
		vimService = new VimService();
		vimPort = vimService.getVimPort();
		vimServiceRSC = new VimService();
		vimPortRSC = vimServiceRSC.getVimPort();
	}
	
	/**
	 * Establishes session with the virtual center server.
	 * @param url
	 * @param userName
	 * @param password
	 *
	 * @throws Exception
	 */
	private void connect(String url, String userName, String password) throws Exception {
		Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();

		ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
		ctxt.put("customCookieID", "vmware_soap_session");
		ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

		Map<String, Object> ctxtRSC = ((BindingProvider)vimPortRSC).getRequestContext();
		ctxtRSC.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

		serviceContent = vimPortRSC.retrieveServiceContent(SVC_INST_REF);
		propCollectorRef = serviceContent.getPropertyCollector();
		
		vimPort.login(serviceContent.getSessionManager(), userName, password, null);
		isConnected = true;		
	}
	
	/**
	 * Disconnects the user session.
	 *
	 * @throws Exception
	 */
	private void disconnect() throws Exception {
		if (isConnected ) {
			vimPort.logout(serviceContent.getSessionManager());			
		}
		isConnected = false;
	}

	/**
	 * Uses the new RetrievePropertiesEx method to emulate the now deprecated
	 * RetrieveProperties method.
	 *
	 * @param listpfs
	 * @return list of object content
	 * @throws Exception
	 */
	private List<ObjectContent> retrievePropertiesAllObjects(List<PropertyFilterSpec> listpfs) throws Exception {

		RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

		List<ObjectContent> listObjectContent = new ArrayList<ObjectContent>();
		try {
			RetrieveResult rslts = vimPort.retrievePropertiesEx(propCollectorRef, listpfs, propObjectRetrieveOpts);
			if (rslts != null && rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
				listObjectContent.addAll(rslts.getObjects());
			}
			String token = null;
			if (rslts != null && rslts.getToken() != null) {
				token = rslts.getToken();
			}
			while (token != null && !token.isEmpty()) {
				rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
				token = null;
				if (rslts != null) {
					token = rslts.getToken();
					if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
						listObjectContent.addAll(rslts.getObjects());
					}
				}
			}
		} catch (SOAPFaultException sfe) {
			printSoapFaultException(sfe);
		} catch (Exception e) {
			log.log(Level.SEVERE," Failed Getting Contents", e);
			throw e;
		}

		return listObjectContent;
	}

	/*
	 * @return An array of SelectionSpec covering VM, Host, Resource pool,
	 * Cluster Compute Resource and Datastore.
	 */
	private static List<SelectionSpec> buildFullTraversal() {
		// Terminal traversal specs

		// RP -> VM
		TraversalSpec rpToVm = new TraversalSpec();
		rpToVm.setName("rpToVm");
		rpToVm.setType("ResourcePool");
		rpToVm.setPath("vm");
		rpToVm.setSkip(Boolean.FALSE);

		// vApp -> VM
		TraversalSpec vAppToVM = new TraversalSpec();
		vAppToVM.setName("vAppToVM");
		vAppToVM.setType("VirtualApp");
		vAppToVM.setPath("vm");

		// HostSystem -> VM
		TraversalSpec hToVm = new TraversalSpec();
		hToVm.setType("HostSystem");
		hToVm.setPath("vm");
		hToVm.setName("hToVm");
		hToVm.getSelectSet().add(getSelectionSpec("visitFolders"));
		hToVm.setSkip(Boolean.FALSE);

		// DC -> DS
		TraversalSpec dcToDs = new TraversalSpec();
		dcToDs.setType("Datacenter");
		dcToDs.setPath("datastore");
		dcToDs.setName("dcToDs");
		dcToDs.setSkip(Boolean.FALSE);

		// Recurse through all ResourcePools
		TraversalSpec rpToRp = new TraversalSpec();
		rpToRp.setType("ResourcePool");
		rpToRp.setPath("resourcePool");
		rpToRp.setSkip(Boolean.FALSE);
		rpToRp.setName("rpToRp");
		rpToRp.getSelectSet().add(getSelectionSpec("rpToRp"));
		rpToRp.getSelectSet().add(getSelectionSpec("rpToVm"));

		TraversalSpec crToRp = new TraversalSpec();
		crToRp.setType("ComputeResource");
		crToRp.setPath("resourcePool");
		crToRp.setSkip(Boolean.FALSE);
		crToRp.setName("crToRp");
		crToRp.getSelectSet().add(getSelectionSpec("rpToRp"));
		crToRp.getSelectSet().add(getSelectionSpec("rpToVm"));

		TraversalSpec crToH = new TraversalSpec();
		crToH.setSkip(Boolean.FALSE);
		crToH.setType("ComputeResource");
		crToH.setPath("host");
		crToH.setName("crToH");

		TraversalSpec dcToHf = new TraversalSpec();
		dcToHf.setSkip(Boolean.FALSE);
		dcToHf.setType("Datacenter");
		dcToHf.setPath("hostFolder");
		dcToHf.setName("dcToHf");
		dcToHf.getSelectSet().add(getSelectionSpec("visitFolders"));

		TraversalSpec vAppToRp = new TraversalSpec();
		vAppToRp.setName("vAppToRp");
		vAppToRp.setType("VirtualApp");
		vAppToRp.setPath("resourcePool");
		vAppToRp.getSelectSet().add(getSelectionSpec("rpToRp"));

		TraversalSpec dcToVmf = new TraversalSpec();
		dcToVmf.setType("Datacenter");
		dcToVmf.setSkip(Boolean.FALSE);
		dcToVmf.setPath("vmFolder");
		dcToVmf.setName("dcToVmf");
		dcToVmf.getSelectSet().add(getSelectionSpec("visitFolders"));

		// For Folder -> Folder recursion
		TraversalSpec visitFolders = new TraversalSpec();
		visitFolders.setType("Folder");
		visitFolders.setPath("childEntity");
		visitFolders.setSkip(Boolean.FALSE);
		visitFolders.setName("visitFolders");
		List<SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
		sspecarrvf.add(getSelectionSpec("visitFolders"));
		sspecarrvf.add(getSelectionSpec("dcToVmf"));
		sspecarrvf.add(getSelectionSpec("dcToHf"));
		sspecarrvf.add(getSelectionSpec("dcToDs"));
		sspecarrvf.add(getSelectionSpec("crToRp"));
		sspecarrvf.add(getSelectionSpec("crToH"));
		sspecarrvf.add(getSelectionSpec("hToVm"));
		sspecarrvf.add(getSelectionSpec("rpToVm"));
		sspecarrvf.add(getSelectionSpec("rpToRp"));
		sspecarrvf.add(getSelectionSpec("vAppToRp"));
		sspecarrvf.add(getSelectionSpec("vAppToVM"));

		visitFolders.getSelectSet().addAll(sspecarrvf);

		List<SelectionSpec> resultspec = new ArrayList<SelectionSpec>();
		resultspec.add(visitFolders);
		resultspec.add(dcToVmf);
		resultspec.add(dcToHf);
		resultspec.add(dcToDs);
		resultspec.add(crToRp);
		resultspec.add(crToH);
		resultspec.add(hToVm);
		resultspec.add(rpToVm);
		resultspec.add(vAppToRp);
		resultspec.add(vAppToVM);
		resultspec.add(rpToRp);

		return resultspec;
	}

	private static SelectionSpec getSelectionSpec(String name) {
		SelectionSpec genericSpec = new SelectionSpec();
		genericSpec.setName(name);
		return genericSpec;
	}

	/**
	 *
	 * @return TraversalSpec specification to get to the HostSystem managed
	 *         object.
	 */
	private static TraversalSpec getHostSystemTraversalSpec() {
		// Create a traversal spec that starts from the 'root' objects
		// and traverses the inventory tree to get to the Host system.
		// Build the traversal specs bottoms up
		SelectionSpec ss = new SelectionSpec();
		ss.setName("VisitFolders");

		// Traversal to get to the host from ComputeResource
		TraversalSpec computeResourceToHostSystem = new TraversalSpec();
		computeResourceToHostSystem.setName("computeResourceToHostSystem");
		computeResourceToHostSystem.setType("ComputeResource");
		computeResourceToHostSystem.setPath("host");
		computeResourceToHostSystem.setSkip(false);
		computeResourceToHostSystem.getSelectSet().add(ss);

		// Traversal to get to the ComputeResource from hostFolder
		TraversalSpec hostFolderToComputeResource = new TraversalSpec();
		hostFolderToComputeResource.setName("hostFolderToComputeResource");
		hostFolderToComputeResource.setType("Folder");
		hostFolderToComputeResource.setPath("childEntity");
		hostFolderToComputeResource.setSkip(false);
		hostFolderToComputeResource.getSelectSet().add(ss);

		// Traversal to get to the hostFolder from DataCenter
		TraversalSpec dataCenterToHostFolder = new TraversalSpec();
		dataCenterToHostFolder.setName("DataCenterToHostFolder");
		dataCenterToHostFolder.setType("Datacenter");
		dataCenterToHostFolder.setPath("hostFolder");
		dataCenterToHostFolder.setSkip(false);
		dataCenterToHostFolder.getSelectSet().add(ss);

		//TraversalSpec to get to the DataCenter from rootFolder
		TraversalSpec traversalSpec = new TraversalSpec();
		traversalSpec.setName("VisitFolders");
		traversalSpec.setType("Folder");
		traversalSpec.setPath("childEntity");
		traversalSpec.setSkip(false);

		List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
		sSpecArr.add(ss);
		sSpecArr.add(dataCenterToHostFolder);
		sSpecArr.add(hostFolderToComputeResource);
		sSpecArr.add(computeResourceToHostSystem);
		traversalSpec.getSelectSet().addAll(sSpecArr);
		return traversalSpec;
	}

	/**
	 * Retrieves the MOREF of the host.
	 *
	 * @param hostName
	 *           :
	 * @return
	 * @throws Exception
	 */
	private ManagedObjectReference getHostByHostName(String hostName) throws Exception {
		ManagedObjectReference retVal = null;
		ManagedObjectReference rootFolder = serviceContent.getRootFolder();
		try {
			TraversalSpec tSpec = getHostSystemTraversalSpec();
			// Create Property Spec
			PropertySpec propertySpec = new PropertySpec();
			propertySpec.setAll(Boolean.FALSE);
			propertySpec.getPathSet().add("name");
			propertySpec.setType("HostSystem");

			// Now create Object Spec
			ObjectSpec objectSpec = new ObjectSpec();
			objectSpec.setObj(rootFolder);
			objectSpec.setSkip(Boolean.TRUE);
			objectSpec.getSelectSet().add(tSpec);

			// Create PropertyFilterSpec using the PropertySpec and ObjectPec
			// created above.
			PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
			propertyFilterSpec.getPropSet().add(propertySpec);
			propertyFilterSpec.getObjectSet().add(objectSpec);
			List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
			listpfs.add(propertyFilterSpec);
			List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

			for (ObjectContent oc : listobjcont) {
				ManagedObjectReference mr = oc.getObj();
				String hostnm = null;
				List<DynamicProperty> listDynamicProps = oc.getPropSet();
				DynamicProperty[] dps = listDynamicProps.toArray(new DynamicProperty[listDynamicProps.size()]);
				if (dps != null) {
					for (DynamicProperty dp : dps) {
						hostnm = (String) dp.getVal();
					}
				}
				if (hostnm != null && hostnm.equals(hostName)) {
					retVal = mr;
					break;
				}
			}
		} catch (SOAPFaultException sfe) {
			printSoapFaultException(sfe);
		} catch (Exception e) {
			log.severe(stackTraceToString(e));
		}
		if (retVal == null) {
			throw new Exception("Host " + hostName + " not found.");
		}
		return retVal;
	}


	/**
	 * Print stackTrace to log file
	 * @param e
	 * @return
	 */
	private String stackTraceToString(Throwable e) {
		StringBuilder sb = new StringBuilder();

		for (StackTraceElement stackTraceElement : e.getStackTrace()) {
			sb.append(stackTraceElement.toString());
			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * This code takes an array of [typename, property, property, ...] and
	 * converts it into a PropertySpec[]. handles case where multiple references
	 * to the same typename are specified.
	 *
	 * @param typeinfo
	 *           2D array of type and properties to retrieve
	 *
	 * @return Array of container filter specs
	 */
	private static List<PropertySpec> buildPropertySpecArray(String[][] typeinfo) {
		// Eliminate duplicates
		HashMap<String, Set<String>> tInfo = new HashMap<String, Set<String>>();
		for (int ti = 0; ti < typeinfo.length; ++ti) {
			Set<String> props = tInfo.get(typeinfo[ti][0]);
			if (props == null) {
				props = new HashSet<String>();
				tInfo.put(typeinfo[ti][0], props);
			}
			boolean typeSkipped = false;
			for (int pi = 0; pi < typeinfo[ti].length; ++pi) {
				String prop = typeinfo[ti][pi];
				if (typeSkipped) {
					props.add(prop);
				} else {
					typeSkipped = true;
				}
			}
		}

		// Create PropertySpecs
		ArrayList<PropertySpec> pSpecs = new ArrayList<PropertySpec>();

		for (Map.Entry<String, Set<String>> entry : tInfo.entrySet()) {
			String type = entry.getKey();
			Set<String> props = entry.getValue();

			PropertySpec pSpec = new PropertySpec();
			pSpec.setType(type);
			pSpec.setAll(props.isEmpty() ? Boolean.TRUE : Boolean.FALSE);

				for (Iterator<String> pi = props.iterator(); pi.hasNext();) {
					String prop = pi.next();
					pSpec.getPathSet().add(prop);
				}
			pSpecs.add(pSpec);
		}

		return pSpecs;
	}

	/**
	 * Retrieve content recursively with multiple properties. the typeinfo array
	 * contains typename + properties to retrieve.
	 *
	 * @param collector
	 *           a property collector if available or null for default
	 * @param root
	 *           a root folder if available, or null for default
	 * @param typeinfo
	 *           2D array of properties for each typename
	 * @param recurse
	 *           retrieve contents recursively from the root down
	 *
	 * @return retrieved object contents
	 */
	private List<ObjectContent> getContentsRecursively(ManagedObjectReference collector, ManagedObjectReference root,
													   String[][] typeinfo, boolean recurse) throws Exception {
		if (typeinfo == null || typeinfo.length == 0) {
			return null;
		}

		ManagedObjectReference usecoll = collector;
		if (usecoll == null) {
			usecoll = serviceContent.getPropertyCollector();
		}

		ManagedObjectReference useroot = root;
		if (useroot == null) {
			useroot = serviceContent.getRootFolder();
		}

		List<SelectionSpec> selectionSpecs = null;
		if (recurse) {
			selectionSpecs = buildFullTraversal();
		}

		List<PropertySpec> propspecary = buildPropertySpecArray(typeinfo);
		ObjectSpec objSpec = new ObjectSpec();
		objSpec.setObj(useroot);
		objSpec.setSkip(Boolean.FALSE);
		objSpec.getSelectSet().addAll(selectionSpecs);
		List<ObjectSpec> objSpecList = new ArrayList<ObjectSpec>();
		objSpecList.add(objSpec);
		PropertyFilterSpec spec = new PropertyFilterSpec();
		spec.getPropSet().addAll(propspecary);
		spec.getObjectSet().addAll(objSpecList);
		List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
		listpfs.add(spec);
		List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

		return listobjcont;
	}

	private static boolean typeIsA(String searchType, String foundType) {
		if (searchType.equals(foundType)) {
			return true;
		} else if (searchType.equals("ManagedEntity")) {
			for (int i = 0; i < meTree.length; ++i) {
				if (meTree[i].equals(foundType)) {
					return true;
				}
			}
		} else if (searchType.equals("ComputeResource")) {
			for (int i = 0; i < crTree.length; ++i) {
				if (crTree[i].equals(foundType)) {
					return true;
				}
			}
		} else if (searchType.equals("HistoryCollector")) {
			for (int i = 0; i < hcTree.length; ++i) {
				if (hcTree[i].equals(foundType)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the ManagedObjectReference for an item under the specified root folder
	 * that has the type and name specified.
	 *
	 * @param root
	 *           a root folder if available, or null for default
	 * @param type
	 *           type of the managed object
	 * @param name
	 *           name to match
	 *
	 * @return First ManagedObjectReference of the type / name pair found
	 */
	private ManagedObjectReference getDecendentMoRef(ManagedObjectReference root, String type, String name) throws Exception {
		if (name == null || name.length() == 0) {
			return null;
		}

		String[][] typeinfo = new String[][] { new String[] { type, "name" }, };

		List<ObjectContent> ocary =
				getContentsRecursively(null, root, typeinfo, true);

		if (ocary == null || ocary.size() == 0) {
			return null;
		}

		ObjectContent oc = null;
		ManagedObjectReference mor = null;
		List<DynamicProperty> propary = null;
		String propval = null;
		boolean found = false;
		for (int oci = 0; oci < ocary.size() && !found; oci++) {
			oc = ocary.get(oci);
			mor = oc.getObj();
			propary = oc.getPropSet();

			propval = null;
			if (type == null || typeIsA(type, mor.getType())) {
				if (propary.size() > 0) {
					propval = (String) propary.get(0).getVal();
				}
				found = propval != null && name.equals(propval);
			}
		}

		if (!found) {
			mor = null;
		}

		return mor;
	}


	public void migrateVM(String vmName, String pool, String tHost, String srcHost, String priority, String  state) throws Exception {
		VirtualMachinePowerState st = null;
		VirtualMachineMovePriority pri = null;
		if (state != null) {
			if (VirtualMachinePowerState.POWERED_OFF.toString().equalsIgnoreCase(state)) {
				st = VirtualMachinePowerState.POWERED_OFF;
			} else if (VirtualMachinePowerState.POWERED_ON.toString().equalsIgnoreCase(state)) {
				st = VirtualMachinePowerState.POWERED_ON;
			} else if (VirtualMachinePowerState.SUSPENDED.toString().equalsIgnoreCase(state)) {
				st = VirtualMachinePowerState.SUSPENDED;
			}
		}
		if (priority == null) {
			pri = VirtualMachineMovePriority.DEFAULT_PRIORITY;
		} else {
			if (VirtualMachineMovePriority.DEFAULT_PRIORITY.toString().equalsIgnoreCase(priority)) {
				pri = VirtualMachineMovePriority.DEFAULT_PRIORITY;
			} else if (VirtualMachineMovePriority.HIGH_PRIORITY.toString().equalsIgnoreCase(priority)) {
				pri = VirtualMachineMovePriority.HIGH_PRIORITY;
			} else if (VirtualMachineMovePriority.LOW_PRIORITY.toString().equalsIgnoreCase(priority)) {
				pri = VirtualMachineMovePriority.LOW_PRIORITY;
			}
		}
		try {
			ManagedObjectReference srcMOR = getHostByHostName(srcHost);
			if (srcMOR == null) {
				throw new IllegalArgumentException("Source Host" + srcHost + " Not Found.");
			}
			ManagedObjectReference vmMOR = getDecendentMoRef(srcMOR, "VirtualMachine", vmName);
			if (vmMOR == null) {
				throw new IllegalArgumentException("Virtual Machine " + vmName + " Not Found on source host.");
			}
			ManagedObjectReference poolMOR = getDecendentMoRef(null, "ResourcePool", pool);
			if (poolMOR == null) {
				throw new IllegalArgumentException("Target Resource Pool " + pool + " Not Found.");
			}
			ManagedObjectReference hMOR = getHostByHostName(tHost);
			if (hMOR == null) {
				throw new IllegalArgumentException(" Target Host " + tHost + " Not Found.");
			}
			log.info("Migrating the Virtual Machine " + vmName);
			ManagedObjectReference taskMOR = vimPort.migrateVMTask(vmMOR, poolMOR, hMOR, pri, st);
			if (getTaskResultAfterDone(taskMOR)) {
				log.info("Migration of Virtual Machine " + vmName + " done successfully to " + tHost);
			} else {
				String msg = "Error::  Migration failed";
				log.severe(msg);
				throw new Exception(msg);
			}
		} catch (SOAPFaultException sfe) {
			printSoapFaultException(sfe);
			throw sfe;
		} catch (Exception e) {
			log.log(Level.SEVERE,"Error::" , e);
			throw e;
		}
	}


	private static void printSoapFaultException(SOAPFaultException sfe) {
		log.severe("SOAP Fault -");
		if (sfe.getFault().hasDetail()) {
			log.severe(sfe.getFault().getDetail().getFirstChild().getLocalName());
		}
		if (sfe.getFault().getFaultString() != null) {
			log.severe("\n Message: " + sfe.getFault().getFaultString());
		}
	}

	/**
	 * This method returns a boolean value specifying whether the Task is
	 * succeeded or failed.
	 *
	 * @param task
	 *           ManagedObjectReference representing the Task.
	 *
	 * @return boolean value representing the Task result.
	 * @throws InvalidCollectorVersionFaultMsg
	 * @throws RuntimeFaultFaultMsg
	 * @throws InvalidPropertyFaultMsg
	 */
	private boolean getTaskResultAfterDone(ManagedObjectReference task) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg {

		boolean retVal = false;

		// info has a property - state for state of the task
		Object[] result =
				waitForValues(task, new String[] { "info.state", "info.error" },
						new String[] { "state" }, new Object[][] { new Object[] {
								TaskInfoState.SUCCESS, TaskInfoState.ERROR } });

		if (result[0].equals(TaskInfoState.SUCCESS)) {
			retVal = true;
		}
		if (result[1] instanceof LocalizedMethodFault) {
			throw new RuntimeException(((LocalizedMethodFault) result[1]).getLocalizedMessage());
		}
		return retVal;
	}

	/**
	 * Handle Updates for a single object. waits till expected values of
	 * properties to check are reached Destroys the ObjectFilter when done.
	 *
	 * @param objmor       MOR of the Object to wait for
	 * @param filterProps  Properties list to filter
	 * @param endWaitProps Properties list to check for expected values these be properties
	 *                     of a property in the filter properties list
	 * @param expectedVals values for properties to end the wait
	 * @return true indicating expected values were met, and false otherwise
	 * @throws RuntimeFaultFaultMsg
	 * @throws InvalidPropertyFaultMsg
	 * @throws InvalidCollectorVersionFaultMsg
	 *
	 */
	private Object[] waitForValues(ManagedObjectReference objmor, String[] filterProps, String[] endWaitProps, Object[][] expectedVals)
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg {

		// version string is initially null
		String version = "";
		Object[] endVals = new Object[endWaitProps.length];
		Object[] filterVals = new Object[filterProps.length];

		PropertyFilterSpec spec = new PropertyFilterSpec();
		ObjectSpec oSpec = new ObjectSpec();
		oSpec.setObj(objmor);
		oSpec.setSkip(Boolean.FALSE);
		spec.getObjectSet().add(oSpec);

		PropertySpec pSpec = new PropertySpec();
		pSpec.getPathSet().addAll(Arrays.asList(filterProps));
		pSpec.setType(objmor.getType());
		spec.getPropSet().add(pSpec);

		ManagedObjectReference filterSpecRef = vimPort.createFilter(serviceContent.getPropertyCollector(), spec, true);

		boolean reached = false;

		UpdateSet updateset = null;
		List<PropertyFilterUpdate> filtupary = null;
		List<ObjectUpdate> objupary = null;
		List<PropertyChange> propchgary = null;
		while (!reached) {
			updateset = vimPort.waitForUpdates(serviceContent.getPropertyCollector(), version);
			if (updateset == null || updateset.getFilterSet() == null) {
				continue;
			}
			version = updateset.getVersion();

			// Make this code more general purpose when PropCol changes later.
			filtupary = updateset.getFilterSet();

			for (PropertyFilterUpdate filtup : filtupary) {
				objupary = filtup.getObjectSet();
				for (ObjectUpdate objup : objupary) {
					if (objup.getKind() == ObjectUpdateKind.MODIFY
							|| objup.getKind() == ObjectUpdateKind.ENTER
							|| objup.getKind() == ObjectUpdateKind.LEAVE) {
						propchgary = objup.getChangeSet();
						for (PropertyChange propchg : propchgary) {
							updateValues(endWaitProps, endVals, propchg);
							updateValues(filterProps, filterVals, propchg);
						}
					}
				}
			}

			Object expctdval = null;
			// Check if the expected values have been reached and exit the loop
			// if done.
			// Also exit the WaitForUpdates loop if this is the case.
			for (int chgi = 0; chgi < endVals.length && !reached; chgi++) {
				for (int vali = 0; vali < expectedVals[chgi].length && !reached; vali++) {
					expctdval = expectedVals[chgi][vali];

					reached = expctdval.equals(endVals[chgi]) || reached;
				}
			}
		}

		// Destroy the filter when we are done.
		vimPort.destroyPropertyFilter(filterSpecRef);
		return filterVals;
	}

	private static void updateValues(String[] props, Object[] vals, PropertyChange propchg) {
		for (int findi = 0; findi < props.length; findi++) {
			if (propchg.getName().lastIndexOf(props[findi]) >= 0) {
				if (propchg.getOp() == PropertyChangeOp.REMOVE) {
					vals[findi] = "";
				} else {
					vals[findi] = propchg.getVal();
				}
			}
		}
	}

	/**
	 * Method  calls vmotion and waits for result
	 *
	 * @param vCenterHost
	 * @param userName
	 * @param password
	 * @param vmName
	 * @param resourcePool
	 * @param targetHost
	 * @param srcHost
	 * @throws Exception
	 */
	public static boolean callvMotionTask(String vCenterHost, String userName, String password,
										  String vmName, String resourcePool, String srcHost, String targetHost) throws Exception {

		checkArgument(vCenterHost, "vCenterHost");
		checkArgument(userName, "vCenterUser");
		//		checkArgument(password, "vCenterPassword");
		checkArgument(vmName, "vmName");
		checkArgument(resourcePool, "resPool");
		checkArgument(srcHost, "fromHost");
		checkArgument(targetHost, "toHost");


		final String url = "https://" + vCenterHost + "/sdk";
		getInstance();
		try {
			instance.connect(url, userName, password);
			instance.migrateVM(vmName, resourcePool, targetHost, srcHost, VirtualMachineMovePriority.HIGH_PRIORITY.toString(), null);
		} finally {
			instance.disconnect();
		}
		return true;
	}


	private static void checkArgument(String argument, String name) throws Exception {
		if (argument == null || argument.trim().length()==0){
			throw new Exception ( "Parameter: " +name +" is empty or null" );
		}
	}
}
