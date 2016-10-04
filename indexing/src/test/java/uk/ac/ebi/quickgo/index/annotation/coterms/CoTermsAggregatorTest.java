package uk.ac.ebi.quickgo.index.annotation.coterms;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.quickgo.annotation.common.document.AnnotationDocMocker;
import uk.ac.ebi.quickgo.annotation.common.document.AnnotationDocument;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

/**
 * @author Tony Wardell
 * Date: 26/11/2015
 * Time: 16:26
 * Created with IntelliJ IDEA.
 */
public class CoTermsAggregatorTest {

    CoTermsAggregator aggregator;

    @Before
    public void setup(){
        aggregator = new CoTermsAggregator(t -> true);
    }

	@Test
    public void calculateStatisticsForTwoRecordsWithTheSameGoTerm() throws Exception {

        AnnotationDocument annotation1 = AnnotationDocMocker.createAnnotationDoc("A0A000");
        AnnotationDocument annotation2 = AnnotationDocMocker.createAnnotationDoc("A0A000");
        List<AnnotationDocument> docs = Arrays.asList(annotation1, annotation2);
        aggregator.write(docs);
        aggregator.finish();

        //Now test
        Map<String, Map<String, AtomicLong>> matrix = aggregator.getCoTerms();

        assertThat(matrix.keySet(), hasSize(1));

        Map<String, AtomicLong> coTerms = matrix.get("GO:0003824");
        assertThat(coTerms, is(notNullValue()));
        assertThat(coTerms.keySet(), hasSize(1));

        //Is the only one
        AtomicLong ac = coTerms.get("GO:0003824");
        assertThat(ac.get(), is(1l));

        assertThat(aggregator.getTotalOfAnnotatedGeneProducts(), is(1l));
        assertThat(aggregator.getGeneProductCounts().keySet(), hasSize(1));
        assertThat(aggregator.getGeneProductCounts().get(annotation1.goId).get(), is(1L));
        assertThat(aggregator.getGeneProductCounts().get(annotation2.goId).get(), is(1L));

	}

    @Test
    public void calculateStatisticsForTwoRecordsWithDifferentGoTermsAndDifferentGeneProductSoNoCoTerms() throws
                                                                                                       Exception {

        AnnotationDocument annotation1 = AnnotationDocMocker.createAnnotationDoc("A0A000");
        AnnotationDocument annotation2 = AnnotationDocMocker.createAnnotationDoc("A0A001");
        annotation2.goId = "GO:0009999";
        List<AnnotationDocument> docs = Arrays.asList(annotation1, annotation2);
        aggregator.write(docs);
        aggregator.finish();

        //Now test
        Map<String, Map<String, AtomicLong>> matrix = aggregator.getCoTerms();

        assertThat(matrix.keySet(), hasSize(2));

        Map<String, AtomicLong> coTerms1 = matrix.get(annotation1.goId);
        assertThat(coTerms1.keySet(), hasSize(1));//2
        AtomicLong ac1 = coTerms1.get(annotation1.goId);
        assertThat(ac1.get(), is(1l));

        Map<String, AtomicLong> coTerms2 = matrix.get(annotation2.goId);
        assertThat(coTerms2.keySet(), hasSize(1));
        AtomicLong ac2 = coTerms2.get(annotation2.goId);
        assertThat(ac2.get(), is(1l));

        assertThat(aggregator.getTotalOfAnnotatedGeneProducts(), is(2l));
        assertThat(aggregator.getGeneProductCounts().keySet(), hasSize(2));
        assertThat(aggregator.getGeneProductCounts().get(annotation1.goId).get(), is(1L));
        assertThat(aggregator.getGeneProductCounts().get(annotation2.goId).get(), is(1L));
    }


    @Test
    public void calculateStatisticsForTwoRecordsWithTheDifferentGoTermsSameGeneProduct() throws Exception {

        AnnotationDocument annotation1 = AnnotationDocMocker.createAnnotationDoc("A0A000");
        AnnotationDocument annotation2 = AnnotationDocMocker.createAnnotationDoc("A0A000");
        annotation2.goId = "GO:0009999";
        List<AnnotationDocument> docs = Arrays.asList(annotation1, annotation2);
        aggregator.write(docs);
        aggregator.finish();

        //Now test
        Map<String, Map<String, AtomicLong>> matrix = aggregator.getCoTerms();

        assertThat(matrix.keySet(), hasSize(2));

        Map<String, AtomicLong> coTerms1 = matrix.get(annotation1.goId);
        assertThat(coTerms1.keySet(), hasSize(2));
        AtomicLong ac1x1 = coTerms1.get(annotation1.goId);
        assertThat(ac1x1.get(), is(1l));
        AtomicLong ac1x2 = coTerms1.get(annotation2.goId);
        assertThat(ac1x1.get(), is(1l));

        Map<String, AtomicLong> coTerms2 = matrix.get(annotation2.goId);
        assertThat(coTerms2.keySet(), hasSize(2));
        AtomicLong ac2x1 = coTerms2.get(annotation2.goId);
        assertThat(ac2x1.get(), is(1l));
        AtomicLong ac2x2 = coTerms2.get(annotation1.goId);
        assertThat(ac2x2.get(), is(1l));

        assertThat(aggregator.getTotalOfAnnotatedGeneProducts(), is(1l));
        assertThat(aggregator.getGeneProductCounts().keySet(), hasSize(2));
        assertThat(aggregator.getGeneProductCounts().get(annotation1.goId).get(), is(1L));
        assertThat(aggregator.getGeneProductCounts().get(annotation2.goId).get(), is(1L));
    }

    @Test
    public void zeroAnnotationsProcessedIfPredicateNotTrue() throws Exception {

        AnnotationDocument annotation1 = AnnotationDocMocker.createAnnotationDoc("A0A000");
        AnnotationDocument annotation2 = AnnotationDocMocker.createAnnotationDoc("A0A000");
        CoTermsAggregator aggregatorFalse = new CoTermsAggregator(t -> false);
        List<AnnotationDocument> docs = Arrays.asList(annotation1, annotation2);
        aggregator.write(docs);
        aggregator.finish();

        //Now test
        Map<String, Map<String, AtomicLong>> matrix = aggregatorFalse.getCoTerms();

        assertThat(matrix.keySet(), hasSize(0));

        assertThat(aggregatorFalse.getTotalOfAnnotatedGeneProducts(), is(0l));
        assertThat(aggregatorFalse.getGeneProductCounts().keySet(), hasSize(0));

    }

    @Test(expected=IllegalArgumentException.class)
    public void exceptionThrownIfNullAnnotationPassedToAddRowToMatrix() throws Exception {
        aggregator.write(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void exceptionThrownIfNullPredicatePassedToConstructor() {
        new CoTermsAggregator(null);
    }

}
