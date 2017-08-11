package uk.ac.ebi.quickgo.annotation.download.header;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.quickgo.annotation.download.TSVDownload.*;
import static uk.ac.ebi.quickgo.annotation.download.header.TSVHeaderCreator.*;

/**
 * @author Tony Wardell
 * Date: 23/05/2017
 * Time: 12:10
 * Created with IntelliJ IDEA.
 */
public class TSVHeaderCreatorTest {

    private static final String REQUEST_URI =
            "/QuickGO/services/annotation/downloadSearch?downloadLimit=7&geneProductId" +
                    "=UniProtKB:A0A000&includeFields=goName,taxonName";
    private static final List<String[]> fields2Columns = new ArrayList<>();
    public static final String FULL_HEADER_STRING =
            GENE_PRODUCT_ID + "\t" + SYMBOL + "\t" + QUALIFIER + "\t" + GO_TERM + "\t" + GO_ASPECT + "\t" +
                    GO_NAME + "\t" + ECO_ID + "\t" + GO_EVIDENCE_CODE + "\t" + REFERENCE + "\t" + WITH_FROM + "\t" +
                    TAXON_ID + "\t" + ASSIGNED_BY + "\t" + ANNOTATION_EXTENSION + "\t" + DATE + "\t" + TAXON_NAME +
                    "\t" + GENE_PRODUCT_NAME + "\t" + GENE_PRODUCT_SYNONYMS + "\t" + GENE_PRODUCT_TYPE + "\n";

    static {
        initialiseFieldColumns();
    }

    private final OntologyHeaderInfo mockOntology = mock(OntologyHeaderInfo.class);
    private final ResponseBodyEmitter mockEmitter = mock(ResponseBodyEmitter.class);
    private final HeaderContent mockContent = mock(HeaderContent.class);
    private TSVHeaderCreator tsvHeaderCreator;

    @Before
    public void setup() {
        String FORMAT_VERSION_1 = "test-version_1";
        String FORMAT_VERSION_2 = "test-version_2";
        tsvHeaderCreator = new TSVHeaderCreator();
        when(mockOntology.versions()).thenReturn(asList(FORMAT_VERSION_1, FORMAT_VERSION_2));
        when(mockContent.getUri()).thenReturn(REQUEST_URI);
        when(mockContent.getSelectedFields()).thenReturn(emptyList());
        when(mockContent.isSlimmed()).thenReturn(false);
    }

    @Test
    public void writeColumnNameForIndividualField() throws Exception {
        for (String[] field2Column : fields2Columns) {
            HeaderContent content = mock(HeaderContent.class);
            when(content.isSlimmed()).thenReturn(false);
            when(content.getSelectedFields()).thenReturn(singletonList(field2Column[0]));

            tsvHeaderCreator.write(mockEmitter, content);

            verify(mockEmitter).send(field2Column[1] + "\n", MediaType.TEXT_PLAIN);
        }
    }

    @Test
    public void writeHeaderForSeveralSelectedFields() throws Exception {
        when(mockContent.isSlimmed()).thenReturn(false);
        when(mockContent.getSelectedFields())
                .thenReturn(asList(GENE_PRODUCT_ID_FIELD_NAME, GO_NAME_FIELD_NAME, TAXON_NAME_FIELD_NAME));

        tsvHeaderCreator.write(mockEmitter, mockContent);

        verify(mockEmitter).send(TSVHeaderCreator.GENE_PRODUCT_ID + "\t"
                + TSVHeaderCreator.GO_NAME + "\t"
                + TSVHeaderCreator.TAXON_NAME + "\n", MediaType.TEXT_PLAIN);
    }

    @Test
    public void writeHeaderForSeveralSelectedFieldsInNewOrder() throws Exception {
        when(mockContent.isSlimmed()).thenReturn(false);
        when(mockContent.getSelectedFields()).thenReturn(asList(TAXON_NAME_FIELD_NAME, GO_NAME_FIELD_NAME,
                GENE_PRODUCT_ID_FIELD_NAME));

        tsvHeaderCreator.write(mockEmitter, mockContent);

        verify(mockEmitter).send(TSVHeaderCreator.TAXON_NAME + "\t"
                + TSVHeaderCreator.GO_NAME + "\t"
                + TSVHeaderCreator.GENE_PRODUCT_ID + "\n", MediaType.TEXT_PLAIN);
    }

