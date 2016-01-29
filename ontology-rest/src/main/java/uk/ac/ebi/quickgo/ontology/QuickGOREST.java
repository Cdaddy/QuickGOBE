package uk.ac.ebi.quickgo.ontology;



import uk.ac.ebi.quickgo.ontology.common.config.RepoConfig;
import uk.ac.ebi.quickgo.ontology.config.ServiceConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Runnable class to start an embedded Jetty server to host the defined RESTful components.
 *
 * Created 16/11/15
 * @author Edd
 */
@SpringBootApplication
@ComponentScan({"uk.ac.ebi.quickgo.ontology.controller"})
@Import({ServiceConfig.class, RepoConfig.class})
public class QuickGOREST {
    /**
     * Ensures that placeholders are replaced with property values
     */
    @Bean
    static PropertySourcesPlaceholderConfigurer propertyPlaceHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    public static void main(String[] args) {
        SpringApplication.run(QuickGOREST.class, args);
    }
}
