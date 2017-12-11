package uk.ac.ebi.quickgo.annotation.controller;

import uk.ac.ebi.quickgo.annotation.IdGeneratorUtil;
import uk.ac.ebi.quickgo.annotation.common.AnnotationDocument;
import uk.ac.ebi.quickgo.annotation.service.comm.rest.ontology.model.OntologyRelatives;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ebi.quickgo.annotation.AnnotationParameters.GO_ID_PARAM;
import static uk.ac.ebi.quickgo.annotation.AnnotationParameters.GO_USAGE_PARAM;
import static uk.ac.ebi.quickgo.annotation.AnnotationParameters.GO_USAGE_RELATIONS_PARAM;
import static uk.ac.ebi.quickgo.annotation.common.document.AnnotationDocMocker.createAnnotationDoc;
import static uk.ac.ebi.quickgo.annotation.controller.ResponseVerifier.*;

/**
 * Created 02/11/16
 * @author Edd
 */
public class FilterAnnotationByGORESTIT extends AbstractFilterAnnotationByOntologyRESTIT {
    private static final String GO_DESCENDANTS_RESOURCE_FORMAT = "/ontology/go/terms/%s/descendants?relations=%s";
    private static final String GO_SLIM_RESOURCE_FORMAT = "/ontology/go/slim?slimsToIds=%s&relations=%s";

    public FilterAnnotationByGORESTIT() {
        resourceFormat = GO_DESCENDANTS_RESOURCE_FORMAT;
        usageParam = GO_USAGE_PARAM.getName();
        idParam = GO_ID_PARAM.getName();
        usageRelations = GO_USAGE_RELATIONS_PARAM.getName();
    }

