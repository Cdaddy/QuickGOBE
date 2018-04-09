package uk.ac.ebi.quickgo.annotation.download.converter;

import uk.ac.ebi.quickgo.annotation.download.converter.helpers.ConnectedXRefs;
import uk.ac.ebi.quickgo.annotation.model.Annotation;
import uk.ac.ebi.quickgo.common.model.Aspect;
import uk.ac.ebi.quickgo.annotation.download.converter.helpers.GeneProductId;

import java.util.*;
import java.util.function.BiFunction;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static uk.ac.ebi.quickgo.annotation.download.converter.helpers.Date.toYYYYMMDD;
import static uk.ac.ebi.quickgo.annotation.download.converter.helpers.GeneProductType.toGpType;
import static uk.ac.ebi.quickgo.annotation.download.converter.helpers.Helper.nullOrEmptyListToEmptyString;
import static uk.ac.ebi.quickgo.annotation.download.converter.helpers.Helper.nullToEmptyString;
import static uk.ac.ebi.quickgo.common.model.Aspect.fromScientificName;

/**
 * Convert an {@link Annotation}  to a String representation.
 * See http://geneontology.org/page/go-annotation-file-gaf-format-21}
 *
 * An excerpt from a GAF file is below:
 <pre>
 UniProtKB	Q4VCS5	AMOT		GO:0001570	GO_REF:0000107	IEA	UniProtKB:Q8VHG2|ensembl:ENSMUSP00000108455	P
 Angiomotin	AMOT_HUMAN|AMOT|KIAA1071	protein	taxon:9606	20170107	Ensembl
 UniProtKB	Q4VCS5	AMOT		GO:0001701	GO_REF:0000107	IEA	UniProtKB:Q8VHG2|ensembl:ENSMUSP00000108455	P
 Angiomotin	AMOT_HUMAN|AMOT|KIAA1071	protein	taxon:9606	20170107	Ensembl
 UniProtKB	Q4VCS5	AMOT		GO:0001702	GO_REF:0000107	IEA	UniProtKB:Q8VHG2|ensembl:ENSMUSP00000108455	P
 Angiomotin	AMOT_HUMAN|AMOT|KIAA1071	protein	taxon:9606	20170107	Ensembl
 UniProtKB	Q4VCS5	AMOT		GO:0001725	PMID:16043488	IDA		C	Angiomotin	AMOT_HUMAN|AMOT|KIAA1071
 protein	taxon:9606	20051207	UniProt		UniProtKB:Q4VCS5-1
 UniProtKB	Q4VCS5	AMOT		GO:0001726	PMID:11257124	IDA		C	Angiomotin	AMOT_HUMAN|AMOT|KIAA1071
 protein	taxon:9606	20091109	MGI
 UniProtKB	Q4VCS5	AMOT		GO:0003365	GO_REF:0000107	IEA	UniProtKB:Q8VHG2|ensembl:ENSMUSP00000108455	P
 Angiomotin	AMOT_HUMAN|AMOT|KIAA1071	protein	taxon:9606	20170107	Ensembl
 UniProtKB	Q4VCS5	AMOT		GO:0004872	PMID:11257124	IDA		F	Angiomotin	AMOT_HUMAN|AMOT|KIAA1071
 protein	taxon:9606	20091109	MGI
 UniProtKB	Q4VCS5	AMOT		GO:0005515	PMID:11257124	IPI	UniProtKB:P00747	F	Angiomotin
 AMOT_HUMAN|AMOT|KIAA1071	protein	taxon:9606	20051212	HGNC
 UniProtKB	Q4VCS5	AMOT		GO:0005515	PMID:16043488	IPI	UniProtKB:Q6RHR9-2	F	Angiomotin
 AMOT_HUMAN|AMOT|KIAA1071	protein	taxon:9606	20051207	UniProt		UniProtKB:Q4VCS5-1
 UniProtKB	Q4VCS5	AMOT		GO:0005515	PMID:19615732	IPI	UniProtKB:P35240	F	Angiomotin
 AMOT_HUMAN|AMOT|KIAA1071	protein	taxon:9606	20170108	IntAct
 </pre>
 *
 * @author Tony Wardell
 * Date: 17/01/2017
 * Time: 11:54
 * Created with IntelliJ IDEA.
 */
