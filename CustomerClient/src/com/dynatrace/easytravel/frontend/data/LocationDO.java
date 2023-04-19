package com.dynatrace.easytravel.frontend.data;

import java.io.Serializable;

/**
 * Location Display Object
 *
 * @author philipp.grasboeck
 *
 */
public class LocationDO implements Serializable {

	private static final long serialVersionUID = -226478881182089667L;

	private String name;

	public LocationDO() {
	}

	public LocationDO(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "LocationDO [name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocationDO other = (LocationDO) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
