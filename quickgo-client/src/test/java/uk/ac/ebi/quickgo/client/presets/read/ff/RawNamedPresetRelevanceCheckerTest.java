package uk.ac.ebi.quickgo.client.presets.read.ff;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Created 01/09/16
 * @author Edd
 */
public class RawNamedPresetRelevanceCheckerTest {
    private static final String UNIPARC = "UniParc";
    private static final String UNIPROT = "UniProt";
    private final static List<String> UNIPROT_OR_UNIPARC = asList(UNIPROT, UNIPARC);
    private RawNamedPresetRelevanceChecker relevanceChecker;

    @Before
    public void setUp() {
        this.relevanceChecker = new RawNamedPresetRelevanceChecker(UNIPROT_OR_UNIPARC);
    }

    @Test
    public void invalidItemIsFiltered() throws Exception {
        assertThat(relevanceChecker.process(createAssignedBy("invalid")), is(nullValue()));
    }

    @Test
    public void validItemIsNotFiltered() throws Exception {
        assertThat(relevanceChecker.process(createAssignedBy(UNIPROT)), is(not(nullValue())));
        assertThat(relevanceChecker.process(createAssignedBy(UNIPROT)).name, is(UNIPROT));
    }

    private RawNamedPreset createAssignedBy(String name) {
        RawNamedPreset preset = new RawNamedPreset();
        preset.name = name;
        return preset;
    }
}