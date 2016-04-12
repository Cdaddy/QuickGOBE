package uk.ac.ebi.quickgo.geneproduct.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.ac.ebi.quickgo.geneproduct.common.GeneProductRepository;
import uk.ac.ebi.quickgo.geneproduct.common.RepoConfig;
import uk.ac.ebi.quickgo.geneproduct.service.converter.GeneProductDocConverter;
import uk.ac.ebi.quickgo.geneproduct.service.converter.GeneProductDocConverterImpl;
import uk.ac.ebi.quickgo.rest.search.ControllerHelper;
import uk.ac.ebi.quickgo.rest.search.ControllerHelperImpl;
import uk.ac.ebi.quickgo.rest.search.QueryStringSanitizer;
import uk.ac.ebi.quickgo.rest.search.SolrQueryStringSanitizer;
import uk.ac.ebi.quickgo.rest.service.ServiceHelper;
import uk.ac.ebi.quickgo.rest.service.ServiceHelperImpl;

/**
 *
 * Spring configuration for the service layer, which depends on the repositories
 * made available by {@link RepoConfig} and {@link SearchServiceConfig}. Services
 * to additionally make accessible are defined in specified the {@link ComponentScan} packages.
 *
 * @author Tony Wardell
 * Date: 04/04/2016
 * Time: 11:42
 * Created with IntelliJ IDEA.
 */
@Configuration
@ComponentScan({"uk.ac.ebi.quickgo.geneproduct.service"})
@Import({RepoConfig.class, SearchServiceConfig.class})
public class ServiceConfig {

	@Bean
	public GeneProductService goGeneProductService(GeneProductRepository geneProductRepository) {
		return new GeneProductServiceImpl(
				serviceHelper(),
				geneProductRepository,
				geneProductDocConverter());
	}

	private ServiceHelper serviceHelper(){
		return new ServiceHelperImpl(queryStringSanitizer());
	}

	private GeneProductDocConverter geneProductDocConverter() {
		return new GeneProductDocConverterImpl();
	}

	private QueryStringSanitizer queryStringSanitizer() {
		return new SolrQueryStringSanitizer();
	}

	@Bean
	public ControllerHelper controllerHelper(){
		return new ControllerHelperImpl();
	}
}
