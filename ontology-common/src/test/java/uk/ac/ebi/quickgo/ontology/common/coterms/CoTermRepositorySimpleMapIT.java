package uk.ac.ebi.quickgo.ontology.common.coterms;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author Tony Wardell
 * Date: 11/10/2016
 * Time: 15:57
 * Created with IntelliJ IDEA.
 */
public class CoTermRepositorySimpleMapIT {

    private CoTermRepositorySimpleMap coTermRepository;
    private static final String GO_TERM_ID_ALL_ONLY = "GO:7777771";
    private static final String GO_TERM_ID_MANUAL_ONLY = "GO:8888881";

    @Before
    public void setup(){
        Resource manualResource = new ClassPathResource("CoTermsManual");
        Resource allResource = new ClassPathResource("CoTermsAll");
        coTermRepository = new CoTermRepositorySimpleMap();
        CoTermRepositorySimpleMap.CoTermLoader coTermLoader = coTermRepository.new CoTermLoader(manualResource, allResource);
        coTermLoader.load();
    }


    @Test
    public void retrievalIsSuccessfulFromAll(){
        List<CoTerm> coTerms = coTermRepository.findCoTerms(GO_TERM_ID_ALL_ONLY, CoTermSource.ALL, 1, t -> true);
        assertThat(coTerms.get(0).getId(), is(GO_TERM_ID_ALL_ONLY));
        assertThat(coTerms.get(0).getCompare(), is("GO:0003333"));
        assertThat(coTerms.get(0).getProbabilityRatio(), is(486.4f));
        assertThat(coTerms.get(0).getSignificance(), is(22.28f));
        assertThat(coTerms.get(0).getTogether(), is(8632L));
        assertThat(coTerms.get(0).getCompared(), is(5778L));
    }

    @Test
    public void retrievalIsSuccessfulFromManual(){
        List<CoTerm> coTerms = coTermRepository.findCoTerms(GO_TERM_ID_MANUAL_ONLY, CoTermSource.MANUAL, 1, t -> true);
        assertThat(coTerms.get(0).getId(), is(GO_TERM_ID_MANUAL_ONLY));
        assertThat(coTerms.get(0).getCompare(), is("GO:0004444"));
        assertThat(coTerms.get(0).getProbabilityRatio(), is(302.4f));
        assertThat(coTerms.get(0).getSignificance(), is(78.28f));
        assertThat(coTerms.get(0).getTogether(), is(1933L));
        assertThat(coTerms.get(0).getCompared(), is(5219L));
    }
}
