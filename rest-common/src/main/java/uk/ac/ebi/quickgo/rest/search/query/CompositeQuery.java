package uk.ac.ebi.quickgo.rest.search.query;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Set;

/**
 * Represents a query that is a result of a logical operation (i.e. AND, OR, NOT), of one or more
 * {@link QuickGOQuery}.
 */
public class CompositeQuery extends QuickGOQuery {
    private QueryOp queryOperator;

    private Set<QuickGOQuery> queries;

    public CompositeQuery(Set<QuickGOQuery> queries, QueryOp operator) {
        Preconditions.checkArgument(queries != null && !queries.isEmpty(),
                "Queries to compose cannot be null or empty");
        Preconditions.checkArgument(operator != null, "Logical query operator cannot be null");

        this.queryOperator = operator;
        this.queries = Collections.unmodifiableSet(queries);
    }

    @Override public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public QueryOp queryOperator() {
        return queryOperator;
    }

    public Set<QuickGOQuery> queries() {
        return queries;
    }

    public enum QueryOp {
        OR,
        AND,
        NOT
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompositeQuery that = (CompositeQuery) o;

        if (queryOperator != that.queryOperator) {
            return false;
        }
        return queries != null ? queries.equals(that.queries) : that.queries == null;

    }

    @Override public int hashCode() {
        int result = queryOperator != null ? queryOperator.hashCode() : 0;
        result = 31 * result + (queries != null ? queries.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "CompositeQuery{" +
                "queryOperator=" + queryOperator +
                ", queries=" + queries +
                '}';
    }
}