    // slimming tests (which is GO specific)
    @Test
    public void slimFilterThatSlimsToItself() throws Exception {
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(1), ontologyId(1)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(2), ontologyId(2)));

        expectRestCallHasSlims(
                singletonList(ontologyId(1)),
                emptyList(),
                singletonList((singletonList(ontologyId(1)))));

        ResultActions response = mockMvc.perform(
                get(SEARCH_RESOURCE)
                        .param(usageParam, SLIM_USAGE)
                        .param(idParam, ontologyId(1)));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(contentTypeToBeJson())
                .andExpect(pageInfoExists())
                .andExpect(totalNumOfResults(1))
                .andExpect(fieldsInAllResultsExist(1))
                .andExpect(valuesOccurInField(idParam, ontologyId(1)))
                .andExpect(valuesOccurInField(SLIMMED_ID_FIELD, singletonList(singletonList(ontologyId(1)))));
    }

    @Test
    public void slimFilterFor1TermWith1Slim() throws Exception {
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(1), ontologyId(1)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(2), ontologyId(2)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(3), ontologyId(3)));

        expectRestCallHasSlims(singletonList(ontologyId(1)), emptyList(), singletonList(singletonList(ontologyId(3))));

        ResultActions response = mockMvc.perform(
                get(SEARCH_RESOURCE)
                        .param(usageParam, SLIM_USAGE)
                        .param(idParam, ontologyId(3)));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(contentTypeToBeJson())
                .andExpect(pageInfoExists())
                .andExpect(totalNumOfResults(1))
                .andExpect(fieldsInAllResultsExist(1))
                .andExpect(valuesOccurInField(idParam, ontologyId(1)))
                .andExpect(valuesOccurInField(SLIMMED_ID_FIELD, singletonList(singletonList(ontologyId(3)))));
    }

    @Test
    public void slimFilterFor1TermWith2Slims() throws Exception {
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(1), ontologyId(1)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(2), ontologyId(2)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(3), ontologyId(3)));

        expectRestCallHasSlims(
                singletonList(ontologyId(1)),
                singletonList(IS_A),
                singletonList(asList(ontologyId(2), ontologyId(3))));

        ResultActions response = mockMvc.perform(
                get(SEARCH_RESOURCE)
                        .param(usageParam, SLIM_USAGE)
                        .param(usageRelations, IS_A)
                        .param(idParam, ontologyId(2), ontologyId(3)));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(contentTypeToBeJson())
                .andExpect(pageInfoExists())
                .andExpect(totalNumOfResults(1))
                .andExpect(fieldsInAllResultsExist(1))
                .andExpect(valuesOccurInField(idParam, ontologyId(1)))
                .andExpect(valuesOccurInField(SLIMMED_ID_FIELD,
                        singletonList(asList(ontologyId(2), ontologyId(3)))));
    }

    @Test
    public void slimFilterFor2TermsWith1Slim() throws Exception {
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(1), ontologyId(1)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(2), ontologyId(2)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(3), ontologyId(3)));

        expectRestCallHasSlims(
                asList(ontologyId(1), ontologyId(2)),
                emptyList(),
                asList(
                        singletonList(ontologyId(3)),
                        singletonList(ontologyId(3))));

        ResultActions response = mockMvc.perform(
                get(SEARCH_RESOURCE)
                        .param(usageParam, SLIM_USAGE)
                        .param(idParam, csv(ontologyId(3))));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(contentTypeToBeJson())
                .andExpect(pageInfoExists())
                .andExpect(totalNumOfResults(2))
                .andExpect(fieldsInAllResultsExist(2))
                .andExpect(valuesOccurInField(idParam, ontologyId(1), ontologyId(2)))
                .andExpect(valuesOccurInField(SLIMMED_ID_FIELD, asList(singletonList(ontologyId(3)), singletonList(ontologyId(3)))));
    }

    @Test
    public void slimFilterFor2TermsWith2Slims() throws Exception {
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(1), ontologyId(1)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(2), ontologyId(2)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(3), ontologyId(3)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(4), ontologyId(4)));

        expectRestCallHasSlims(
                asList(ontologyId(1), ontologyId(2)),
                emptyList(),
                asList(
                        asList(ontologyId(3), ontologyId(4)),
                        singletonList(ontologyId(3))));

        ResultActions response = mockMvc.perform(
                get(SEARCH_RESOURCE)
                        .param(usageParam, SLIM_USAGE)
                        .param(idParam, csv(ontologyId(3), ontologyId(4))));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(contentTypeToBeJson())
                .andExpect(pageInfoExists())
                .andExpect(totalNumOfResults(2))
                .andExpect(fieldsInAllResultsExist(2))
                .andExpect(valuesOccurInField(idParam, ontologyId(1), ontologyId(2)))
                .andExpect(valuesOccurInField(SLIMMED_ID_FIELD,
                        asList(
                                asList(ontologyId(3), ontologyId(4)),
                                singletonList(ontologyId(3)))));
    }

    @Test
    public void wrongResourcePathForSlimCausesErrorMessage() throws Exception {
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(1), ontologyId(1)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(2), ontologyId(2)));

        ResultActions response = mockMvc.perform(
                get(SEARCH_RESOURCE)
                        .param(usageParam, SLIM_USAGE)
                        .param(idParam, ontologyId(1)));

        response.andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(contentTypeToBeJson())
                .andExpect(valueStartingWithOccursInErrorMessage(FAILED_REST_FETCH_PREFIX));
    }

    @Test
    public void fetchTimeoutForSlimFilteringCauses500() throws Exception {
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(1), ontologyId(1)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(2), ontologyId(2)));

        expectRestCallResponse(GET, buildResource(resourceFormat, ontologyId(1)), withTimeout());

        ResultActions response = mockMvc.perform(
                get(SEARCH_RESOURCE)
                        .param(usageParam, SLIM_USAGE)
                        .param(idParam, ontologyId(1)));

        response.andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(contentTypeToBeJson())
                .andExpect(valueStartingWithOccursInErrorMessage(FAILED_REST_FETCH_PREFIX));
    }

    @Test
    public void serverErrorForSlimFilteringCauses500() throws Exception {
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(1), ontologyId(1)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(2), ontologyId(2)));

        expectRestCallResponse(GET, buildResource(resourceFormat, ontologyId(1)), withServerError());

        ResultActions response = mockMvc.perform(
                get(SEARCH_RESOURCE)
                        .param(usageParam, SLIM_USAGE)
                        .param(idParam, ontologyId(1)));

        response.andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(contentTypeToBeJson())
                .andExpect(valueStartingWithOccursInErrorMessage(FAILED_REST_FETCH_PREFIX));
    }

    @Test
    public void badRequestForSlimFilteringCauses500() throws Exception {
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(1), ontologyId(1)));
        annotationRepository.save(createAnnotationDocWithId(IdGeneratorUtil.createGPId(2), ontologyId(2)));

        expectRestCallResponse(GET, buildResource(resourceFormat, ontologyId(1)), withBadRequest());

        ResultActions response = mockMvc.perform(
                get(SEARCH_RESOURCE)
                        .param(usageParam, SLIM_USAGE)
                        .param(idParam, ontologyId(1)));

        response.andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(contentTypeToBeJson())
                .andExpect(valueStartingWithOccursInErrorMessage(FAILED_REST_FETCH_PREFIX));
    }


    @Override protected AnnotationDocument createAnnotationDocWithId(String geneProductId, String goId) {
        AnnotationDocument doc = createAnnotationDoc(geneProductId);
        doc.goId = goId;

        return doc;
    }

    @Override protected String ontologyId(int id) {
        return IdGeneratorUtil.createGoId(id);
    }

    private void expectRestCallHasSlims(
            List<String> termIds,
            List<String> usageRelations,
            List<List<String>> slims) {

//        expectRestCallHasValues(
//                GO_SLIM_RESOURCE_FORMAT,
//                termIds,
//                usageRelations,
//                slims,
//                OntologyRelatives.Result::setSlimsTo);
        String slimTermsCSV = slims.stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(COMMA));
        String relationsCSV = usageRelations.stream().collect(Collectors.joining(COMMA));

        expectRestCallSuccess(
                GET,
                buildResource(
                        GO_SLIM_RESOURCE_FORMAT,
                        slimTermsCSV,
                        relationsCSV),
                constructResponseObject(termIds, slims, OntologyRelatives.Result::setSlimsTo));
    }
}
