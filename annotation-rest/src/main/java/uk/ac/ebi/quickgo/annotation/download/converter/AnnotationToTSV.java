package uk.ac.ebi.quickgo.annotation.download.converter;

import uk.ac.ebi.quickgo.annotation.model.Annotation;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static uk.ac.ebi.quickgo.annotation.download.TSVDownload.*;

/**
 * Convert an {@link Annotation} to a String representation of the view seen in QuickGO front end.
 * DB           ID	    Symbol	Qualifier	GO ID       GO Name         Aspect	    Evidence    Reference
 * With	            Taxon	Date        Source  Splice
 * UniProtKB	Q4VCS5	AMOT	-	        GO:0005515	protein binding	Function	IPI	        PMID:11257124
 * UniProtKB:P00747	9606	20051212	HGNC	-
 * UniProtKB	Q4VCS5	AMOT	-	        GO:0005515	protein binding	Function	IPI	        PMID:16043488
 * UniProtKB:Q6RHR9-2	9606	20051207	UniProt	UniProtKB:Q4VCS5-1
 *
 * @author Tony Wardell
 * Date: 26/04/2017
 * Time: 14:56
 * Created with IntelliJ IDEA.
 */
public class AnnotationToTSV extends AnnotationTo implements BiFunction<Annotation, List<String>, List<String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationToTSV.class);
    private final Map<String, BiConsumer<OutputContent, StringJoiner>> selected2Content;

    public AnnotationToTSV() {
        selected2Content = new HashMap<>();
        initialiseContentMappings();
    }

    @Override public List<String> apply(Annotation annotation, List<String> selectedFields) {
        final List<String> columns = whichColumnsWillWeShow(selectedFields);
        if (isSlimmedRequest(annotation)) {
            return Collections.singletonList(output(new OutputContent(annotation, columns, null)));
        } else {
            return annotation.slimmedIds.stream()
                    .map(goId -> output(new OutputContent(annotation, columns, goId)))
                    .collect(toList());
        }
    }

    private void initialiseContentMappings() {
        selected2Content.put(GENE_PRODUCT_ID_FIELD_NAME,
                             (c, j) -> {
                                 String[] elements = nullToEmptyString.apply(c.annotation.geneProductId).split(":");
                                 if (elements.length == 2) {
                                     j.add(elements[0]);
                                     j.add(elements[1]);
                                 } else {
                                     j.add("");
                                     j.add("");
                                 }
                             });
        selected2Content.put(SYMBOL_FIELD_NAME, (c, j) -> j.add(nullToEmptyString.apply(c.annotation.symbol)));
        selected2Content.put(QUALIFIER_FIELD_NAME, (c, j) -> j.add(nullToEmptyString.apply(c.annotation.qualifier)));
        selected2Content.put(GO_TERM_FIELD_NAME, (c, j) -> {
            if (Objects.nonNull(c.slimmedToGoId)) {
                j.add(c.slimmedToGoId);
            }
            j.add(nullToEmptyString.apply(c.annotation.goId));
        });
        selected2Content.put(GO_NAME_FIELD_NAME, (c, j) -> j.add(nullToEmptyString.apply(c.annotation.goName)));
        selected2Content.put(ECO_ID_FIELD_NAME, (c, j) -> j.add(nullToEmptyString.apply(c.annotation.evidenceCode)));
        selected2Content.put(GO_EVIDENCE_CODE_FIELD_NAME,
                (c, j) -> j.add(nullToEmptyString.apply(c.annotation.goEvidence)));
        selected2Content.put(REFERENCE_FIELD_NAME, (c, j) -> j.add(nullToEmptyString.apply(c.annotation.reference)));
        selected2Content.put(WITH_FROM_FIELD_NAME, (c, j) -> j.add(withFromAsString(c.annotation.withFrom)));
        selected2Content.put(TAXON_ID_FIELD_NAME,
                (c, j) -> j.add(c.annotation.taxonId == 0 ? "" : Integer.toString(c.annotation.taxonId)));
        selected2Content.put(ASSIGNED_BY_FIELD_NAME,
                (c, j) -> j.add(nullToEmptyString.apply(c.annotation.assignedBy)));
        selected2Content.put(ANNOTATION_EXTENSION_FIELD_NAME,
                (c, j) -> j.add(extensionsAsString(c.annotation.extensions)));
        selected2Content.put(DATE_FIELD_NAME, (c, j) -> j.add(toYMD(c.annotation.date)));
        selected2Content.put(TAXON_NAME_FIELD_NAME, (c, j) -> j.add(nullToEmptyString.apply(c.annotation.taxonName)));
        selected2Content.put(GENE_PRODUCT_NAME_FIELD_NAME, (c, j) -> j.add(nullToEmptyString.apply(c.annotation.name)));
        selected2Content.put(GENE_PRODUCT_SYNONYMS_FIELD_NAME,
                (c, j) -> j.add(nullToEmptyString.apply(c.annotation.synonyms)));
        selected2Content.put(GENE_PRODUCT_TYPE_FIELD_NAME,
                (c, j) -> j.add(toGeneProductType(idToComponents(c.annotation.geneProductId)[DB])));
    }

    private boolean isSlimmedRequest(Annotation annotation) {
        return Objects.isNull(annotation.slimmedIds) || annotation.slimmedIds.isEmpty();
    }

    private String output(OutputContent outputContent) {
        StringJoiner tsvJoiner = new StringJoiner(OUTPUT_DELIMITER);
        for (String selectedField : outputContent.selectedFields) {
            selected2Content.get(selectedField).accept(outputContent, tsvJoiner);
        }
        return tsvJoiner.toString();
    }

    private static class OutputContent {
        final Annotation annotation;
        final List<String> selectedFields;
        final String slimmedToGoId;

        private OutputContent(Annotation annotation, List<String> selectedFields, String slimmedToGoId) {
            this.annotation = annotation;
            this.selectedFields = selectedFields;
            this.slimmedToGoId = slimmedToGoId;
        }
    }
}
