package uk.ac.ebi.quickgo.index.geneproduct;

import uk.ac.ebi.quickgo.common.store.TemporarySolrDataStore;
import uk.ac.ebi.quickgo.geneproduct.common.GeneProductDocument;
import uk.ac.ebi.quickgo.index.DocumentWriteRetryHelper;
import uk.ac.ebi.quickgo.index.common.JobTestRunnerConfig;

import java.util.List;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;
import static uk.ac.ebi.quickgo.index.DocumentWriteRetryHelper.stubSolrWriteResponses;
import static uk.ac.ebi.quickgo.index.DocumentWriteRetryHelper.validateWriteAttempts;

/**
 * Tests whether Spring Batch is correctly wired up to run the Gene product indexing.
 */
@ActiveProfiles(profiles = {"embeddedServer", "twoSolrRemoteHostErrors"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {GeneProductConfig.class, JobTestRunnerConfig.class, GeneProductIndexingRetrySucceedsBatchIT
                .RetryConfig.class},
        loader = SpringApplicationContextLoader.class)
public class GeneProductIndexingRetrySucceedsBatchIT {
    @ClassRule
    public static final TemporarySolrDataStore solrDataStore = new TemporarySolrDataStore();

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private ItemWriter<GeneProductDocument> geneProductRepositoryWriter;

    @Captor
    private ArgumentCaptor<List<GeneProductDocument>> argumentCaptor;

    private static final List<DocumentWriteRetryHelper.SolrResponse> SOLR_RESPONSES = asList(
            DocumentWriteRetryHelper.SolrResponse.OK,// error
            DocumentWriteRetryHelper.SolrResponse.REMOTE_EXCEPTION,// error
            DocumentWriteRetryHelper.SolrResponse.OK,// too many errors -- indexing fails
            DocumentWriteRetryHelper.SolrResponse.REMOTE_EXCEPTION,
            DocumentWriteRetryHelper.SolrResponse.OK,
            DocumentWriteRetryHelper.SolrResponse.OK,
            DocumentWriteRetryHelper.SolrResponse.REMOTE_EXCEPTION // never called
            );

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void successfulJobRun() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        StepExecution indexingStep = getStepByName(GeneProductConfig.GENE_PRODUCT_INDEXING_STEP_NAME, jobExecution);
        assertThat(indexingStep.getReadCount(), is(7));
        assertThat(indexingStep.getReadSkipCount(), is(1));
        assertThat(indexingStep.getProcessSkipCount(), is(1));
        assertThat(indexingStep.getWriteCount(), is(6));

        verify(geneProductRepositoryWriter, times(6)).write(argumentCaptor.capture());
        List<List<GeneProductDocument>> docsSentToBeWritten = argumentCaptor.getAllValues();
        validateWriteAttempts(SOLR_RESPONSES, docsSentToBeWritten, d -> d.id);

        BatchStatus status = jobExecution.getStatus();
        assertThat(status, is(BatchStatus.COMPLETED));
    }

    private StepExecution getStepByName(String stepName, JobExecution jobExecution) {
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            if (stepExecution.getStepName().equals(stepName)) {
                return stepExecution;
            }
        }

        throw new IllegalArgumentException("Step name not recognized: " + stepName);
    }

    @Profile("twoSolrRemoteHostErrors")
    public static class RetryConfig {

        private static final String HOST = "http://www.myhost.com";
        private static final String MESSAGE = "Looks like the host is not reachable?!";
        private static final int CODE = 1;

        @Bean
        @Primary
        @SuppressWarnings(value = "unchecked")
        ItemWriter<GeneProductDocument> geneProductRepositoryWriter() throws Exception {
            ItemWriter<GeneProductDocument> mockItemWriter = mock(ItemWriter.class);

            stubSolrWriteResponses(SOLR_RESPONSES)
                    .when(mockItemWriter).write(any());

            return mockItemWriter;
        }
    }
}
