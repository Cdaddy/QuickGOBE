package uk.ac.ebi.quickgo.model.ontology.converter.field;

import uk.ac.ebi.quickgo.model.ontology.OBOTerm;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.ac.ebi.quickgo.document.FlatFieldBuilder.newFlatField;
import static uk.ac.ebi.quickgo.document.FlatFieldLeaf.newFlatFieldLeaf;

/**
 * Created 01/12/15
 * @author Edd
 */
public class AnnotationGuideLineFieldConverterTest {

    private AnnotationGuideLineFieldConverter converter;

    @Before
    public void setup() {
        this.converter = new AnnotationGuideLineFieldConverter();
    }

    @Test
    public void convertsAnnotationGuideLines() {
        List<String> rawAnnotationGuideLines = new ArrayList<>();
        String description0 = "description 0";
        rawAnnotationGuideLines.add(newFlatField()
                .addField(newFlatFieldLeaf(description0))
                .addField(newFlatFieldLeaf("http://www.guardian.co.uk"))
                .buildString()
        );
        String url1 = "http://www.pinkun.com";
        rawAnnotationGuideLines.add(newFlatField()
                .addField(newFlatFieldLeaf("description 1"))
                .addField(newFlatFieldLeaf(url1))
                .buildString()
        );

        List<OBOTerm.AnnotationGuideLine> annotationGuideLines = converter.convertField(rawAnnotationGuideLines);
        assertThat(annotationGuideLines.size(), is(2));
        assertThat(annotationGuideLines.get(0).description, is(description0));
        assertThat(annotationGuideLines.get(1).url, is(url1));
    }
}