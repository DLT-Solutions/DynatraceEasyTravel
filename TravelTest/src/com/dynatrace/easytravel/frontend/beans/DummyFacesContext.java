package com.dynatrace.easytravel.frontend.beans;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;


public class DummyFacesContext extends FacesContext {
    
    
    private List<FacesMessage> messages = new LinkedList<FacesMessage>();
    
    public void setFacesContextInstance() {
        setCurrentInstance(this);
    }

    @Override
    public void addMessage(String arg0, FacesMessage arg1) {
        messages.add(arg1);
    }

    @Override
    public Application getApplication() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<String> getClientIdsWithMessages() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExternalContext getExternalContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Severity getMaximumSeverity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<FacesMessage> getMessages() {
        return messages.iterator();
    }

    @Override
    public Iterator<FacesMessage> getMessages(String arg0) {
        return messages.iterator();
    }

    @Override
    public RenderKit getRenderKit() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getRenderResponse() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getResponseComplete() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ResponseStream getResponseStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseWriter getResponseWriter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UIViewRoot getViewRoot() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void renderResponse() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void responseComplete() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setResponseStream(ResponseStream arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setResponseWriter(ResponseWriter arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setViewRoot(UIViewRoot arg0) {
        // TODO Auto-generated method stub
        
    }

}
