package uk.ac.ebi.quickgo.service.converter.ontology;

import uk.ac.ebi.quickgo.ff.flatfield.FlatField;
import uk.ac.ebi.quickgo.service.converter.FieldConverter;
import uk.ac.ebi.quickgo.service.model.ontology.OBOTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.quickgo.ff.flatfield.FlatFieldBuilder.newFlatFieldFromDepth;

/**
 * Created 01/12/15
 * @author Edd
 */
class TaxonConstraintsFieldConverter implements FieldConverter<OBOTerm.TaxonConstraint> {
    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TaxonConstraintsFieldConverter.class);

    @Override public Optional<OBOTerm.TaxonConstraint> apply(String s) {
        // format: ancestorId|ancestorName|relationship|taxId|taxIdType|taxName|pubMedId1&pubMedId2
        OBOTerm.TaxonConstraint taxonConstraint = new OBOTerm.TaxonConstraint();

        List<FlatField> fields = newFlatFieldFromDepth(2).parse(s).getFields();
        if (fields.size() == 7) {
            taxonConstraint.ancestorId = nullOrString(fields.get(0).buildString());
            taxonConstraint.ancestorName = nullOrString(fields.get(1).buildString());
            taxonConstraint.relationship = nullOrString(fields.get(2).buildString());
            taxonConstraint.taxId = nullOrString(fields.get(3).buildString());
            taxonConstraint.taxIdType = nullOrString(fields.get(4).buildString());
            taxonConstraint.taxName = nullOrString(fields.get(5).buildString());
            taxonConstraint.citations = new ArrayList<>();
            fields.get(6).getFields().stream().forEach(
                    rawLit -> {
                        OBOTerm.Literature literature = new OBOTerm.Literature();
                        literature.id = rawLit.buildString();
                        taxonConstraint.citations.add(literature);
                    }
            );

            return Optional.of(taxonConstraint);
        } else {
            LOGGER.warn("Could not parse flattened taxonConstraint: {}", s);
        }
        return Optional.empty();
    }
}