public class AnnotationToGAF implements BiFunction<Annotation, List<String>, List<String>> {

    static final String OUTPUT_DELIMITER = "\t";
    private static final String PIPE = "|";
    private static final String TAXON = "taxon:";
    private static final Set<String> NOT_GAF_QUALIFIERS =
            new HashSet<>(asList("NOT|enables", "NOT|part_of", "NOT|involved_in"));
    private static final Set<String> VALID_GAF_QUALIFIERS =
            new HashSet<>(asList("contributes_to", "NOT|contributes_to", "colocalizes_with", "NOT|colocalizes_with"));

    /**
     * Convert an {@link Annotation} to a String representation.
     * @param annotation instance
     * @param selectedFields ignore for GAF
     * @return String TSV delimited representation of an annotation in GAF format.
     *
     */
    @Override
    public List<String> apply(Annotation annotation, List<String> selectedFields) {
        if (Objects.isNull(annotation.slimmedIds) || annotation.slimmedIds.isEmpty()) {
            return Collections.singletonList(toOutputRecord(annotation, annotation.goId));
        } else {
            return annotation.slimmedIds.stream()
                    .map(goId -> this.toOutputRecord(annotation, goId))
                    .collect(toList());
        }
    }

    private String toOutputRecord(Annotation annotation, String goId) {
        StringJoiner tsvJoiner = new StringJoiner(OUTPUT_DELIMITER);
        final GeneProductId geneProductId = GeneProductId.fromString(annotation.geneProductId);
        return tsvJoiner
                .add(nullToEmptyString(geneProductId.db))
                .add(nullToEmptyString(geneProductId.id))
                .add(nullToEmptyString(annotation.symbol))
                .add(gafQualifierAsString(annotation.qualifier))
                .add(nullToEmptyString(goId))
                .add(nullToEmptyString(annotation.reference))
                .add(nullToEmptyString(annotation.goEvidence))
                .add(ConnectedXRefs.asString(annotation.withFrom))
                .add(fromScientificName(annotation.goAspect).map(Aspect::getCharacter).orElse(""))
                .add(nullToEmptyString(annotation.name))
                .add(nullToEmptyString(annotation.synonyms))
                .add(nonNull(geneProductId.db) ? toGpType.apply(geneProductId.db) : "")
                .add(gafTaxonAsString(annotation))
                .add(nonNull(annotation.date) ? toYYYYMMDD.apply(annotation.date) : "")
                .add(nullToEmptyString(annotation.assignedBy))
                .add(nullOrEmptyListToEmptyString(annotation.extensions, ConnectedXRefs::asString))
                .add(nullToEmptyString(geneProductId.withIsoFormOrVariant))
                .toString();
    }

    private String gafQualifierAsString(String qualifier) {
        String annotationQualifier = nullToEmptyString.apply(qualifier);

        String gafQualifier;
        if (NOT_GAF_QUALIFIERS.contains(annotationQualifier)) {
            gafQualifier = "NOT";
        } else if (VALID_GAF_QUALIFIERS.contains(annotationQualifier)) {
            gafQualifier = annotationQualifier;
        } else {
            gafQualifier = "";
        }

        return gafQualifier;
    }

    String gafTaxonAsString(Annotation annotation) {
        StringBuilder taxonBuilder = new StringBuilder();
        taxonBuilder.append(TAXON)
                .append(annotation.taxonId)
                .append(annotation.interactingTaxonId > 0 ? PIPE + annotation.interactingTaxonId : "");
        return taxonBuilder.toString();
    }

}
