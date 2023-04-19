package com.dynatrace.easytravel.jpa.business;

import javax.persistence.*;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.jpa.QueryNames;

@Entity
@NamedQueries({
	
		@NamedQuery(name = QueryNames.LOCATION_ALL, query = "select l from Location l order by lower(name)"),
		@NamedQuery(
				name = QueryNames.LOCATION_FIND,
				query = "select l from Location l where lower(name) like '%' || :name || '%'"),
		@NamedQuery(
				name = QueryNames.LOCATION_FIND_WITH_JOURNEYS,
				query = "select l from Location l where lower(name) like '%' || :name || '%' " +
						"and exists (select j from Journey j where j.destination = l)"),
		@NamedQuery(
				name = QueryNames.LOCATION_FIND_WITH_JOURNEYS_AND_NORMALIZE,
				// The purpose of normalize_location() is only to increase execution time of the query.  Ideally, it should be executed only once per query,
				// however, executing for every time the first two components of the query (the join) are true is acceptable, as this gives a small
				// number of executions (usually <20).
				// From tests we know that:
				// Oracle - once per query
				// Derby - for every row of the result
				// mySQL - once per query.
				// Note that incorporating the delay function in the first component of the query, would result in the function being called for
				// every row of the dataset. Attempts to resolve this by passing the row number to the delay function would fail because in Derby,
				// the ROW_NUMBER () function cannot currently be used in a WHERE clause. Also failed trying to wrap the select in another
				// "select... where normalize_location(...) is not null".
				
				query = "select l from Location l where lower(name) like '%' || :name || '%' " +
						"and exists (select j from Journey j where j.destination = l) and normalize_location(:name, :factor) is not null"
						)
		
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Location extends Base {

	public static final String LOCATION_NAME = "name";

	@Id
	@Column(length = 100)
	private String name;

	// only used for Memory Leak plugins, not persisted and not transferred via WebServices
	@Transient
	transient private byte[] picture;

	public Location() {
		super();
	}

	public Location(String name) {
		super();
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
		return "Location [name=" + name + "]";
	}

	// does not have effect... @WebMethod(exclude=true)
	public void storePicture(byte[] picture) {
		this.picture = ArrayUtils.clone(picture);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
