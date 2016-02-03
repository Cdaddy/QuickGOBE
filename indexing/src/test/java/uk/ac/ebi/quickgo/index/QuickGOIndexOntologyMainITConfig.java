package uk.ac.ebi.quickgo.index;

import uk.ac.ebi.quickgo.index.reader.ODocReader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * Created 12/01/16
 * @author Edd
 */
@Profile("QuickGOIndexOntologyMainIT")
@Configuration
public class QuickGOIndexOntologyMainITConfig {
    @Bean
    @Primary
    public ODocReader reader() {
        return mock(ODocReader.class);
    }
}