package uk.ac.ebi.quickgo.model.ontology.converter.field;

import uk.ac.ebi.quickgo.document.FlatField;
import uk.ac.ebi.quickgo.model.FieldConverter;
import uk.ac.ebi.quickgo.model.ontology.OBOTerm;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.quickgo.document.FlatFieldBuilder.parseFlatField;

/**
 * Created 01/12/15
 * @author Edd
 */
public class HistoryFieldConverter implements FieldConverter<OBOTerm.History> {
    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryFieldConverter.class);

    @Override public Optional<OBOTerm.History> apply(String s) {
        // format: name|timestamp|action|category|text
        OBOTerm.History historicalInfo = new OBOTerm.History();

        List<FlatField> fields = parseFlatField(s).getFields();
        if (fields.size() == 5) {
            historicalInfo.name = nullOrString(fields.get(0).buildString());
            historicalInfo.timestamp = nullOrString(fields.get(1).buildString());
            historicalInfo.action = nullOrString(fields.get(2).buildString());
            historicalInfo.category = nullOrString(fields.get(3).buildString());
            historicalInfo.text = nullOrString(fields.get(4).buildString());
            return Optional.of(historicalInfo);
        } else {
            LOGGER.warn("Could not parse flattened history: {}", s);
        }
        return Optional.empty();
    }
}