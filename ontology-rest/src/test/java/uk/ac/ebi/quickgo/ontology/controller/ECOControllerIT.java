package uk.ac.ebi.quickgo.ontology.controller;

import uk.ac.ebi.quickgo.ontology.common.document.OntologyDocMocker;
import uk.ac.ebi.quickgo.ontology.common.document.OntologyDocument;

import java.util.Arrays;
import java.util.List;

/**
 * Tests the {@link ECOController} class. All tests for ECO
 * are covered by the tests in the parent class, {@link OBOControllerIT}.
 *
 * Created 24/11/15
 * @author Edd
 */
public class ECOControllerIT extends OBOControllerIT {
    private static final String RESOURCE_URL = "/QuickGO/services/eco";
    private static final String ECO_0000001 = "ECO:0000001";
    private static final String ECO_0000002 = "ECO:0000002";

    @Override
    protected List<OntologyDocument> createBasicDocs() {
        return Arrays.asList(
                OntologyDocMocker.createECODoc(ECO_0000001, "eco doc name 1"),
                OntologyDocMocker.createECODoc(ECO_0000002, "eco doc name 2"));
    }

    @Override
    protected String getResourceURL() {
        return RESOURCE_URL;
    }

    @Override
    protected String idMissingInRepository() {
        return "ECO:0000003";
    }

    @Override
    protected String invalidId() {
        return "ECO|0000001";
    }

}