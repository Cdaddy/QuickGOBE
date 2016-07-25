package uk.ac.ebi.quickgo.ontology.service;

import uk.ac.ebi.quickgo.ontology.common.OntologyRepoConfig;
import uk.ac.ebi.quickgo.ontology.common.OntologyRepository;
import uk.ac.ebi.quickgo.ontology.common.document.OntologyType;
import uk.ac.ebi.quickgo.ontology.model.ECOTerm;
import uk.ac.ebi.quickgo.ontology.model.GOTerm;
import uk.ac.ebi.quickgo.ontology.service.converter.ECODocConverter;
import uk.ac.ebi.quickgo.ontology.service.converter.GODocConverter;
import uk.ac.ebi.quickgo.ontology.service.search.SearchServiceConfig;
import uk.ac.ebi.quickgo.ontology.traversal.OntologyGraphTraversal;
import uk.ac.ebi.quickgo.ontology.traversal.read.OntologyGraphConfig;
import uk.ac.ebi.quickgo.rest.search.QueryStringSanitizer;
import uk.ac.ebi.quickgo.rest.search.SolrQueryStringSanitizer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring configuration for the service layer, which depends on the repositories
 * made available by {@link OntologyRepoConfig} and {@link SearchServiceConfig}. Services
 * to additionally make accessible are defined in specified the {@link ComponentScan} packages.
 *
 * Created 19/11/15
 * @author Edd
 */
@Configuration
@ComponentScan({"uk.ac.ebi.quickgo.ontology.service"})
@Import({OntologyRepoConfig.class, OntologyGraphConfig.class})
public class ServiceConfig {
    @Bean
    public OntologyService<GOTerm> goOntologyService(OntologyRepository ontologyRepository, OntologyGraphTraversal ontologyGraphTraversal) {
        return new OntologyServiceImpl<>(
                ontologyRepository,
                goDocumentConverter(),
                OntologyType.GO,
                queryStringSanitizer(),
                ontologyGraphTraversal);
    }

    private GODocConverter goDocumentConverter() {
        return new GODocConverter();
    }

    @Bean
    public OntologyService<ECOTerm> ecoOntologyService(OntologyRepository ontologyRepository, OntologyGraphTraversal ontologyGraphTraversal) {
        return new OntologyServiceImpl<>(
                ontologyRepository,
                ecoDocConverter(),
                OntologyType.ECO,
                queryStringSanitizer(),
                ontologyGraphTraversal);
    }

    private ECODocConverter ecoDocConverter() {
        return new ECODocConverter();
    }

    private QueryStringSanitizer queryStringSanitizer() {
        return new SolrQueryStringSanitizer();
    }
}
