package uk.ac.ebi.quickgo.rest.controller.search;

/**
 * Checks if a given value is a searchable field
 */
public class OntologySearchableField implements SearchableField {
    public enum Search {
        synonymName,
        id,
        name,
        definition
    }

    @Override public boolean isSearchable(String field) {
        for (Search searchableField : Search.values()) {
            if (searchableField.name().equals(field)) {
                return true;
            }
        }
        return false;
    }

}
