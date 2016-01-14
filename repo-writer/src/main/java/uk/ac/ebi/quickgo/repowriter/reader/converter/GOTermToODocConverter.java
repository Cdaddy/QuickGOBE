package uk.ac.ebi.quickgo.repowriter.reader.converter;

import uk.ac.ebi.quickgo.document.ontology.OntologyDocument;
import uk.ac.ebi.quickgo.ff.flatfield.FlatFieldBuilder;
import uk.ac.ebi.quickgo.model.ontology.go.GOTerm;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.ac.ebi.quickgo.ff.flatfield.FlatFieldBuilder.newFlatField;
import static uk.ac.ebi.quickgo.ff.flatfield.FlatFieldLeaf.newFlatFieldLeaf;

/**
 * Converts a {@link GOTerm} instance into an {@link OntologyDocument} instance.
 *
 * Created 14/12/15
 * @author Edd
 */
public class GOTermToODocConverter implements Function<Optional<GOTerm>, Optional<OntologyDocument>> {

    private final static GenericTermToODocConverter GENERIC_TERM_TO_DOC_CONVERTER = new GenericTermToODocConverter();

    @Override public Optional<OntologyDocument> apply(Optional<GOTerm> termOptional) {
        Optional<OntologyDocument> ontologyDocument = GENERIC_TERM_TO_DOC_CONVERTER.apply(termOptional);

        if (termOptional.isPresent() && ontologyDocument.isPresent()) {

            GOTerm goTerm = termOptional.get();
            OntologyDocument doc = ontologyDocument.get();

            doc.annotationGuidelines = extractAnnGuidelines(goTerm);
            doc.aspect = goTerm.getAspect() == null?
                    null : goTerm.getAspect().text;
            doc.children = extractChildren(goTerm);
            doc.taxonConstraints = extractTaxonConstraints(goTerm);
            doc.usage = goTerm.getUsage() == null?
                    null : goTerm.getUsage().getText();

            return Optional.of(doc);
        } else {
            return Optional.empty();
        }
    }

    protected List<String> extractChildren(GOTerm goTerm) {
        if (!isEmpty(goTerm.getChildren())) {
            return goTerm.getChildren().stream()
                    .map(
                            t -> t.getChild().getId())
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    /*
     * format: description|url
     */
    protected List<String> extractAnnGuidelines(GOTerm goTerm) {
        if (!isEmpty(goTerm.getGuidelines())) {
            return goTerm.getGuidelines().stream()
                    .map(
                            t -> newFlatField()
                                    .addField(newFlatFieldLeaf(t.getTitle()))
                                    .addField(newFlatFieldLeaf(t.getUrl()))
                                    .buildString())
                    .collect(Collectors.toList());
        } else return null;
    }

    /*
     * format: ancestorId|ancestorName|relationship|taxId|taxIdType|taxName|pubMedId1&pubMedId2
     */
    protected List<String> extractTaxonConstraints(GOTerm goTerm) {
        if (!isEmpty(goTerm.getTaxonConstraints())) {
            return goTerm.getTaxonConstraints().stream()
                    .map(t -> {
                        FlatFieldBuilder pubmedsAsFlatField = newFlatField();
                        t.getSourcesIds().stream().forEach(
                                s -> pubmedsAsFlatField.addField(newFlatFieldLeaf(s))
                        );

                        return newFlatField()
                                .addField(newFlatFieldLeaf(t.getGoId()))
                                .addField(newFlatFieldLeaf(t.getName()))
                                .addField(newFlatFieldLeaf(t.relationship()))
                                .addField(newFlatFieldLeaf(t.getTaxId()))
                                .addField(newFlatFieldLeaf(t.getTaxonName()))
                                .addField(pubmedsAsFlatField)
                                .buildString();
                    })
                    .collect(Collectors.toList());
        } else return null;
    }
}