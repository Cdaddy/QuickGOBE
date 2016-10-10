package uk.ac.ebi.quickgo.ontology.service;

import uk.ac.ebi.quickgo.ontology.common.OntologyRepository;
import uk.ac.ebi.quickgo.ontology.common.coterm.CoTermSource;
import uk.ac.ebi.quickgo.ontology.common.document.OntologyType;
import uk.ac.ebi.quickgo.ontology.common.coterm.CoTerm;
import uk.ac.ebi.quickgo.ontology.model.OBOTerm;
import uk.ac.ebi.quickgo.ontology.model.OntologyRelationType;
import uk.ac.ebi.quickgo.ontology.model.OntologyRelationship;
import uk.ac.ebi.quickgo.rest.search.query.Page;
import uk.ac.ebi.quickgo.rest.search.results.QueryResult;

import java.util.List;
import java.util.Set;

/**
 * Service layer for retrieving results from an underlying searchable data store.
 *
 * See also {@link OntologyRepository}
 *
 * Created 11/11/15
 * @author Edd
 */
public interface OntologyService<T extends OBOTerm> {
    /**
     * Search over everything and return a list of results,
     * which fulfil the specification of the {@code pageable} instance.
     * @param page the requested page of results
     * @return the page of results
     */
    QueryResult<T> findAllByOntologyType(OntologyType type, Page page);

    /**
     * Find the complete data set stored for a specified list of ontology IDs.
     * @param ids the ontology IDs
     * @return a {@link List} of {@link OBOTerm} instances corresponding to the ontology term ids containing the
     * chosen information
     */
    List<T> findCompleteInfoByOntologyId(List<String> ids);

    /**
     * Find the core data set stored for a specified list of ontology IDs.
     * @param ids the ontology IDs
     * @return a {@link List} of {@link OBOTerm} instances corresponding to the ontology term ids containing the
     * chosen information
     */
    List<T> findCoreInfoByOntologyId(List<String> ids);

    /**
     * Find historical changes related to specified list of ontology IDs.
     * @param ids the ontology IDs
     * @return a {@link List} of {@link OBOTerm} instances corresponding to the ontology term ids containing the
     * chosen information
     */
    List<T> findHistoryInfoByOntologyId(List<String> ids);

    /**
     * Find the cross-references stored for a specified list of ontology IDs.
     * @param ids the ontology IDs
     * @return a {@link List} of {@link OBOTerm} instances corresponding to the ontology term ids containing the
     * chosen information
     */
    List<T> findXRefsInfoByOntologyId(List<String> ids);

    /**
     * Find the taxonomy constraints stored for a specified list of ontology IDs.
     * @param ids the ontology IDs
     * @return a {@link List} of {@link OBOTerm} instances corresponding to the ontology term ids containing the
     * chosen information
     */
    List<T> findTaxonConstraintsInfoByOntologyId(List<String> ids);

    /**
     * Find information about cross-ontology relations, for a specified list of ontology IDs.
     * @param ids the ontology IDs
     * @return a {@link List} of {@link OBOTerm} instances corresponding to the ontology term ids containing the
     * chosen information
     */
    List<T> findXORelationsInfoByOntologyId(List<String> ids);

    /**
     * Find the annotation guidelines for a specified list of ontology IDs.
     * @param ids the ontology IDs
     * @return a {@link List} of {@link OBOTerm} instances corresponding to the ontology term ids containing the
     * chosen information
     */
    List<T> findAnnotationGuideLinesInfoByOntologyId(List<String> ids);

    /**
     * Find the list of paths between two sets of vertices in a graph, navigable via
     * a specified set of relations.
     *
     * @param startingIds the starting ids from which returned paths must start
     * @param endingIds the ending ids from which returned paths end
     * @param relations a varargs value containing the relationships over which paths can only travel.
     *                  By omitting a {@code relation} value, all paths will be returned.
     * @return a list of paths from {@code startingIds} to {@code endingIds} via {@code relations}
     */
    List<List<OntologyRelationship>> paths(
            Set<String> startingIds,
            Set<String> endingIds,
            OntologyRelationType... relations);

    /**
     * Find the set of ancestor vertices reachable from a list of ids, {@code ids}, navigable via a specified
     * set of relations.
     *
     * @param ids a {@link List} of ids whose ancestors one is interested in
     * @param relations a varargs value containing the relationships over which paths can only travel.
     *                  By omitting a {@code relation} value, all paths will be returned.
     * @return a {@link List} of {@link OBOTerm} instances corresponding to the ontology term ids containing the
     * chosen information
     */
    List<T> findAncestorsInfoByOntologyId(List<String> ids, OntologyRelationType... relations);

    /**
     * Find the set of descendant ids reachable from a specified list of ids, {@code ids}, navigable via a specified
     * set of relations.
     *
     * @param ids a {@link List} ids whose descendants one is interested in
     * @param relations a varargs value containing the relationships over which paths can only travel.
     *                  By omitting a {@code relation} value, all paths will be returned.
     * @return a {@link List} of {@link OBOTerm} instances corresponding to the ontology term ids containing the
     * chosen information
     */
    List<T> findDescendantsInfoByOntologyId(List<String> ids, OntologyRelationType... relations);

    /**
     * For a single GO Term, retrieve a list of co-occurring terms and related statistics, in order of the
     * co-occurring terms similarity probablity (descending).
     * @param id is the target GO term, for which the method will retrieve co-occurring terms (GO terms that are used
     * to annotation the same gene products as this GO Term is used to annotate).
     * @param source is the method from which the annotation that uses the GO term was generated.
     * @param limit Limit the number of co-occurring terms return to the limit specified.
     * @param similarityThreshold if specified (greater than zero), only return co-occurring GO terms with a
     * similarity ratio above this figure.
     * @return a list of objects, each one of which represent a GO Term that is used to annotate the same gene
     * product as the id. Each object holds statistics related to that co-occurrence.
     */
    List<CoTerm> findCoTermsByOntologyId(String id, CoTermSource source, int limit, Float similarityThreshold);
}
