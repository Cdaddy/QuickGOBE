package uk.ac.ebi.quickgo.index.annotation;

import uk.ac.ebi.quickgo.annotation.common.document.AnnotationDocument;
import uk.ac.ebi.quickgo.index.common.DocumentReaderException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.batch.item.ItemProcessor;

import static uk.ac.ebi.quickgo.index.common.datafile.DataFileParsingHelper.COLON;
import static uk.ac.ebi.quickgo.index.common.datafile.DataFileParsingHelper.EQUALS;
import static uk.ac.ebi.quickgo.index.common.datafile.DataFileParsingHelper.PIPE;
import static uk.ac.ebi.quickgo.index.common.datafile.DataFileParsingHelper.convertLinePropertiesToMap;

/**
 * Created 19/04/16
 * @author Edd
 */
public class AnnotationDocumentConverter implements ItemProcessor<Annotation, AnnotationDocument> {
    private static final String GO_EVIDENCE = "go_evidence";

    @Override public AnnotationDocument process(Annotation annotation) throws Exception {
        if (annotation == null) {
            throw new DocumentReaderException("Annotation object is null");
        }

        Map<String, String> propertiesMap = convertLinePropertiesToMap(annotation.annotationProperties, PIPE, EQUALS);

        AnnotationDocument doc = new AnnotationDocument();

        doc.geneProductId = constructGeneProductId(annotation);
        doc.qualifier = annotation.qualifier;
        doc.goId = annotation.goId;
        doc.goEvidence = propertiesMap.get(GO_EVIDENCE);
        doc.reference = annotation.dbReferences;
        doc.withFrom = constructWithFrom(annotation);
        doc.taxonId = annotation.interactingTaxonId;
        doc.assignedBy = annotation.assignedBy;
        doc.extension = annotation.annotationExtension;
        doc.ecoId = annotation.eco;

        // symbol: doc.symbol = ??

        return doc;
    }

    private List<String> constructWithFrom(Annotation annotation) {
        return annotation.with == null? null : Arrays.asList(annotation.with.split(PIPE));
    }

    private String constructGeneProductId(Annotation annotation) {
        return annotation.db + COLON + annotation.dbObjectId;
    }
}
