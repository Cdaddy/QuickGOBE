package uk.ac.ebi.quickgo.rest.search;

import org.apache.solr.client.solrj.util.ClientUtils;

/**
 * An Solr specific implementation for sanitizing Solr queries.
 *
 * Created 29/02/16
 * @author Edd
 */
public class SolrQueryStringSanitizer implements QueryStringSanitizer {
    /**
     * Sanitize a Solr specific query string
     *
     * @param query the query to be sanitized
     * @return the sanitized query
     */
    @Override public String sanitize(String query) {
        return ClientUtils.escapeQueryChars(query);
    }
}
