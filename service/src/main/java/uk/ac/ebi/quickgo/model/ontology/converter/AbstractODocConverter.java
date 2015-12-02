package uk.ac.ebi.quickgo.model.ontology.converter;

import uk.ac.ebi.quickgo.document.ontology.OntologyDocument;
import uk.ac.ebi.quickgo.model.ontology.OBOTerm;
import uk.ac.ebi.quickgo.model.ontology.converter.field.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created 24/11/15
 * @author Edd
 */
public abstract class AbstractODocConverter<T extends OBOTerm> implements OntologyDocConverter<T> {
    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractODocConverter.class);

    private final static AnnotationGuideLineFieldConverter AG_FIELD_CONVERTER =
            new AnnotationGuideLineFieldConverter();
    private final static BlackListFieldConverter BLACKLIST_FIELD_CONVERTER =
            new BlackListFieldConverter();
    private final static XORelationsFieldConverter XORELATIONS_FIELD_CONVERTER =
            new XORelationsFieldConverter();
    private final static TaxonConstraintsFieldConverter TAXON_CONSTRAINTS_FIELD_CONVERTER =
            new TaxonConstraintsFieldConverter();
    private final static SynonymsFieldConverter SYNONYMS_FIELD_CONVERTER =
            new SynonymsFieldConverter();
    private final static HistoryFieldConverter HISTORY_FIELD_CONVERTER =
            new HistoryFieldConverter();
    private final static XRefsFieldConverter XREFS_FIELD_CONVERTER =
            new XRefsFieldConverter();

    public abstract T convert(OntologyDocument ontologyDocument);

    protected T addCommonFields(OntologyDocument ontologyDocument, T term) {
        term.id = ontologyDocument.id;
        term.name = ontologyDocument.name;
        term.definition = ontologyDocument.definition;
        term.subsets = ontologyDocument.subsets;
        term.isObsolete = ontologyDocument.isObsolete;
        term.replacedBy = ontologyDocument.replacedBy;
        term.comment = ontologyDocument.comment;
        term.children = ontologyDocument.children;
        term.synonyms = SYNONYMS_FIELD_CONVERTER.convertField(ontologyDocument.synonyms);
        term.ancestors = ontologyDocument.ancestors;
        term.secondaryIds = ontologyDocument.secondaryIds;
        term.history = HISTORY_FIELD_CONVERTER.convertField(ontologyDocument.history);
        term.xrefs = XREFS_FIELD_CONVERTER.convertField(ontologyDocument.xrefs);
        term.taxonConstraints = TAXON_CONSTRAINTS_FIELD_CONVERTER.convertField(ontologyDocument.taxonConstraints);
        term.xRelations = XORELATIONS_FIELD_CONVERTER.convertField(ontologyDocument.xRelations);
        term.blacklist = BLACKLIST_FIELD_CONVERTER.convertField(ontologyDocument.blacklist);
        term.annotationGuidelines = AG_FIELD_CONVERTER.convertField(ontologyDocument.annotationGuidelines);

        return term;
    }

}