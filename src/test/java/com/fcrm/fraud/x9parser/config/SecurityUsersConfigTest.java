package com.fcrm.fraud.x9parser.config;
 
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
 
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SecurityUsersConfigTest {
    @Autowired
    private SecurityUsersConfig config;
 
    @Test
    void loadsBothUsersFromThePropertiesFile() {
        assertEquals(2, config.getUsers().size());
    }
 
    @Test
    void loadsTheAdminUser() {
        SecurityUsersConfig.UserEntry admin = config.getUsers().get(0);
 
        assertEquals("admin", admin.getUsername());
        assertEquals("admin123", admin.getPassword());
        assertEquals("ADMIN", admin.getRole());
    }
 
    @Test
    void loadsTheNormalUser() {
        SecurityUsersConfig.UserEntry user = config.getUsers().get(1);
 
        assertEquals("user", user.getUsername());
        assertEquals("user123", user.getPassword());
        assertEquals("USER", user.getRole());

    }
}
