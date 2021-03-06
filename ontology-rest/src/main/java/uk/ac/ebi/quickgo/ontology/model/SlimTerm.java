package uk.ac.ebi.quickgo.ontology.model;

import java.util.Collections;
import java.util.List;

/**
 * A minimal class whose purpose is to represent only term IDs and a list of other term IDs, to which the id slims up
 * to.
 *
 * Created 11/10/17
 * @author Edd
 */
public class SlimTerm {
    public SlimTerm(String slimsFromId, List<String> slimmedTerms) {
        this.slimsFromId = slimsFromId;
        this.slimsToIds = Collections.unmodifiableList(slimmedTerms);
    }

    public final String slimsFromId;
    public final List<String> slimsToIds;
}
