package com.dynatrace.easytravel.launcher.fancy;

/**
 * @author christoph.neumueller
 */
public class FilterTaskParams {

	private String filterText;
    private Boolean radioBoxState = null;

	public FilterTaskParams(String filterText) {
		this.filterText = filterText;
	}

    public FilterTaskParams(Boolean radioBoxState) {
        this.radioBoxState = radioBoxState;
    }

	public String getFilterText() {
		return filterText;
	}

    public Boolean getRadioBoxState() {
        return radioBoxState;
    }
}
