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
    void aVisitorWhoIsNotSignedInIsRedirected() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(authorities = "FCRMADMIN")
    void anAdminCanReachTheParsePage() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("X9 File Parser")));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void aNormalUserWhoOpensTheParsePageIsSentToTheNoPermissionPage() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/no-permission"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void aNormalUserCanSeeTheNoPermissionPage() throws Exception {
        mockMvc.perform(get("/no-permission"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("You do not have permission to parse files.")));
    }
}