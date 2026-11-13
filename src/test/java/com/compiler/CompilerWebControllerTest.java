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
    public void testAstViewTree() throws Exception {
        mockMvc.perform(get("/ast").param("view", "tree"))
                .andExpect(status().isOk())
                .andExpect(view().name("ast"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attribute("viewMode", "tree"));
    }

    @Test
    public void testAstViewRaw() throws Exception {
        mockMvc.perform(get("/ast").param("view", "raw"))
                .andExpect(status().isOk())
                .andExpect(view().name("ast"))
                .andExpect(model().attribute("viewMode", "raw"));
    }

    @Test
    public void testSymbolsViewFiltered() throws Exception {
        mockMvc.perform(get("/symbols")
                        .param("type", "CONTROLLER")
                        .param("scope", "ALL")
                        .param("search", "Product"))
                .andExpect(status().isOk())
                .andExpect(view().name("symbols"))
                .andExpect(model().attributeExists("symbols"))
                .andExpect(model().attribute("selectedType", "CONTROLLER"));
    }

    @Test
    public void testSymbolsExportCsv() throws Exception {
        mockMvc.perform(get("/symbols/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"symbols.csv\""));
    }

    @Test
    public void testRelationshipsViewFiltered() throws Exception {
        mockMvc.perform(get("/relationships").param("filter", "VALID"))
                .andExpect(status().isOk())
                .andExpect(view().name("relationships"))
                .andExpect(model().attributeExists("relationships"))
                .andExpect(model().attribute("selectedFilter", "VALID"));
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
