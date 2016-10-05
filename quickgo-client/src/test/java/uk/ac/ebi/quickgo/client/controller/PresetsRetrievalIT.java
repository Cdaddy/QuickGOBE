package uk.ac.ebi.quickgo.client.controller;

import uk.ac.ebi.quickgo.client.QuickGOREST;
import uk.ac.ebi.quickgo.client.model.presets.impl.CompositePresetImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests whether the {@link CompositePresetImpl} instance is populated correctly at application startup.
 *
 * Created 05/09/16
 * @author Edd
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {QuickGOREST.class})
@WebAppConfiguration
public class PresetsRetrievalIT {
    private static final String RESOURCE_URL = "/QuickGO/internal/presets";

    @Autowired
    protected WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void canRetrieveAssignedByPresets() throws Exception {
        mockMvc.perform(get(RESOURCE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedBy.*", hasSize(1)));
    }

    @Test
    public void canRetrieveReferencePresets() throws Exception {
        mockMvc.perform(get(RESOURCE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.references.*", hasSize(greaterThan(0))));
    }

    @Test
    public void canRetrieveEvidencePresets() throws Exception {
        mockMvc.perform(get(RESOURCE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidences.*", hasSize(greaterThan(0))));
    }

    @Test
    public void canRetrieveWithFromPresets() throws Exception {
        mockMvc.perform(get(RESOURCE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.withFrom.*", hasSize(greaterThan(0))));
    }

    @Test
    public void canRetrieveGeneProductPresets() throws Exception {
        mockMvc.perform(get(RESOURCE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.geneProducts.*", hasSize(greaterThan(0))));
    }

    @Test
    public void canRetrieveGOSlimSetsPresets() throws Exception {
        mockMvc.perform(get(RESOURCE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goSlimSets.*", hasSize(greaterThan(0))));
    }

    @Test
    public void canRetrieveTaxonPresets() throws Exception {
        mockMvc.perform(get(RESOURCE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taxons").exists())
                .andExpect(jsonPath("$.taxons.presets").exists());
    }

    @Test
    public void canRetrieveQualifierPresets() throws Exception {
        mockMvc.perform(get(RESOURCE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qualifiers").exists())
                .andExpect(jsonPath("$.qualifiers.presets").exists());
    }

    @Test
    public void canRetrieveAspectPresets() throws Exception {
        mockMvc.perform(get(RESOURCE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aspects").exists())
                .andExpect(jsonPath("$.aspects.presets.*", hasSize(3)));
    }

    @Test
    public void canRetrieveGeneProductTypesPresets() throws Exception {
        mockMvc.perform(get(RESOURCE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.geneProductTypes").exists())
                .andExpect(jsonPath("$.geneProductTypes.presets.*", hasSize(3)));
    }
}
