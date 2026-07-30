package com.fcrm.fraud.x9parser.config;
 
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
 
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "x9.security")
public class SecurityUsersConfig {
    private List<UserEntry> users = new ArrayList<>();
    
    public List<UserEntry> getUsers(){
        return users;
    }

    public void setUsers(List<UserEntry> users){
        this.users=users;
    }

    // One user from the properties file: username, password, and role.
    public static class UserEntry{
        private String username;
        private String password;
        private String role;

        public String getUsername(){
            return username;
        }

        public void setUsername(String username){
            this.username = username;
        }

        public String getPassword() {
            return password;
        }
 
        public void setPassword(String password) {
            this.password = password;
        }
 
        public String getRole() {
            return role;
        }
 
        public void setRole(String role) {
            this.role = role;
        }
    }
}
