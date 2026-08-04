package com.fcrm.fraud.x9parser.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityFlowTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                 .apply(springSecurity())
                                 .build();
    }

    @Test
    void theLoginPageIsOpenToEveryone() throws Exception {
        mockMvc.perform(get("/login"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("Sign in")));
    }

    @Test
    void aVisitorWhoIsNotSignedInIsSentToLogin() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void theAdminUserCanSignInAndLandsOnTheParsePage() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password("admin123"))
               .andExpect(authenticated().withRoles("ADMIN"))
               .andExpect(redirectedUrl("/"));
    }

    @Test
    void theNormalUserSignsInAndIsSentToTheNoPermissionPage() throws Exception {
        mockMvc.perform(formLogin("/login").user("user").password("user123"))
               .andExpect(authenticated().withRoles("USER"))
               .andExpect(redirectedUrl("/no-permission"));
    }

    @Test
    void aWrongPasswordIsRejected() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password("wrong"))
               .andExpect(unauthenticated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void aNormalUserWhoOpensTheParsePageIsSentToTheNoPermissionPage() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/no-permission"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void aNormalUserCanSeeTheNoPermissionPage() throws Exception {
        mockMvc.perform(get("/no-permission"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("You do not have permission to parse files.")));
    }
}