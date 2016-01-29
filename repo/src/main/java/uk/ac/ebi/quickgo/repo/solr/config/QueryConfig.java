package uk.ac.ebi.quickgo.repo.solr.config;

import uk.ac.ebi.quickgo.repo.solr.query.model.QueryRequestConverter;
import uk.ac.ebi.quickgo.repo.solr.query.model.SolrQueryConverter;

import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines beans and configuration related to queries, which can
 * be used to access the underlying data store.
 *
 * Created 19/01/16
 * @author Edd
 */
@Configuration
public class QueryConfig {

    public static final String SOLR_ONTOLOGY_QUERY_REQUEST_HANDLER = "/search";

    @Bean
    public QueryRequestConverter<SolrQuery> ontologySolrQueryRequestConverter() {
        return new SolrQueryConverter(SOLR_ONTOLOGY_QUERY_REQUEST_HANDLER);
    }
}