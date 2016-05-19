package uk.ac.ebi.quickgo.rest.search;

import uk.ac.ebi.quickgo.rest.search.filter.FilterConverter;
import uk.ac.ebi.quickgo.rest.search.filter.FilterConverterFactory;
import uk.ac.ebi.quickgo.rest.search.filter.RequestFilter;
import uk.ac.ebi.quickgo.rest.search.query.QueryRequest;
import uk.ac.ebi.quickgo.rest.search.query.QuickGOQuery;

import com.google.common.base.Preconditions;
import java.util.*;

/**
 * Reduces the amount of boiler plate code necessary to setup the mandatory elements to configure a
 * {@link QueryRequest}.
 *
 * The builder exposes just enough configuration methods to create a simple {@link QueryRequest}. Which means that
 * the builder will not expose advanced configuration methods such as: faceting; highlighting. To enable these you
 * will need to use more advanced templates such as: {@link FacetedSearchQueryTemplate} or
 * {@link HighlightedSearchQueryTemplate}.
 *
 * @author Ricardo Antunes
 */
public class BasicSearchQueryTemplate {
    static final int DEFAULT_PAGE_SIZE = 25;
    static final int DEFAULT_PAGE_NUMBER = 1;

    private final FilterConverterFactory filterConverterFactory;
    private final List<String> returnedFields;

    public BasicSearchQueryTemplate(List<String> returnedFields, FilterConverterFactory filterConverterFactory) {
        Preconditions.checkArgument(returnedFields != null, "Returned fields list cannot be null.");
        Preconditions.checkArgument(filterConverterFactory != null, "FilterConverterFactory can not be null.");

        this.returnedFields = returnedFields;
        this.filterConverterFactory = filterConverterFactory;
    }

    public Builder newBuilder() {
        return new Builder(
                returnedFields,
                filterConverterFactory
        );
    }

    public static class Builder implements SearchQueryRequestBuilder {
        private final FilterConverterFactory converterFactory;

        private Set<String> returnedFields;
        private Set<RequestFilter> filters;

        private QuickGOQuery query;
        private int page;
        private int pageSize;

        public Builder(List<String> returnedFields, FilterConverterFactory converterFactory) {
            this.returnedFields = new LinkedHashSet<>(returnedFields);
            this.converterFactory = converterFactory;

            page = DEFAULT_PAGE_NUMBER;
            pageSize = DEFAULT_PAGE_SIZE;

            this.filters = Collections.emptySet();
        }

        /**
         * Specify a set of filters that should be used.
         * <p>
         * Note that this argument is nullable.
         *
         * @param filters the filter queries
         * @return this {@link Builder} instance
         */
        public Builder setFilters(Set<RequestFilter> filters) {
            if (filters != null) {
                this.filters = filters;
            }

            return this;
        }

        /**
         * Specify a set of fields to return with the query response.
         * <p>
         * .
         * @param returnedFields the filter queries
         * @return this {@link Builder} instance
         */
        public Builder setReturnedFields(List<String> returnedFields) {
            if (returnedFields != null) {
                this.returnedFields = new LinkedHashSet<>(returnedFields);
            }

            return this;
        }

        /**
         * Specify the search query.
         *
         * @param query the search query.
         * @return this {@link Builder} instance
         */
        public Builder setQuery(QuickGOQuery query) {
            this.query = query;

            return this;
        }

        /**
         * Specify the number of results to be returned per page, i.e., page size.
         *
         * @param pageSize the page size.
         * @return this {@link Builder} instance
         */
        public Builder setPageSize(int pageSize) {
            this.pageSize = pageSize;

            return this;
        }

        /**
         * Specify which page of results to return.
         *
         * @param page the page of results to return.
         * @return this {@link Builder} instance
         */
        public Builder setPage(int page) {
            this.page = page;

            return this;
        }

        @Override public QueryRequest build() {
            return createBuilder().build();
        }

        protected QueryRequest.Builder createBuilder() {
            QueryRequest.Builder builder = new QueryRequest.Builder(query);
            builder.setPageParameters(page, pageSize);

            filters.stream()
                    .map(converterFactory::createConverter)
                    .map(FilterConverter::transform)
                    .forEach(builder::addQueryFilter);

            returnedFields
                    .forEach(builder::addProjectedField);

            return builder;
        }
    }
}