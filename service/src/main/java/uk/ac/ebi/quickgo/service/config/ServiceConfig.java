package uk.ac.ebi.quickgo.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.ac.ebi.quickgo.repo.solr.config.RepoConfig;
import uk.ac.ebi.quickgo.repo.solr.document.ontology.OntologyType;
import uk.ac.ebi.quickgo.repo.solr.io.ontology.OntologyRepository;
import uk.ac.ebi.quickgo.service.OntologyService;
import uk.ac.ebi.quickgo.service.OntologyServiceImpl;
import uk.ac.ebi.quickgo.service.converter.ontology.ECODocConverter;
import uk.ac.ebi.quickgo.service.converter.ontology.GODocConverter;
import uk.ac.ebi.quickgo.service.model.ontology.ECOTerm;
import uk.ac.ebi.quickgo.service.model.ontology.GOTerm;

/**
 * Spring configuration for the service layer, which depends on the repositories
 * made available by {@link RepoConfig}. Services to additionally make accessible
 * are defined in specified the {@link ComponentScan} packages.
 *
 * Created 19/11/15
 * @author Edd
 */
@Configuration
@Import({RepoConfig.class})
@ComponentScan({"uk.ac.ebi.quickgo.service"})
public class ServiceConfig {
    @Bean
    public OntologyService<GOTerm> goOntologyService(OntologyRepository ontologyRepository) {
        return new OntologyServiceImpl<>(ontologyRepository, goDocumentConverter(), OntologyType.GO);
    }

    private GODocConverter goDocumentConverter() {
        return new GODocConverter();
    }

    @Bean
    public OntologyService<ECOTerm> ecoOntologyService(OntologyRepository ontologyRepository) {
        return new OntologyServiceImpl<>(ontologyRepository, ecoDocConverter(), OntologyType.ECO);
    }

    private ECODocConverter ecoDocConverter() {
        return new ECODocConverter();
    }
}
