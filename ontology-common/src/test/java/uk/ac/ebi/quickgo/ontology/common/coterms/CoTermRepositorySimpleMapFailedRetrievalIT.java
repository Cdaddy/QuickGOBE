package uk.ac.ebi.quickgo.ontology.common.coterms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static uk.ac.ebi.quickgo.ontology.common.coterms.CoTermRepoTestConfig.FAILED_RETRIEVAL;

/**
 * @author Tony Wardell
 * Date: 11/10/2016
 * Time: 15:57
 * Created with IntelliJ IDEA.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CoTermRepoTestConfig.class}, loader = SpringApplicationContextLoader.class)
@ActiveProfiles(profiles = FAILED_RETRIEVAL)
public class CoTermRepositorySimpleMapFailedRetrievalIT {

    private static final String GO_TERM = "GO:7777771";

    @Autowired
    private CoTermRepository coTermRepository;

    @Test(expected = IllegalStateException.class)
    public void cannotLoadAllFromCoTermRepositoryAsFileIsEmpty() {
        coTermRepository.findCoTerms(GO_TERM, CoTermSource.ALL);

    }

    @Test(expected = IllegalStateException.class)
    public void cannotLoadManualFromCoTermRepositoryAsFileIsEmpty() {
        coTermRepository.findCoTerms(GO_TERM, CoTermSource.MANUAL);

    }
}
