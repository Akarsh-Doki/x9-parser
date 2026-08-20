package com.fcrm.fraud.x9parser.controller;

import com.fcrm.fraud.x9parser.config.X9Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Runs the whole flow through the web layer as an Admin user: the form, a good
// file, and bad input.
@SpringBootTest
@WithMockUser(authorities = "FCRMADMIN")
class ParseFlowTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private X9Config config;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(@TempDir Path outputDir) {
        config.setOutputDir(outputDir.toString());
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                 .apply(springSecurity())
                                 .build();
    }

    @Test
    void theFormLoads() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("X9 File Parser")));
    }

    @Test
    void parsingTheSampleShowsASuccessSummary() throws Exception {
        String samplePath = Path.of(getClass().getResource("/sample.x9").toURI()).toString();

        mockMvc.perform(post("/parse").param("filePath", samplePath).with(csrf()))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("parsed successfully")))
               .andExpect(content().string(containsString("10")));
    }

    @Test
    void aBlankPathShowsAMessage() throws Exception {
        mockMvc.perform(post("/parse").param("filePath", "").with(csrf()))
               .andExpect(redirectedUrl("/"))
               .andExpect(flash().attribute("error", "Please enter a file path."));
    }

    @Test
    void aBadPathShowsAMessage() throws Exception {
        mockMvc.perform(post("/parse").param("filePath", "/no/such/file.x9").with(csrf()))
               .andExpect(redirectedUrl("/"))
               .andExpect(flash().attributeExists("error"));
    }
}