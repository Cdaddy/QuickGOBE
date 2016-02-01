package uk.ac.ebi.quickgo.ontology.common.config;

import uk.ac.ebi.quickgo.ontology.common.OntologyRepository;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.core.CoreContainer;
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
 * Publishes the configuration beans of the ontology repository.
 */
@Configuration
public class RepoConfig {
    @Bean
    static PropertySourcesPlaceholderConfigurer propertyPlaceHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

//    @Bean
//    @Profile("httpServer")
//    public SolrServer httpSolrServer(@Value("${solr.host}") String solrUrl)  {
//        return new HttpSolrServer(solrUrl);
//    }

    @Bean
    @Profile("embeddedServer")
    public SolrServer embeddedSolrServer(SolrServerFactory solrServerFactory) {
        return solrServerFactory.getSolrServer();
    }

    @Bean
    @Profile("embeddedServer")
    public SolrServerFactory solrServerFactory(CoreContainer coreContainer)
            throws IOException, SAXException, ParserConfigurationException {
        EmbeddedSolrServer embeddedSolrServer = new EmbeddedSolrServer(coreContainer, null);
        return new MulticoreSolrServerFactory(embeddedSolrServer);
    }

    @Bean
    @Profile("embeddedServer")
    public CoreContainer coreContainer(@Value("${solr.solr.home}") String solrHome) {
        CoreContainer container = new CoreContainer(new File(solrHome).getAbsolutePath());
        container.load();
        return container;
    }

    @Bean
    public SolrTemplate ontologyTemplate(SolrServer server)  {
        return new SolrTemplate(server, "ontology");
    }

    @Bean
    public OntologyRepository ontologyRepository(SolrTemplate ontologyTemplate) {
        return new SolrRepositoryFactory(ontologyTemplate)
                .getRepository(OntologyRepository.class);
    }
}