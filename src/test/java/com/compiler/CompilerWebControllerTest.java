package com.compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class CompilerWebControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testRootRedirectsToDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    public void testDashboardView() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attribute("activePage", "dashboard"));
    }

    @Test
    public void testIndexView() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attribute("activePage", "index"));
    }

    @Test
    public void testAstView() throws Exception {
        mockMvc.perform(get("/ast"))
                .andExpect(status().isOk())
                .andExpect(view().name("ast"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attribute("activePage", "ast"));
    }

    @Test
    public void testSymbolsView() throws Exception {
        mockMvc.perform(get("/symbols"))
                .andExpect(status().isOk())
                .andExpect(view().name("symbols"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attribute("activePage", "symbols"));
    }

    @Test
    public void testRelationshipsView() throws Exception {
        mockMvc.perform(get("/relationships"))
                .andExpect(status().isOk())
                .andExpect(view().name("relationships"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attribute("activePage", "relationships"));
    }

    @Test
    public void testAnalysisView() throws Exception {
        mockMvc.perform(get("/analysis"))
                .andExpect(status().isOk())
                .andExpect(view().name("analysis"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attribute("activePage", "analysis"));
    }

    @Test
    public void testCompileEndpoint() throws Exception {
        mockMvc.perform(post("/compile").param("path", "test-project"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("alertWarning"));
    }
}
