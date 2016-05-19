package uk.ac.ebi.quickgo.rest.search.solr;

import uk.ac.ebi.quickgo.rest.search.QueryResultConverter;
import uk.ac.ebi.quickgo.rest.search.query.Page;
import uk.ac.ebi.quickgo.rest.search.query.QueryRequest;
import uk.ac.ebi.quickgo.rest.search.results.*;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 * Abstract class that deals with the conversion of the non type specific aspects of a {@link QueryResult}.
 *
 * @author Ricardo Antunes
 */
public abstract class AbstractSolrQueryResultConverter<T> implements QueryResultConverter<T, QueryResponse> {
    private final QueryResultHighlightingConverter<SolrDocumentList, Map<String, Map<String, List<String>>>>
            queryResultHighlightingConverter;

    public AbstractSolrQueryResultConverter() {
        queryResultHighlightingConverter = null;
    }

    public AbstractSolrQueryResultConverter(QueryResultHighlightingConverter<SolrDocumentList, Map<String, Map<String, List<String>>>> queryResultHighlightingConverter){
        Preconditions.checkArgument(queryResultHighlightingConverter != null, "Field map converter cannot be null");

        this.queryResultHighlightingConverter = queryResultHighlightingConverter;
    }

    @Override public QueryResult<T> convert(QueryResponse toConvert, QueryRequest request) {
        Preconditions.checkArgument(toConvert != null, "Query response cannot be null");
        Preconditions.checkArgument(request != null, "Query request cannot be null");

        SolrDocumentList solrResults = toConvert.getResults();
        Page page = request.getPage();
        List<FacetField> facetFieldResults = toConvert.getFacetFields();
        Map<String, Map<String, List<String>>> resultHighlights = toConvert.getHighlighting();

        long totalNumberOfResults = 0;

        List<T> results;

        if (solrResults != null) {
            totalNumberOfResults = solrResults.getNumFound();
            results = convertResults(solrResults);
        } else {
            results = Collections.emptyList();
        }

        PageInfo pageInfo = null;

        if (page != null) {
            pageInfo = convertPage(page, totalNumberOfResults);
        }

        Facet facet = null;

        if (facetFieldResults != null && !facetFieldResults.isEmpty()) {
            facet = convertFacet(facetFieldResults);
        }

        List<DocHighlight> highlights = null;

        if (resultHighlights != null && solrResults != null && queryResultHighlightingConverter != null) {
            highlights = queryResultHighlightingConverter.convertResultHighlighting(solrResults, resultHighlights);
        }

        return new QueryResult.Builder<>(totalNumberOfResults, results)
                .withPageInfo(pageInfo)
                .withFacets(facet)
                .appendHighlights(highlights)
                .build();
    }

    /**
     * Creates a {@link Facet} object containing all facet related information.
     *
     * @param fields Solr facet fields to convert
     * @return a domain facet object
     */
    private Facet convertFacet(List<FacetField> fields) {
        Facet facet = new Facet();

        fields.stream()
                .map(this::convertFacetField)
                .forEach(facet::addFacetField);

        return facet;
    }

    /**
     * Converts a Solr {@link FacetField} into a domain {@link FieldFacet} object
     *
     * @param solrField field containing facet information
     * @return a domain facet field
     */
    private FieldFacet convertFacetField(FacetField solrField) {
        String name = solrField.getName();

        final FieldFacet domainFieldFacet = new FieldFacet(name);

        solrField.getValues().stream()
                .forEach(count -> domainFieldFacet.addCategory(count.getName(), count.getCount()));

        return domainFieldFacet;
    }

    private PageInfo convertPage(Page page, long totalNumberOfResults) {
        int resultsPerPage = page.getPageSize();
        int totalPages = (int) Math.ceil((double) totalNumberOfResults / (double) resultsPerPage);

        Preconditions.checkArgument((page.getPageNumber()-1)<=totalPages, "The requested page number should not be " +
                "greater " +
                "than the number of pages available.");
        int currentPage = (totalPages == 0 ? 0 : page.getPageNumber());

        return new PageInfo(totalPages, currentPage, resultsPerPage);
    }

    protected abstract List<T> convertResults(SolrDocumentList results);
}
