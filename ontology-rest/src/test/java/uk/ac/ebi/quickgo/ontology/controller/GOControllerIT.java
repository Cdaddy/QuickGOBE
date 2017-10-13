package uk.ac.ebi.quickgo.ontology.controller;

import uk.ac.ebi.quickgo.ontology.common.OntologyDocument;
import uk.ac.ebi.quickgo.ontology.common.document.OntologyDocMocker;
import uk.ac.ebi.quickgo.ontology.model.OntologyRelationType;
import uk.ac.ebi.quickgo.ontology.model.OntologyRelationship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ebi.quickgo.common.converter.HelpfulConverter.toCSV;
import static uk.ac.ebi.quickgo.ontology.controller.OBOController.COMPLETE_SUB_RESOURCE;
import static uk.ac.ebi.quickgo.ontology.controller.OBOController.CONSTRAINTS_SUB_RESOURCE;

/**
 * Tests the {@link GOController} class. All tests for GO
 * are covered by the tests in the parent class, {@link OBOControllerIT}.
 *
 * Created 16/11/15
 * @author Edd
 */
public class GOControllerIT extends OBOControllerIT {

    private static final String RESOURCE_URL = "/ontology/go";
    private static final String GO_0000001 = "GO:0000001";
    private static final String GO_0000002 = "GO:0000002";
    private static final String GO_0000003 = "GO:0000003";
    private static final String GO_0000004 = "GO:0000004";
    private static final String GO_0000005 = "GO:0000005";
    private static final String GO_0000006 = "GO:0000006";


    private static final String GO_SLIM_CHILD1 = "GO:9000001";
    private static final String GO_SLIM_CHILD2 = "GO:9000002";
    private static final String GO_SLIM_CHILD3 = "GO:9000003";
    private static final String GO_SLIM_CHILD4 = "GO:9000004";
    private static final String GO_SLIM_CHILD5 = "GO:9000005";
    private static final String GO_SLIM_CHILD6 = "GO:9000006";

    private static final String GO_SLIM1 = "GO:1111111";
    private static final String GO_SLIM2 = "GO:2222222";
    private static final String GO_SLIM3 = "GO:3333333";
    private static final String GO_SLIM4 = "GO:4444444";
    private static final String GO_SLIM5 = "GO:5555555";
    private static final String GO_SLIM6 = "GO:6666666";
    private static final String GO_SLIM7 = "GO:7777777";

    private static final String SLIM_RESOURCE = "/slim";
    private static final String SLIM_IDS_PARAM = "ids";
    private static final String SLIM_RELATIONS_PARAM = "relations";
    private static final String STOP_NODE = "GO:0008150";

    @Before
    public void setUp() {
        List<OntologyRelationship> relationships = new ArrayList<>();

        relationships.add(createSlimRelationship(GO_SLIM_CHILD1, GO_SLIM1));
        relationships.add(createSlimRelationship(GO_SLIM_CHILD2, GO_SLIM2));
        relationships.add(createSlimRelationship(GO_SLIM_CHILD2, GO_SLIM3));
        relationships.add(createSlimRelationship(GO_SLIM_CHILD3, GO_SLIM4));
        relationships.add(createSlimRelationship(GO_SLIM_CHILD4, GO_SLIM4));
        relationships.add(createSlimRelationship(GO_SLIM_CHILD5, GO_SLIM5));
        relationships.add(createSlimRelationship(GO_SLIM_CHILD5, GO_SLIM6));
        relationships.add(createSlimRelationship(GO_SLIM_CHILD6, GO_SLIM6));
        relationships.add(createSlimRelationship(GO_SLIM_CHILD6, GO_SLIM7));

        relationships.add(createSlimRelationship(GO_SLIM1, STOP_NODE));
        relationships.add(createSlimRelationship(GO_SLIM2, STOP_NODE));
        relationships.add(createSlimRelationship(GO_SLIM3, STOP_NODE));
        relationships.add(createSlimRelationship(GO_SLIM4, STOP_NODE));
        relationships.add(createSlimRelationship(GO_SLIM4, STOP_NODE));
        relationships.add(createSlimRelationship(GO_SLIM5, STOP_NODE));
        relationships.add(createSlimRelationship(GO_SLIM6, STOP_NODE));
        relationships.add(createSlimRelationship(GO_SLIM6, STOP_NODE));
        relationships.add(createSlimRelationship(GO_SLIM7, STOP_NODE));

        ontologyGraph.addRelationships(relationships);
    }

    private OntologyRelationship createSlimRelationship(String term, String slimmedUpTerm) {
        return new OntologyRelationship(term, slimmedUpTerm, OntologyRelationType.IS_A);
    }

