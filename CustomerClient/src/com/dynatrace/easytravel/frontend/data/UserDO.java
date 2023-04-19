package com.dynatrace.easytravel.frontend.data;

import java.io.Serializable;


public final class UserDO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6801980514794167634L;
    
    
    private String name;
    private String password;
    
    public UserDO() {
    }
    
    public UserDO(String name, String password) {
        this.name = name;
        this.password = password;
    }

    
    public String getName() {
        return name;
    }

    
    public String getPassword() {
        return password;
    }
    
    
    @Override
    public int hashCode() {
        return name.hashCode() + 59 * password.hashCode();
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserDO)) {
            return false;
        }
        UserDO other = (UserDO)obj;
        return (name == other.name || name != null && name.equals(other.name))
                && (password == other.password || password != null && password.equals(other.password));
    }
    

}
