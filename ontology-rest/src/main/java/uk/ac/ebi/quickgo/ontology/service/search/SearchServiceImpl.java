package uk.ac.ebi.quickgo.ontology.service.search;

import uk.ac.ebi.quickgo.rest.search.RequestRetrieval;
import uk.ac.ebi.quickgo.rest.search.SearchService;
import uk.ac.ebi.quickgo.rest.search.query.QueryRequest;
import uk.ac.ebi.quickgo.rest.search.results.QueryResult;
import uk.ac.ebi.quickgo.ontology.model.OBOTerm;

/**
 * The search service implementation for ontologies. This class implements the
 * {@link SearchService} interface, and delegates retrieval of ontology results
 * to a {@link RequestRetrieval} instance.
 *
 * Created 18/01/16
 * @author Edd
 */
public class SearchServiceImpl implements SearchService<OBOTerm> {
    private final RequestRetrieval<OBOTerm> requestRetrieval;

    public SearchServiceImpl(RequestRetrieval<OBOTerm> requestRetrieval) {
        this.requestRetrieval = requestRetrieval;
    }

    @Override
    public QueryResult<OBOTerm> findByQuery(QueryRequest request) {
        return this.requestRetrieval.findByQuery(request);
    }
}
