package uk.ac.ebi.quickgo.rest.search.request.converter;

import uk.ac.ebi.quickgo.rest.search.query.QuickGOQuery;
import uk.ac.ebi.quickgo.rest.search.request.FilterRequest;
import uk.ac.ebi.quickgo.rest.search.request.config.FilterConfig;

import com.google.common.base.Preconditions;
import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.ebi.quickgo.rest.search.query.QuickGOQuery.or;
import static uk.ac.ebi.quickgo.rest.search.query.QuickGOQuery.and;

/**
 * Defines the conversion of a simple request to a corresponding {@link QuickGOQuery}.
 *
 * Created by Edd on 05/06/2016.
 */
class SimpleFilterConverter implements FilterConverter<FilterRequest, QuickGOQuery> {
    //Below const. taken from annotation-common:uk.ac.ebi.quickgo.annotation.common.AnnotationFields
    private static final String PROTEIN = "protein";
    private static final String GENE_PRODUCT_TYPE = "geneProductType_unsorted";
    private static final String GENE_PRODUCT_SUBSET = "geneProductSubset_unsorted";
    private static final String PROTEOME = "proteome_unsorted";

    private final FilterConfig filterConfig;

    SimpleFilterConverter(FilterConfig filterConfig) {
        Preconditions.checkArgument(filterConfig != null, "FilterConfig cannot be null");

        this.filterConfig = filterConfig;
    }

    /**
     * Converts a given {@link FilterRequest} into a corresponding {@link QuickGOQuery}.
     * If {@code request} has multiple values, they are ORed together in the
     * resulting query.
     *
     * @param request the client request
     * @return a {@link QuickGOQuery} corresponding to a join query, representing the original client request
     */
    @Override public ConvertedFilter<QuickGOQuery> transform(FilterRequest request) {
        Preconditions.checkArgument(request != null, "FilterRequest cannot be null");
        Preconditions.checkArgument(request.getValues().size() >= 1,
                "FilterRequest should contain at least 1 property for application to a SimpleRequestConverter, " +
                        "instead it contained " + request.getValues().size());
        return request.getValues().size() == 1 ? new ConvertedFilter<>(getQuickGOQuery(request)) : new
                ConvertedFilter<>(getQuickGOQueryForMultipleProperties(request));
    }

    /**
     * Computes the {@link QuickGOQuery} corresponding to for the specified {@link FilterRequest} and {@code values}.
     *
     * <p>Note: inlining this method, as parameter to another method, lead to compilation failure, due to:
     * <ul>
     *     <li>http://stackoverflow.com/questions/25523375/java8-lambdas-and-exceptions</li>
     *     <li>https://bugs.openjdk.java.net/browse/JDK-8054569</li>
     * </ul>
     *
     * @param request the filter request
     * @return the corresponding {@link QuickGOQuery}
     */
    private QuickGOQuery getQuickGOQuery(FilterRequest request) {
        Set<QuickGOQuery> queries = request.getValues()
                                           .stream()
                                           .flatMap(Collection::stream)
                                           .map(value -> QuickGOQuery.createQuery(request.getSignature()
                                                                                         .stream()
                                                                                         .collect(Collectors.joining()),
                                                                                  value))
                                           .collect(Collectors.toSet());

        return or(queries.toArray(new QuickGOQuery[queries.size()]));
    }

    private QuickGOQuery getQuickGOQueryForMultipleProperties(FilterRequest request) {

        //Handling request manually
        // GOA-3266
        if (request.getSignature().contains(GENE_PRODUCT_TYPE)) {
            return handleGeneProductTypeMultiplePropertiesFilter(request);
        }else{
            return handleGenericMultiplePropertiesInSingleFilter(request);
        }
    }

    private QuickGOQuery handleGeneProductTypeMultiplePropertiesFilter(FilterRequest request){
        QuickGOQuery gptOtherQuery = null;

        final List<String> values = request.getProperties().get(GENE_PRODUCT_TYPE);

        boolean proteinRemoved = values.remove(PROTEIN);

        if (!values.isEmpty()) {
            gptOtherQuery = or(values.stream().map(val -> QuickGOQuery.createQuery
                    (GENE_PRODUCT_TYPE, val)).toArray(size -> new QuickGOQuery[size]));
        }

        if (proteinRemoved) {

            QuickGOQuery gptProteinQuery = QuickGOQuery.createQuery(GENE_PRODUCT_TYPE, PROTEIN);

            if (request.getProperties().containsKey(GENE_PRODUCT_SUBSET)) {
                final QuickGOQuery subset = or(request.getProperties().get(GENE_PRODUCT_SUBSET).stream()
                        .map(val -> QuickGOQuery.createQuery(GENE_PRODUCT_SUBSET, val))
                        .toArray(size -> new QuickGOQuery[size]));
                gptProteinQuery = and(gptProteinQuery, subset);
            }

            if (request.getProperties().containsKey(PROTEOME)) {
                final QuickGOQuery proteome = or(request.getProperties().get(PROTEOME).stream().map(val ->
                        QuickGOQuery.createQuery(PROTEOME, val)).toArray(size -> new QuickGOQuery[size]));
                gptProteinQuery = and(gptProteinQuery, proteome);
            }

            return gptOtherQuery == null ? gptProteinQuery : or(gptProteinQuery, gptOtherQuery);
        }
        return gptOtherQuery;
    }

    private QuickGOQuery handleGenericMultiplePropertiesInSingleFilter(FilterRequest request){
        //Remaining request handle to make it fail safe
        //can be change in future
        Set<QuickGOQuery> andQuerySet = new HashSet<>();
        request.getProperties().forEach(
                (fieldName, values) -> {
                    Set<QuickGOQuery> orQuerySet = new HashSet<>();
                    values.forEach(value -> orQuerySet.add(QuickGOQuery.createQuery(fieldName, value)));
                    andQuerySet.add(or(orQuerySet.toArray(new QuickGOQuery[orQuerySet.size()])));
                }
        );
        return and(andQuerySet.toArray(new QuickGOQuery[andQuerySet.size()]));
    }
}