    @Test
    public void canRetrieveBlacklistByIds() throws Exception {
        ResultActions response = mockMvc.perform(get(
                buildTermsURLWithSubResource(toCSV(GO_0000001, GO_0000002), CONSTRAINTS_SUB_RESOURCE)));

        expectBasicFieldsInResults(response, Arrays.asList(GO_0000001, GO_0000002))
                .andExpect(jsonPath("$.results.*.blacklist", hasSize(2)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void canRetrieveGoDiscussions() throws Exception {
        ResultActions response = mockMvc.perform(get(buildTermsURLWithSubResource(GO_0000001, COMPLETE_SUB_RESOURCE)));

        expectBasicFieldsInResults(response, Collections.singletonList(GO_0000001))
                .andExpect(jsonPath("$.results.*.goDiscussions", hasSize(1)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void canRetrieveProteinComplexes() throws Exception {
        ResultActions response = mockMvc.perform(get(buildTermsURLWithSubResource(GO_0000001, COMPLETE_SUB_RESOURCE)));

        expectBasicFieldsInResults(response, Collections.singletonList(GO_0000001))
                .andExpect(jsonPath("$.results.*.proteinComplexes", hasSize(1)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void about() throws Exception {
        ResultActions response = mockMvc.perform(get(getResourceURL() + "/about"));
        final String expectedVersion = "http://purl.obolibrary.org/obo/go/releases/2017-01-12/go.owl";
        final String expectedTimestamp = "2017-01-13 02:19";
        response.andDo(print())
                .andExpect(jsonPath("$.go.version").value(expectedVersion))
                .andExpect(jsonPath("$.go.timestamp").value(expectedTimestamp));
    }

    @Test
    public void oneIdHasOneSlim() throws Exception {
        ResultActions response = mockMvc.perform(get(getSlimURL())
                .param(SLIM_IDS_PARAM, GO_SLIM1));

        response.andDo(print())
                .andExpect(jsonPath("$.numberOfHits").value(1))
                .andExpect(jsonPath("$.results.*.id", contains(GO_SLIM_CHILD1)));
    }

    @Test
    public void oneIdHasTwoSlims() throws Exception {
        ResultActions response = mockMvc.perform(get(getSlimURL())
                .param(SLIM_IDS_PARAM, GO_SLIM2));

        response.andDo(print())
                .andExpect(jsonPath("$.numberOfHits").value(1))
                .andExpect(jsonPath("$.results.*.id", contains(GO_SLIM_CHILD2)));

        mockMvc.perform(get(getSlimURL())
                .param(SLIM_IDS_PARAM, GO_SLIM3));

        response.andDo(print())
                .andExpect(jsonPath("$.numberOfHits").value(1))
                .andExpect(jsonPath("$.results.*.id", contains(GO_SLIM_CHILD2)));
    }

    @Test
    public void twoIdsHave1Slim() {
        // TODO: 13/10/17
    }

    @Test
    public void twoIdsHave2Slims() {
        // TODO: 13/10/17
    }

    @Test
    public void slimmingOverInvalidRelationshipCauses400() {
        // TODO: 13/10/17
    }

    @Test
    public void slimmingOverRelationshipNotInGraphReturnsEmptyResponse() {
        // TODO: 13/10/17
    }

    /*
     * GO produces two more attributes in its response (aspect and usage), when compared
     * to the standard OBO response.
     */
    @Override
    protected ResultActions expectCoreFields(ResultActions result, String id) throws Exception {
        return super
                .expectCoreFields(result, id)
                .andExpect(jsonPath("$.aspect").value("Biological Process"))
                .andExpect(jsonPath("$.usage").value("Unrestricted"));
    }

    @Override protected OntologyDocument createBasicDoc(String id, String name) {
        return OntologyDocMocker.createGODoc(id, name);
    }

    @Override protected List<OntologyDocument> createNDocs(int n) {
        return IntStream.range(1, n + 1)
                .mapToObj(i -> OntologyDocMocker.createGODoc(createId(i), "go doc name " + i)).collect
                        (Collectors.toList());
    }

    @Override
    protected String createId(int idNum) {
        return String.format("GO:%07d", idNum);
    }

    @Override
    protected List<OntologyDocument> createBasicDocs() {
        return Arrays.asList(
                OntologyDocMocker.createGODoc(GO_0000001, "doc name 1"),
                OntologyDocMocker.createGODoc(GO_0000002, "doc name 2"),
                OntologyDocMocker.createGODoc(GO_0000003, "doc name 3"),
                OntologyDocMocker.createGODoc(GO_0000004, "doc name 4"));
    }

    @Override
    protected String idMissingInRepository() {
        return "GO:0000399";
    }

    @Override
    protected String invalidId() {
        return "GO|0000001";
    }

    @Override
    protected String getResourceURL() {
        return RESOURCE_URL;
    }

    private String getSlimURL() {
        return getResourceURL() + "/" + SLIM_RESOURCE;
    }
}