    @Test(expected = HeaderCreationException.class)
    public void ioErrorWhenWritingHeaderCausesHeaderCreationException() throws Exception {
        doThrow(HeaderCreationException.class).when(mockEmitter).send(anyObject(), any());
        when(mockContent.getSelectedFields()).thenReturn(singletonList(TAXON_NAME_FIELD_NAME));

        tsvHeaderCreator.write(mockEmitter, mockContent);
    }

    @Test
    public void writeHeaderForFullListOfFieldsNotSlimmed() throws Exception {
        when(mockContent.isSlimmed()).thenReturn(false);
        when(mockContent.getSelectedFields()).thenReturn(Collections.emptyList());

        tsvHeaderCreator.write(mockEmitter, mockContent);

        verify(mockEmitter).send(FULL_HEADER_STRING,
                MediaType.TEXT_PLAIN);
    }

    @Test
    public void writeHeaderForSeveralSelectedFieldsWhenSlimmed() throws Exception {
        when(mockContent.isSlimmed()).thenReturn(true);
        when(mockContent.getSelectedFields()).thenReturn(asList(GENE_PRODUCT_ID_FIELD_NAME,
                GO_TERM_FIELD_NAME,
                TAXON_NAME_FIELD_NAME));

        tsvHeaderCreator.write(mockEmitter, mockContent);

        verify(mockEmitter).send(GENE_PRODUCT_ID + "\t"
                + GO_TERM + "\t"
                + SLIMMED_FROM + "\t"
                + TAXON_NAME + "\n", MediaType.TEXT_PLAIN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionThrownIfEmitterIsNull() {
        tsvHeaderCreator.write(null, mockContent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionThrownIfContentIsNull() {
        tsvHeaderCreator.write(mockEmitter, null);
    }

    @Test(expected = HeaderCreationException.class)
    public void exceptionReceivedIfEmitterThrowsException() throws Exception{
        doThrow(new HeaderCreationException()).when(mockEmitter).send(FULL_HEADER_STRING, MediaType.TEXT_PLAIN);

        tsvHeaderCreator.write(mockEmitter, mockContent);
    }

    private static void initialiseFieldColumns() {
        fields2Columns.add(new String[]{GENE_PRODUCT_ID_FIELD_NAME, GENE_PRODUCT_ID});
        fields2Columns.add(new String[]{SYMBOL_FIELD_NAME, SYMBOL});
        fields2Columns.add(new String[]{QUALIFIER_FIELD_NAME, QUALIFIER});
        fields2Columns.add(new String[]{GO_TERM_FIELD_NAME, GO_TERM});
        fields2Columns.add(new String[]{GO_ASPECT_FIELD_NAME, GO_ASPECT});
        fields2Columns.add(new String[]{GO_NAME_FIELD_NAME, GO_NAME});
        fields2Columns.add(new String[]{ECO_ID_FIELD_NAME, ECO_ID});
        fields2Columns.add(new String[]{GO_EVIDENCE_CODE_FIELD_NAME, GO_EVIDENCE_CODE});
        fields2Columns.add(new String[]{REFERENCE_FIELD_NAME, REFERENCE});
        fields2Columns.add(new String[]{WITH_FROM_FIELD_NAME, WITH_FROM});
        fields2Columns.add(new String[]{TAXON_ID_FIELD_NAME, TAXON_ID});
        fields2Columns.add(new String[]{ASSIGNED_BY_FIELD_NAME, ASSIGNED_BY});
        fields2Columns.add(new String[]{ANNOTATION_EXTENSION_FIELD_NAME, ANNOTATION_EXTENSION});
        fields2Columns.add(new String[]{DATE_FIELD_NAME, DATE});
        fields2Columns.add(new String[]{TAXON_NAME_FIELD_NAME, TAXON_NAME});
        fields2Columns.add(new String[]{GENE_PRODUCT_NAME_FIELD_NAME, GENE_PRODUCT_NAME});
        fields2Columns.add(new String[]{GENE_PRODUCT_SYNONYMS_FIELD_NAME, GENE_PRODUCT_SYNONYMS});
        fields2Columns.add(new String[]{GENE_PRODUCT_TYPE_FIELD_NAME, GENE_PRODUCT_TYPE});
    }
}
