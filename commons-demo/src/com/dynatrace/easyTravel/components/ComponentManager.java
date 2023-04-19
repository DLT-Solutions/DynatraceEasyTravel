package com.dynatrace.easytravel.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.google.common.collect.Lists;

public class ComponentManager implements ComponentManagerProxy {
	private static final Logger log = LoggerFactory.make();
	List<AbstractComponent> componentsList;
	
	public ComponentManager(){
		componentsList = new ArrayList<AbstractComponent>();
	}
	/**
	 * Set a component for traffic generation
	 * params array: 
	 * [0] - technology eg. Vagrant; 
	 * [1] - box type eg. frontend; 
	 * [2] - protocol eg. http; 
	 * [3] - context
	 *
	 * @param uri the uri of the component
	 * @param params depends on technology
	 *
	 * @author kasper.kulikowski
	 */
	public void setComponent(String uri, String[] params){
		String technology = params[0];
		technology = technology.toLowerCase();

		if(technology.equals("vagrant")){
			if(StringUtils.isNotBlank(params[2])){
				uri=params[2]+uri;
			}
			if(StringUtils.isNotBlank(params[3])){
				uri+=params[3];
			}
			
			if(!isElementWithGivenUriAlreadyInList(uri)){
				VagrantComponent component = new VagrantComponent(uri, params);
				componentsList.add(component);
				log.info("Adding Vagrant Box with uri: "+uri);
			} else {
				log.warn("Vagrant Box with uri: "+uri+" already exists in a list");
			}		
		}	
	};
	
	private boolean isElementWithGivenUriAlreadyInList(String uri){
		for(AbstractComponent comp : componentsList){
			if(comp.getURI().equals(uri)){
				return true;
			}
		}
		return false;
	}
	/**
	 * Get IP list of available components
	 * 
	 * @author kasper.kulikowski
	 */
	public String[] getComponentsIPList(String type){

		List<String> ipList = Lists.newArrayList();
		
		for(AbstractComponent component : componentsList){
			if(component.getType().equals(type)){
				ipList.add(component.getURI());
			}
		}
		
		return ipList.toArray(new String[0]);
	}

	@Override
	public void removeComponent(String uri) {
		Iterator<AbstractComponent> iter = componentsList.iterator();
		
		while(iter.hasNext()){
			if(iter.next().getURI().contains(uri)){
				iter.remove();
			}
		}
	}
}
