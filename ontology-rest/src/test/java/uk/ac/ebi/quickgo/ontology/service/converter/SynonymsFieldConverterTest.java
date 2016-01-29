package uk.ac.ebi.quickgo.ontology.service.converter;

import uk.ac.ebi.quickgo.common.converter.FlatFieldBuilder;
import uk.ac.ebi.quickgo.common.converter.FlatFieldLeaf;
import uk.ac.ebi.quickgo.ontology.model.OBOTerm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Created 01/12/15
 * @author Edd
 */
public class SynonymsFieldConverterTest {

    private SynonymsFieldConverter converter;

    @Before
    public void setup() {
        this.converter = new SynonymsFieldConverter();
    }

    /**
     * Check the flatted document synonyms are converted correctly to a
     * {@link OBOTerm.Synonym}
     * DTO.
     */
    @Test
    public void convertingBadlyFlattenedSynonymsFailsWithoutError() {
        List<String> rawSynonyms = Arrays.asList("syn name 0-syn type 0", "syn name 1-syn type 1");
        List<OBOTerm.Synonym> synonyms = converter.convertFieldList(rawSynonyms);
        assertThat(synonyms.size(), is(0));
    }

    /**
     * Check the flatted document synonyms are converted correctly to a
     * {@link OBOTerm.Synonym}
     * DTO.
     */
    @Test
    public void converts0FlattenedSynonymsToSynonymsDTO() {
        List<String> rawSynonyms = Collections.emptyList();
        List<OBOTerm.Synonym> synonyms = converter.convertFieldList(rawSynonyms);
        assertThat(synonyms.size(), is(0));
    }

    /**
     * Check the flatted document synonyms are converted correctly to a
     * {@link OBOTerm.Synonym}
     * DTO.
     */
    @Test
    public void converts1FlattenedSynonymToSynonymsDTO() {
        List<String> rawSynonyms = Collections.singletonList(
                FlatFieldBuilder.newFlatField()
                .addField(FlatFieldLeaf.newFlatFieldLeaf("syn name 0"))
                .addField(FlatFieldLeaf.newFlatFieldLeaf("syn type 0"))
                .buildString());
        List<OBOTerm.Synonym> synonyms = converter.convertFieldList(rawSynonyms);
        assertThat(synonyms.size(), is(1));
        assertThat(synonyms.get(0).synonymName, is(equalTo("syn name 0")));
        assertThat(synonyms.get(0).synonymType, is(equalTo("syn type 0")));
    }

    /**
     * Check the flatted document synonyms are converted correctly to a
     * {@link OBOTerm.Synonym}
     * DTO.
     */
    @Test
    public void converts2FlattenedSynonymsToSynonymsDTO() {
        List<String> rawSynonyms = Arrays.asList(
                FlatFieldBuilder.newFlatField()
                        .addField(FlatFieldLeaf.newFlatFieldLeaf("syn name 0"))
                        .addField(FlatFieldLeaf.newFlatFieldLeaf("syn type 0"))
                        .buildString(),
                FlatFieldBuilder.newFlatField()
                        .addField(FlatFieldLeaf.newFlatFieldLeaf("syn name 1"))
                        .addField(FlatFieldLeaf.newFlatFieldLeaf("syn type 1"))
                        .buildString()
        );
        List<OBOTerm.Synonym> synonyms = converter.convertFieldList(rawSynonyms);
        assertThat(synonyms.size(), is(2));
        assertThat(synonyms.get(0).synonymName, is(equalTo("syn name 0")));
        assertThat(synonyms.get(0).synonymType, is(equalTo("syn type 0")));

        assertThat(synonyms.get(1).synonymName, is(equalTo("syn name 1")));
        assertThat(synonyms.get(1).synonymType, is(equalTo("syn type 1")));
    }

    @Test
    public void gracefullyHandleWrongFieldCount() {
        Optional<OBOTerm.Synonym> result = converter.apply(
                FlatFieldBuilder.newFlatField().addField(FlatFieldLeaf.newFlatFieldLeaf("wrong " +
                "format"))
                .buildString());
        assertThat(result.isPresent(), is(false));
    }
}