package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Highlight;

/**
 * Small bean that handles enabling/disabling facebook scripts. We usually
 * disable facebook scripts for load tests using HtmlUnit as HtmlUnit cannot
 * cope well with facebook JS.
 *
 * @author dominik.stadler
 */
@ManagedBean
@SessionScoped
public class FacebookBean implements Serializable {
	private static final long serialVersionUID = -2665374816301014715L;

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_SOCIALMEDIA);

	private boolean facebookEnabled = true;

	public void setFacebookEnabled(boolean facebookEnabled) {
		this.facebookEnabled = facebookEnabled;
	}

	public boolean isFacebookEnabled() {
		return facebookEnabled;
	}

	public boolean isFacebookGloballyEnabled() {
	    AtomicBoolean facebookEnabled = new AtomicBoolean(false);
        plugins.execute(PluginConstants.FRONTEND_SOCIALMEDIA_FOOTER, facebookEnabled);
        return facebookEnabled.get();
	}

    public String getFacebookState() {
        return isFacebookEnabled() ? "enabled" : "disabled";
    }

    public void setFacebookState(String facebookState) {
        if(facebookState.equals("enabled")) {
        	facebookEnabled = true;
        } else {
        	facebookEnabled = false;
        }
    }

    public SelectItem[] getFacebookStateItems() {
        SelectItem[] facebookItems =
            new SelectItem[] {
        		new SelectItem("enabled"),
        		new SelectItem("disabled")
        		};
        return facebookItems;
    }

    // effect that shows a value binding chance on there server
    protected Effect valueChangeEffect;

    public FacebookBean() {
        valueChangeEffect = new Highlight("#fda505");
        valueChangeEffect.setFired(true);
    }

    public void effectChangeListener(ValueChangeEvent event){
        valueChangeEffect.setFired(false);
    }

    /**
	 * Used to initialize the managed bean.
	 */
	protected void init() {

    }

    public Effect getValueChangeEffect() {
        return valueChangeEffect;
    }

    public void setValueChangeEffect(Effect valueChangeEffect) {
        this.valueChangeEffect = valueChangeEffect;
    }
}
