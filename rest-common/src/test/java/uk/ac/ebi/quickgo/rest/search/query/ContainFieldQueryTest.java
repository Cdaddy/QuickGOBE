package uk.ac.ebi.quickgo.rest.search.query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;

/**
 * Tests the behaviour of the {@link ContainFieldQuery}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainFieldQueryTest {
    @Mock
    private uk.ac.ebi.quickgo.rest.search.query.QueryVisitor visitor;

    @Test(expected = IllegalArgumentException.class)
    public void nullFieldThrowsException() throws Exception {
        String field = null;
        String value = "value";

        new ContainFieldQuery(field, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyFieldThrowsException() throws Exception {
        String field = "";
        String value = "value";

        new ContainFieldQuery(field, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullValueThrowsException() throws Exception {
        String field = "field";
        String value = null;

        new ContainFieldQuery(field, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyValueThrowsException() throws Exception {
        String field = "field";
        String value = "";

        new ContainFieldQuery(field, value);
    }

    @Test
    public void createFieldAndValueQuery() throws Exception {
        String field = "field";
        String value = "myName";

        ContainFieldQuery
                query = new ContainFieldQuery(field, value);

        assertThat(query.field(), is(equalTo(field)));
        assertThat(query.value(), is(equalTo(value)));
    }

    @Test
    public void visitorIsCalledCorrectly() throws Exception {
        ContainFieldQuery
                query = new ContainFieldQuery("field1", "value1");
        query.accept(visitor);
        verify(visitor).visit(query);
    }
}
