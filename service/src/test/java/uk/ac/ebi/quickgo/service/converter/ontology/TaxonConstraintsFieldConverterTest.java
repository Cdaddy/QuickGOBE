package uk.ac.ebi.quickgo.service.converter.ontology;

import uk.ac.ebi.quickgo.service.model.ontology.OBOTerm;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.ac.ebi.quickgo.ff.flatfield.FlatFieldBuilder.newFlatField;
import static uk.ac.ebi.quickgo.ff.flatfield.FlatFieldBuilder.newFlatFieldFromDepth;
import static uk.ac.ebi.quickgo.ff.flatfield.FlatFieldLeaf.newFlatFieldLeaf;

/**
 * Created 01/12/15
 * @author Edd
 */
public class TaxonConstraintsFieldConverterTest {
    private TaxonConstraintsFieldConverter converter;

    @Before
    public void setup() {
        this.converter = new TaxonConstraintsFieldConverter();
    }

    @Test
    public void convertsTaxonConstraints() {
        List<String> rawTaxonConstraints = new ArrayList<>();

        String ancestorId = "GO:0005623";
        String taxIdType = "NCBITaxon";
        String taxId = "131568";
        String citationId = "PMID:00000003";

        rawTaxonConstraints.add(newFlatFieldFromDepth(2)
                .addField(newFlatFieldLeaf(ancestorId))
                .addField(newFlatFieldLeaf("cell"))
                .addField(newFlatFieldLeaf("only_in_taxon"))
                .addField(newFlatFieldLeaf("131567"))
                .addField(newFlatFieldLeaf(taxIdType))
                .addField(newFlatFieldLeaf("cellular organisms"))
                .addField(newFlatField()
                        .addField(newFlatFieldLeaf("PMID:00000001"))
                        .addField(newFlatFieldLeaf("PMID:00000002")))
                .buildString());
        rawTaxonConstraints.add(newFlatFieldFromDepth(2)
                .addField(newFlatFieldLeaf("GO:0005624"))
                .addField(newFlatFieldLeaf("cell"))
                .addField(newFlatFieldLeaf("only_in_taxon"))
                .addField(newFlatFieldLeaf(taxId))
                .addField(newFlatFieldLeaf(taxIdType))
                .addField(newFlatFieldLeaf("cellular organisms"))
                .addField(newFlatField()
                        .addField(newFlatFieldLeaf(citationId))
                        .addField(newFlatFieldLeaf("PMID:00000004")))
                .buildString());

        List<OBOTerm.TaxonConstraint> taxonConstraints = converter.convertField(rawTaxonConstraints);
        assertThat(taxonConstraints.size(), is(2));
        assertThat(taxonConstraints.get(0).ancestorId, is(ancestorId));
        assertThat(taxonConstraints.get(0).taxIdType, is(taxIdType));
        assertThat(taxonConstraints.get(1).taxId, is(taxId));
        assertThat(taxonConstraints.get(1).citations.get(0).id, is(citationId));
    }

}