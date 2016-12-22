package uk.ac.ebi.quickgo.ontology.common.coterms;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;

import static uk.ac.ebi.quickgo.ontology.common.coterms.CoTermRepositorySimpleMap.*;

/**
 * Configuration class related to loading and using co-occurring terms information.
 * @author Tony Wardell
 * Date: 29/09/2016
 * Time: 11:50
 * Created with IntelliJ IDEA.
 */
@Configuration
public class CoTermRepoTestConfig {
    static final String FAILED_RETRIEVAL = "failedRetrieval";
    static final String SUCCESSFUL_RETRIEVAL = "successfulRetrieval";

    @Value("${coterm.source.manual}")
    private Resource manualResource;

    @Value("${coterm.source.all}")
    private Resource allResource;

    @Value("${coterm.source.header.lines:1}")
    private int headerLines;

    @Bean
    static PropertySourcesPlaceholderConfigurer propertyPlaceHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    @Profile(SUCCESSFUL_RETRIEVAL)
    public CoTermRepository coTermRepository() throws IOException {
        return createCoTermRepositorySimpleMap(manualResource, allResource, headerLines);
    }

    @Bean
    @Profile(FAILED_RETRIEVAL)
    public CoTermRepository failedCoTermLoading() throws IOException {
        return createEmptyRepository();
    }
}
