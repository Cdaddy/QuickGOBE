package uk.ac.ebi.quickgo.common.query.model;

import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Tests the {@link QueryRequest} implementation
 */
public class QueryRequestTest {
    @Test(expected = IllegalArgumentException.class)
    public void nullQueryThrowsException() throws Exception {
        new QueryRequest.Builder(null);
    }

    @Test
    public void buildsQueryRequestOnlyWithgetQuery() throws Exception {
        QuickGOQuery query = QuickGOQuery.createQuery("field1", "value1");

        QueryRequest request = new QueryRequest.Builder(query).build();

        assertThat(request.getQuery(), is(equalTo(query)));
        assertThat(request.getFacets(), hasSize(0));
        assertThat(request.getPage(), is(nullValue()));
        assertThat(request.getFilters(), hasSize(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPageParametersThrowsException() throws Exception {
        QuickGOQuery query = QuickGOQuery.createQuery("field1", "value1");

        new QueryRequest.Builder(query).setPageParameters(-1, 2).build();
    }

    @Test
    public void buildsQueryRequestWithQueryAndPageParameterComponents() throws Exception {
        QuickGOQuery query = QuickGOQuery.createQuery("field1", "value1");

        QueryRequest request = new QueryRequest.Builder(query)
                .setPageParameters(1, 2)
                .build();

        assertThat(request.getQuery(), is(equalTo(query)));

        Page expectedPage = request.getPage();
        assertThat(expectedPage.getPageNumber(), is(1));
        assertThat(expectedPage.getPageSize(), is(2));

        assertThat(request.getFacets(), hasSize(0));
        assertThat(request.getFilters(), hasSize(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidFacetFieldThrowsException() throws Exception {
        QuickGOQuery query = QuickGOQuery.createQuery("field1", "value1");

        new QueryRequest.Builder(query)
                .addFacetField(null)
                .build();
    }

    @Test
    public void buildsQueryRequestWithQueryAndTwoFacetFields() throws Exception {
        QuickGOQuery query = QuickGOQuery.createQuery("field1", "value1");

        String facetField1 = "facet1";
        String facetField2 = "facet2";

        QueryRequest request = new QueryRequest.Builder(query)
                .addFacetField(facetField1)
                .addFacetField(facetField2)
                .build();

        assertThat(request.getQuery(), is(equalTo(query)));
        assertThat(request.getPage(), is(nullValue()));

        Collection<Facet> expectedFacets = request.getFacets();

        assertThat(expectedFacets, hasSize(2));
        assertThat(extractFacetFields(expectedFacets), containsInAnyOrder(facetField1, facetField2));
        assertThat(request.getFilters(), hasSize(0));
    }

    private Collection<String> extractFacetFields(Collection<Facet> facets) {
        return facets.stream()
                .map(Facet::getField)
                .collect(Collectors.toList());
    }

    @Test
    public void buildsQueryRequestWithQueryAndFiltergetQuery() throws Exception {
        QuickGOQuery query = QuickGOQuery.createQuery("field1", "value1");

        QuickGOQuery filterQuery1 = QuickGOQuery.createQuery("field1", "value1");
        QuickGOQuery filterQuery2 = QuickGOQuery.createQuery("field1", "value1");

        QueryRequest request = new QueryRequest.Builder(query)
                .addQueryFilter(filterQuery1)
                .addQueryFilter(filterQuery2)
                .build();

        assertThat(request.getQuery(), is(equalTo(query)));
        assertThat(request.getPage(), is(nullValue()));
        assertThat(request.getFacets(), hasSize(0));

        assertThat(request.getFilters(), containsInAnyOrder(filterQuery1, filterQuery2));
    }

    @Test
    public void addFilterQueryToQueryRequest() throws Exception {
        QuickGOQuery query = QuickGOQuery.createQuery("field1", "value1");

        QueryRequest request = new QueryRequest.Builder(query)
                .build();

        QuickGOQuery filterQuery = QuickGOQuery.createQuery("filter", "value");
        request.addFilter(filterQuery);

        assertThat(request.getFilters(), hasSize(1));
        assertThat(request.getFilters().get(0), is(filterQuery));
    }
}