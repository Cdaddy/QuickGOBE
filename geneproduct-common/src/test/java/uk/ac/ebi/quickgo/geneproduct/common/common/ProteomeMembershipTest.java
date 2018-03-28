package uk.ac.ebi.quickgo.geneproduct.common.common;

import uk.ac.ebi.quickgo.geneproduct.common.ProteomeMembership;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;
import static uk.ac.ebi.quickgo.geneproduct.common.ProteomeMembership.COMPLETE;
import static uk.ac.ebi.quickgo.geneproduct.common.ProteomeMembership.NONE;
import static uk.ac.ebi.quickgo.geneproduct.common.ProteomeMembership.NOT_APPLICABLE;
import static uk.ac.ebi.quickgo.geneproduct.common.ProteomeMembership.REFERENCE;

/**
 * @author Tony Wardell
 * Date: 15/03/2018
 * Time: 13:16
 * Created with IntelliJ IDEA.
 */
public class ProteomeMembershipTest {

    @Test
    public void membershipCreationPermutations() {
        assertThat(ProteomeMembership.membership(false, true, true), is(NOT_APPLICABLE.toString()));
        assertThat(ProteomeMembership.membership(false, false, true), is(NOT_APPLICABLE.toString()));
        assertThat(ProteomeMembership.membership(false, true, false), is(NOT_APPLICABLE.toString()));
        assertThat(ProteomeMembership.membership(true, true, true), is(REFERENCE.toString()));
        assertThat(ProteomeMembership.membership(true, true, false), is(REFERENCE.toString()));
        assertThat(ProteomeMembership.membership(true, false, true), is(COMPLETE.toString()));
        assertThat(ProteomeMembership.membership(true, false, false), is(NONE.toString()));
        assertThat(ProteomeMembership.membership(true, true, true), is(REFERENCE.toString()));
    }

    @Test
    public void fromString() {
        assertThat(ProteomeMembership.fromString("Not applicable"), is(NOT_APPLICABLE));
        assertThat(ProteomeMembership.fromString("Not aPPlicable"), is(NOT_APPLICABLE));

        assertThat(ProteomeMembership.fromString("Reference"), is(REFERENCE));
        assertThat(ProteomeMembership.fromString("referEnce"), is(REFERENCE));

        assertThat(ProteomeMembership.fromString("Complete"), is(COMPLETE));
        assertThat(ProteomeMembership.fromString("CompLete"), is(COMPLETE));

        assertThat(ProteomeMembership.fromString("None"), is(NONE));
        assertThat(ProteomeMembership.fromString("NoNe"), is(NONE));

    }

}
