package uk.ac.ebi.quickgo.annotation.common;

import uk.ac.ebi.quickgo.common.store.TemporarySolrDataStore;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.ac.ebi.quickgo.annotation.common.document.AnnotationDocMocker.createAnnotationDoc;

/**
 * Test that the annotation repository can be accessed as expected.
 *
 * Created 14/04/16
 * @author Edd
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AnnotationRepoConfig.class, loader = SpringApplicationContextLoader.class)
public class AnnotationRepositoryIT {

    // temporary data store for solr's data, which is automatically cleaned on exit
    @ClassRule
    public static final TemporarySolrDataStore solrDataStore = new TemporarySolrDataStore();

    @Autowired
    private AnnotationRepository annotationRepository;

    @Before
    public void before() {
        annotationRepository.deleteAll();
    }

    @Test
    public void add1DocAndFind1Doc() {
        AnnotationDocument doc1 = createAnnotationDoc("A0A000");
        annotationRepository.save(doc1);

        assertThat(annotationRepository.findAll(new PageRequest(0, 10)).getTotalElements(), is(1L));
    }

    @Test
    public void add2DocsAndFind2Docs() {
        AnnotationDocument doc1 = createAnnotationDoc("A0A000");
        AnnotationDocument doc2 = createAnnotationDoc("A0A001");

        annotationRepository.save(doc1);
        annotationRepository.save(doc2);

        assertThat(annotationRepository.findAll(new PageRequest(0, 10)).getTotalElements(), is(2L));
    }

    @Test
    public void add3DocsRemove1AndFind2Docs() {
        AnnotationDocument doc1 = createAnnotationDoc("A0A000");
        AnnotationDocument doc2 = createAnnotationDoc("A0A001");
        AnnotationDocument doc3 = createAnnotationDoc("A0A002");

        annotationRepository.save(doc1);
        annotationRepository.save(doc2);
        annotationRepository.save(doc3);
        annotationRepository.delete(doc3);

        assertThat(annotationRepository.findAll(new PageRequest(0, 10)).getTotalElements(), is(2L));
    }
}