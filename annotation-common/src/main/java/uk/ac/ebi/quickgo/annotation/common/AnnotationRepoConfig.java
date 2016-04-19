package uk.ac.ebi.quickgo.annotation.common;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.support.SolrRepositoryFactory;
import org.springframework.data.solr.server.SolrServerFactory;
import org.springframework.data.solr.server.support.MulticoreSolrServerFactory;
import org.xml.sax.SAXException;

/**
 * Publishes the configuration beans of the annotation repository.
 *
 * Created 14/04/16
 * @author Edd
 */
@Configuration
public class AnnotationRepoConfig {

    private static final String SOLR_CORE = "annotation";

    @Bean
    static PropertySourcesPlaceholderConfigurer propertyPlaceHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public SolrServer solrServer(SolrServerFactory solrServerFactory) {
        return solrServerFactory.getSolrServer();
    }

    @Bean
    @Profile("httpServer")
    public SolrServerFactory httpSolrServerFactory(@Value("${solr.host}") String solrUrl) {
        return new MulticoreSolrServerFactory(new HttpSolrServer(solrUrl));
    }

    @Bean
    @Profile("embeddedServer")
    public SolrServerFactory embeddedSolrServerFactory(CoreContainer coreContainer)
            throws IOException, SAXException, ParserConfigurationException {
        return new MulticoreSolrServerFactory(new EmbeddedSolrServer(coreContainer, SOLR_CORE));
    }

    @Bean
    @Profile("embeddedServer")
    public CoreContainer coreContainer(@Value("${solr.solr.home}") String solrHome) {
        CoreContainer container = new CoreContainer(new File(solrHome).getAbsolutePath());
        container.load();
        return container;
    }

    @Bean
    public SolrTemplate annotationTemplate(SolrServerFactory solrServerFactory) {
        SolrTemplate template = new SolrTemplate(solrServerFactory);
        template.setSolrCore(SOLR_CORE);

        return template;
    }

    @Bean
    public AnnotationRepository annotationRepository(
            @Qualifier("annotationTemplate") SolrTemplate annotationTemplate) {
        return new SolrRepositoryFactory(annotationTemplate).getRepository(AnnotationRepository.class);
    }
}
