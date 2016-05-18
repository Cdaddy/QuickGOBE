package uk.ac.ebi.quickgo.geneproduct.service.converter;

import uk.ac.ebi.quickgo.geneproduct.common.document.GeneProductDocument;
import uk.ac.ebi.quickgo.geneproduct.common.document.GeneProductType;
import uk.ac.ebi.quickgo.geneproduct.model.GeneProduct;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.ac.ebi.quickgo.geneproduct.service.converter.GeneProductDocConverterImpl.DEFAULT_TAXON_ID;

/**
 * Unit tests the {@link GeneProductDocConverterImpl} class.
 */
public class GeneProductDocConverterImplTest {
    private static final String ID = "A0A000";

    private static final int TAX_ID = 789;

    private static final String DATABASE = "UniProt";
    private static final String SYMBOL = "G12345";
    private static final String TAX_NAME = "Streptomyces ghanaensis";
    private static final String TYPE = "protein";
    private static final String NAME = "moeA5";
    private static final String PARENT_ID = "QWERTY";
    private static final String REF_PROTEOME = "P1234";

    private static final List<String> DATABASE_SUBSETS = Arrays.asList("SUB1", "SUB2");
    private static final List<String> SYNONYMS = Arrays.asList("Q1234", "R1234", "S1234");

    private GeneProductDocConverter geneProductDocConverter;
    private GeneProductDocument geneProductDocument;

    @Before
    public void setup() {
        geneProductDocConverter = new GeneProductDocConverterImpl();

        geneProductDocument = new GeneProductDocument();

        geneProductDocument.id = ID;
        geneProductDocument.database = DATABASE;
        geneProductDocument.databaseSubsets = DATABASE_SUBSETS;
        geneProductDocument.isAnnotated = true;
        geneProductDocument.synonyms = SYNONYMS;
        geneProductDocument.isIsoform = true;
        geneProductDocument.name = NAME;
        geneProductDocument.referenceProteome = REF_PROTEOME;
        geneProductDocument.isCompleteProteome = true;
        geneProductDocument.parentId = PARENT_ID;
        geneProductDocument.symbol = SYMBOL;
        geneProductDocument.taxonId = TAX_ID;
        geneProductDocument.taxonName = TAX_NAME;
        geneProductDocument.type = TYPE;
    }

    @Test
    public void convertOne() {
        GeneProduct convertedGeneProduct = geneProductDocConverter.convert(geneProductDocument);

        assertThat(convertedGeneProduct.id, is(equalTo(ID)));
        assertThat(convertedGeneProduct.database, is(equalTo(DATABASE)));
        assertThat(convertedGeneProduct.databaseSubset, containsInAnyOrder("SUB1", "SUB2"));
        assertThat(convertedGeneProduct.synonyms, containsInAnyOrder("Q1234", "R1234", "S1234"));
        assertThat(convertedGeneProduct.isIsoform, is(true));
        assertThat(convertedGeneProduct.name, is(NAME));
        assertThat(convertedGeneProduct.referenceProteome, is(REF_PROTEOME));
        assertThat(convertedGeneProduct.isCompleteProteome, is(true));
        assertThat(convertedGeneProduct.parentId, is(PARENT_ID));
        assertThat(convertedGeneProduct.symbol, is(SYMBOL));
        assertThat(convertedGeneProduct.taxonomy.id, is(TAX_ID));
        assertThat(convertedGeneProduct.taxonomy.name, is(TAX_NAME));
        assertThat(convertedGeneProduct.type, is(GeneProductType.PROTEIN));
        assertThat(convertedGeneProduct.isAnnotated, is(true));
    }

    @Test
    public void noTaxIdInDocResultsInNullModelTaxId() {
        geneProductDocument.taxonId = DEFAULT_TAXON_ID;
        GeneProduct convertedGeneProduct = geneProductDocConverter.convert(geneProductDocument);

        assertThat(convertedGeneProduct.taxonomy, is(nullValue()));
    }

    @Test
    public void nullDocDbSubsetConvertsToNullModelDbSubset() {
        geneProductDocument.databaseSubsets = null;

        GeneProduct convertedGeneProduct = geneProductDocConverter.convert(geneProductDocument);

        assertThat(convertedGeneProduct.databaseSubset, is(nullValue()));
    }

    @Test
    public void nullDocSynonymsConvertsToNullModelSynonyms() {
        geneProductDocument.synonyms = null;

        GeneProduct convertedGeneProduct = geneProductDocConverter.convert(geneProductDocument);

        assertThat(convertedGeneProduct.synonyms, is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidGeneProductTypeCausesError() {
        geneProductDocument.type = "this is not a valid gene product type, I promise.";

        geneProductDocConverter.convert(geneProductDocument);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullGeneProductTypeCausesError() {
        geneProductDocument.type = null;

        geneProductDocConverter.convert(geneProductDocument);
    }
}