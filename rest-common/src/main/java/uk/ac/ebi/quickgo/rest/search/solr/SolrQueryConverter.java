package uk.ac.ebi.quickgo.rest.search.solr;

import uk.ac.ebi.quickgo.rest.search.SolrQueryStringSanitizer;
import uk.ac.ebi.quickgo.rest.search.query.*;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * Converts a {@link QueryRequest} into a {@link SolrQuery} object.
 */
public class SolrQueryConverter implements QueryVisitor<String>, QueryRequestConverter<SolrQuery> {
    public static final String SOLR_FIELD_SEPARATOR = ":";

    static final String CROSS_CORE_JOIN_SYNTAX = "{!join from=%s to=%s fromIndex=%s} %s";

    private static final String FACET_ANALYTICS_ID = "json.facet";
    private static final int MIN_COUNT_TO_DISPLAY_FACET = 1;

    private final String requestHandler;
    private final SolrQueryStringSanitizer queryStringSanitizer;

    public SolrQueryConverter(String requestHandler) {
        Preconditions.checkArgument(requestHandler != null && !requestHandler.trim().isEmpty(),
                "Request handler name cannot be null or empty");

        this.requestHandler = requestHandler;
        this.queryStringSanitizer = new SolrQueryStringSanitizer();
    }

    @Override public String visit(FieldQuery query) {
        return "(" + query.field() + SOLR_FIELD_SEPARATOR + queryStringSanitizer.sanitize(query.value()) + ")";
    }

    @Override public String visit(CompositeQuery query) {
        CompositeQuery.QueryOp operator = query.queryOperator();
        Set<QuickGOQuery> queries = query.queries();

        if (queries.size() == 1 && operator.equals(CompositeQuery.QueryOp.NOT)) {
            String singletonQuery = queries.iterator().next().accept(this);
            return CompositeQuery.QueryOp.NOT + " (" + singletonQuery + ")";
        } else {
            String operatorText = " " + operator.name() + " ";

            return queries.stream()
                    .map(q -> q.accept(this))
                    .collect(Collectors.joining(operatorText, "(", ")"));
        }
    }

    @Override public String visit(NoFieldQuery query) {
        return "(" + queryStringSanitizer.sanitize(query.getValue()) + ")";
    }

    @Override public String visit(AllQuery query) {
        return "*:*";
    }

    @Override public String visit(JoinQuery query) {
        String fromFilterString;

        if (query.getFromFilter() != null) {
            fromFilterString = query.getFromFilter().accept(this);
        } else {
            fromFilterString = "";
        }

        return String.format(CROSS_CORE_JOIN_SYNTAX, query.getJoinFromAttribute(), query.getJoinToAttribute(),
                query.getJoinFromTable(), fromFilterString);
    }

    @Override public SolrQuery convert(QueryRequest request) {
        Preconditions.checkArgument(request != null, "Cannot convert null query request");

        final SolrQuery solrQuery = new SolrQuery();

        solrQuery.setQuery(request.getQuery().accept(this));
        solrQuery.setRequestHandler(requestHandler);

        Page page = request.getPage();

        if (page != null) {
            solrQuery.setStart(calculateRowsFromPage(page.getPageNumber(), page.getPageSize()));
            solrQuery.setRows(page.getPageSize());
        }

        List<QuickGOQuery> filterQueries = request.getFilters();

        if (!filterQueries.isEmpty()) {
            List<String> solrFilters = filterQueries.stream()
                    .map(fq -> fq.accept(this))
                    .collect(Collectors.toList());

            solrQuery.setFilterQueries(solrFilters.toArray(new String[solrFilters.size()]));
        }

        if (!request.getFacets().isEmpty()) {
            request.getFacets().forEach(facet -> solrQuery.addFacetField(facet.getField()));
            solrQuery.setFacetMinCount(MIN_COUNT_TO_DISPLAY_FACET);
        }

        if (!request.getHighlightedFields().isEmpty()) {
            solrQuery.setHighlight(true);
            request.getHighlightedFields().stream().forEach(field -> solrQuery.addHighlightField(field.getField()));
            solrQuery.setHighlightSimplePre(request.getHighlightStartDelim());
            solrQuery.setHighlightSimplePost(request.getHighlightEndDelim());
        }

        if (!request.getProjectedFields().isEmpty()) {
            request.getProjectedFields().forEach(field -> solrQuery.addField(field.getField()));
        }

        if (!request.getAggregates().isEmpty()) {
            solrQuery.setParam(FACET_ANALYTICS_ID, mockJsonFacet());
        }

        {
            return solrQuery;
        }
    }

    private static final String STATS_PREFIX = "stats";

//    private String convertAggregates(List<Aggregate> aggregates) {
//        StringBuilder jsonFacet = new StringBuilder("json.facet={");
//
//        for (Aggregate aggregate : aggregates) {
//            jsonFacet.append(aggregate.getName()).append(":");
//
//            Set<AggregateField> aggFields = aggregate.getFields();
//
//            if(!aggFields.isEmpty()) {
//                jsonFacet.append(":");
//                for (AggregateField aggField : aggFields) {
//                    jsonFacet.append(aggField)
//                }
//            }
//        }
//        jsonFacet.append("}");
//    }

    private String mockJsonFacet() {
        return "{\n" +
                "            unique_annotations:\"unique(id)\",\n" +
                "            unique_geneProductId:\"unique(geneProductId)\",\n" +
                "            stats_dbSubset:{\n" +
                "              field:dbSubset,\n" +
                "              type:terms,\n" +
                "              facet:{\n" +
                "                  unique_id:\"unique(id)\",\n" +
                "                  unique_geneProductID:\"unique(geneProductId)\"\n" +
                "              }\n" +
                "          }\n" +
                "}";
    }

    private int calculateRowsFromPage(int page, int numRows) {
        return (page - 1) * numRows;
    }
}