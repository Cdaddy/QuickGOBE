package uk.ac.ebi.quickgo.model.ontology.converter.field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.quickgo.ff.delim.FlatField;
import uk.ac.ebi.quickgo.model.FieldConverter;
import uk.ac.ebi.quickgo.model.ontology.OBOTerm;

import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.quickgo.ff.delim.FlatFieldBuilder.newFlatFieldFromDepth;

/**
 * Created 01/12/15
 * @author Edd
 */
public class SynonymsFieldConverter implements FieldConverter<OBOTerm.Synonym> {
    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(SynonymsFieldConverter.class);

    @Override public Optional<OBOTerm.Synonym> apply(String s) {
        // format: name|type
        OBOTerm.Synonym synonym = new OBOTerm.Synonym();
        List<FlatField> fields = newFlatFieldFromDepth(2).parse(s).getFields();
        if (fields.size() == 2) {
            synonym.synonymName = nullOrString(fields.get(0).buildString());
            synonym.synonymType = nullOrString(fields.get(1).buildString());
            return Optional.of(synonym);
        } else {
            LOGGER.warn("Could not parse flattened synonym: {}", s);
        }
        return Optional.empty();
    }
}
