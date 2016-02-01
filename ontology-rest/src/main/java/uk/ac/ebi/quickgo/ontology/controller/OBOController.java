package uk.ac.ebi.quickgo.ontology.controller;

import uk.ac.ebi.quickgo.ontology.model.OBOTerm;
import uk.ac.ebi.quickgo.ontology.service.OntologyService;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static java.util.Objects.requireNonNull;

/**
 * Abstract controller defining common end-points of an OBO related
 * REST API.
 *
 * Created 27/11/15
 * @author Edd
 */
public abstract class OBOController<T extends OBOTerm> {
    private final OntologyService<T> ontologyService;

    public abstract boolean isValidId(String id);

    public OBOController(OntologyService<T> ontologyService) {
        this.ontologyService = requireNonNull(ontologyService);
    }

    /**
     * An empty or unknown path should result in a bad request
     *
     * @return a 400 response
     */
    @RequestMapping(value = "/*", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<T> emptyId() {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Get core information about a term based on its id
     *
     * @param id ontology identifier
     *
     * @return
     * <ul>
     * <li>id is found: response consists of a 200 with the core information of the ontology term</li>
     * <li>id is not found: response returns 404</li>
     * <li>id is not in correct format: response returns 400</li>
     * </ul>
     */
    @RequestMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<T> findCoreTerm(@PathVariable(value = "id") String id) {
        if (!isValidId(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return getTermResponse(ontologyService.findCoreInfoByOntologyId(id));
    }

    /**
     * Get complete information about a term based on its id
     * @param id ontology identifier
     * @return
     * <ul>
     * <li>id is found: response consists of a 200 with all of the information the ontology term has</li>
     * <li>id is not found: response returns 404</li>
     * <li>id is not in correct format: response returns 400</li>
     * </ul>
     */
    @RequestMapping(value = "/{id}/complete", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<T> findCompleteTerm(@PathVariable(value = "id") String id) {
        if (!isValidId(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return getTermResponse(ontologyService.findCompleteInfoByOntologyId(id));
    }

    /**
     * Get history information about a term based on its id
     *
     * @param id ontology identifier
     * @return
     * <ul>
     * <li>id is found: response consists of a 200 with the history of the ontology term</li>
     * <li>id is not found: response returns 404</li>
     * <li>id is not in correct format: response returns 400</li>
     * </ul>
     */
    @RequestMapping(value = "/{id}/history", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<T> findTermHistory(@PathVariable(value = "id") String id) {
        if (!isValidId(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return getTermResponse(ontologyService.findHistoryInfoByOntologyId(id));
    }

    /**
     * Get cross-reference information about a term based on its id
     * @param id ontology identifier
     * @return
     * <ul>
     * <li>id is found: response consists of a 200 with the cross-references of the ontology term</li>
     * <li>id is not found: response returns 404</li>
     * <li>id is not in correct format: response returns 400</li>
     * </ul>
     */
    @RequestMapping(value = "/{id}/xrefs", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<T> findTermXRefs(@PathVariable(value = "id") String id) {
        if (!isValidId(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return getTermResponse(ontologyService.findXRefsInfoByOntologyId(id));
    }

    /**
     * Get taxonomy constraint and blacklist information about a term based on its id
     * @param id ontology identifier
     * @return
     * <ul>
     * <li>id is found: response consists of a 200 with the constraint and blacklist of an ontology term</li>
     * <li>id is not found: response returns 404</li>
     * <li>id is not in correct format: response returns 400</li>
     * </ul>
     */
    @RequestMapping(value = "/{id}/constraints", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<T> findTermTaxonConstraints(@PathVariable(value = "id") String id) {
        if (!isValidId(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return getTermResponse(ontologyService.findTaxonConstraintsInfoByOntologyId(id));
    }

    /**
     * Get cross-ontology relationship information about a term based on its id
     * @param id ontology identifier
     * @return
     * <ul>
     * <li>id is found: response consists of a 200 with cross-ontology relations of the ontology term</li>
     * <li>id is not found: response returns 404</li>
     * <li>id is not in correct format: response returns 400</li>
     * </ul>
     */
    @RequestMapping(value = "/{id}/xontologyrelations", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<T> findTermXOntologyRelations(@PathVariable(value = "id") String id) {
        if (!isValidId(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return getTermResponse(ontologyService.findXORelationsInfoByOntologyId(id));
    }

    /**
     * Get annotation guideline information about a term based on its id
     * @param id ontology identifier
     * @return
     * <ul>
     * <li>id is found: response consists of a 200 with annotation guidelines of the ontology term</li>
     * <li>id is not found: response returns 404</li>
     * <li>id is not in correct format: response returns 400</li>
     * </ul>
     */
    @RequestMapping(value = "/{id}/guidelines", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<T> findTermAnnotationGuideLines(@PathVariable(value = "id") String id) {
        if (!isValidId(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return getTermResponse(ontologyService.findAnnotationGuideLinesInfoByOntologyId(id));
    }

    private ResponseEntity<T> getTermResponse(Optional<T> optionalECODoc) {
        return optionalECODoc.map(ontology -> new ResponseEntity<>(ontology, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}