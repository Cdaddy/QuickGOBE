package uk.ac.ebi.quickgo.geneproduct.common;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/**
 * An enumeration of the possible states of proteome membership a gene product can have.
 * @author Tony Wardell
 * Date: 06/03/2018
 * Time: 10:33
 * Created with IntelliJ IDEA.
 */
public enum ProteomeMembership {
    REFERENCE("Reference"),
    COMPLETE("Complete"),
    NONE("None"),
    NOT_APPLICABLE("Not applicable");

    private String value;

    ProteomeMembership(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    /**
     * Provide a ProteomeMembership derived from a string representation.
     * @param value string tested for ProteomeMembership equivalence.
     * @return ProteomeMembership
     */
    public static ProteomeMembership fromString(String value) {
        return Arrays.stream(values())
                .filter(v -> v.toString().equalsIgnoreCase(value))
                .findFirst()
                .orElse(NOT_APPLICABLE);
    }

    /**
     * Define the predicates required and order of importance to work out which Proteome membership category is
     * applicable.
     * @param isProtein is the gene product a protein
     * @param isReferenceProteome is the gene product a reference proteome
     * @param isComplete is the gene product a member of a complete proteome.
     * @return the String representation of the ProteomeMembership matching the applied constraints
     */
    public static String membership(boolean isProtein, boolean isReferenceProteome, boolean isComplete) {

        if (!isProtein) {
            return NOT_APPLICABLE.toString();
        } else if (isReferenceProteome) {
            return REFERENCE.toString();
        } else if (isComplete) {
            return COMPLETE.toString();
        }
        return NONE.toString();
    }
}