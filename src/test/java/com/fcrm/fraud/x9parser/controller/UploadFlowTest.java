package com.fcrm.fraud.x9parser.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the whole flow through the web layer: upload the sample file, view the
 * results, download the CSV and an image.
 *
 * MockMvc is built by hand from the application context (rather than with
 * @AutoConfigureMockMvc) so the test does not depend on where that annotation
 * lives, which changed between Spring Boot versions.
 */
@SpringBootTest
class UploadFlowTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void uploadingTheSampleFileLeadsToTheResultsPage() throws Exception {
        MockHttpSession session = uploadSample();

        mockMvc.perform(get("/result").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("10")))
            .andExpect(content().string(containsString("check1_F.tif")));
    }

    @Test
    void theCsvDownloadReturnsTheCheckData() throws Exception {
        MockHttpSession session = uploadSample();
        mockMvc.perform(get("/download/csv").session(session))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("checks.csv")))
            .andExpect(content().string(containsString("05777093")));
    }

    @Test
    void anImageDownloadReturnsTheTifByName() throws Exception {
        MockHttpSession session = uploadSample();

        mockMvc.perform(get("/download/image/check1_F.tif").session(session))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("check1_F.tif")));
    }
    @Test
    void anUnknownImageNameIsANotFound() throws Exception {
        MockHttpSession session = uploadSample();

        mockMvc.perform(get("/download/image/nope.tif").session(session))
            .andExpect(status().isNotFound());
    }

    @Test
    void anEmptyUploadGoesBackToTheFormWithAMessage() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/upload").file(empty))
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("error", "Please choose a file to upload."));
    }

    @Test
    void aNonX9UploadGoesBackToTheFormWithAMessage() throws Exception {
        MockMultipartFile notX9 = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "just some text".getBytes());

        mockMvc.perform(multipart("/upload").file(notX9))
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attributeExists("error"));
    }

    @Test
    void downloadsWithoutAnUploadRedirectToTheForm() throws Exception {
        mockMvc.perform(get("/download/csv"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/"));
    }

    // upload the sample file and hand back the session that now holds the result
    private MockHttpSession uploadSample() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.x9", "application/octet-stream", sampleFileBytes());

        MvcResult result = mockMvc.perform(multipart("/upload").file(file))
                                .andExpect(redirectedUrl("/result"))
                                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private byte[] sampleFileBytes() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/sample.x9")) {
            if (in == null) {
                throw new IllegalStateException("sample.x9 is missing from src/test/resources");
            }
            return in.readAllBytes();
        }
    }
}