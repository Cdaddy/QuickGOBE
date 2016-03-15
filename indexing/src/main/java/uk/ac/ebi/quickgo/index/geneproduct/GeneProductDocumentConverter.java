package uk.ac.ebi.quickgo.index.geneproduct;

import uk.ac.ebi.quickgo.geneproduct.common.document.GeneProductDocument;
import uk.ac.ebi.quickgo.index.common.DocumentReaderException;

import com.google.common.base.Preconditions;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.batch.item.ItemProcessor;

import static uk.ac.ebi.quickgo.index.geneproduct.GeneProductParsingHelper.*;

/**
 * Converts a {@link GeneProduct} into an {@link uk.ac.ebi.quickgo.geneproduct.common.document.GeneProductDocument}
 *
 * @author Ricardo Antunes
 */
public class GeneProductDocumentConverter implements ItemProcessor<GeneProduct, GeneProductDocument> {
    public static final int DEFAULT_TAXON_ID = 0;

    private final static Pattern TAXON_ID_PATTERN = Pattern.compile("taxon:([0-9]+)");

    private final String interValueDelimiter;
    private final String intraValueDelimiter;

    public GeneProductDocumentConverter(String interValueDelimiter, String intraValueDelimiter) {
        Preconditions.checkArgument(interValueDelimiter != null && interValueDelimiter.length() > 0,
                "Inter value delimiter can not be null or empty");
        Preconditions.checkArgument(intraValueDelimiter != null && intraValueDelimiter.length() > 0, "Intra " +
                "value delimiter can not be null or empty");

        this.interValueDelimiter = interValueDelimiter;
        this.intraValueDelimiter = intraValueDelimiter;
    }

    @Override public GeneProductDocument process(GeneProduct geneProduct) throws Exception {
        if (geneProduct == null) {
            throw new DocumentReaderException("Gene product object is null");
        }

        Map<String, String> properties =
                convertToMap(geneProduct.properties, interValueDelimiter, intraValueDelimiter);

        GeneProductDocument doc = new GeneProductDocument();
        doc.database = geneProduct.database;
        doc.id = geneProduct.id;
        doc.symbol = geneProduct.symbol;
        doc.name = geneProduct.name;
        doc.synonyms = convertToList(splitValue(geneProduct.synonym, interValueDelimiter));
        doc.type = geneProduct.type;
        doc.taxonId = extractTaxonIdFromValue(geneProduct.taxonId);
        doc.parentId = geneProduct.parentId;
        doc.taxonName = properties.get(TAXON_NAME_KEY);
        doc.referenceProteome = properties.get(REFERENCE_PROTEOME_KEY);
        doc.databaseSubsets = convertToList(properties.get(DATABASE_SUBSET_KEY));

        doc.isCompleteProteome = isTrue(properties.get(COMPLETE_PROTEOME_KEY));
        doc.isAnnotated = isTrue(properties.get(IS_ANNOTATED_KEY));
        doc.isIsoform = isTrue(properties.get(IS_ISOFORM));

        return doc;
    }

    private boolean isTrue(String value) {
        return value != null && value.equalsIgnoreCase(TRUE_STRING);
    }

    @SafeVarargs private final <T> List<T> convertToList(T... elements) {
        List<T> list = Arrays.stream(elements)
                .filter(element -> element != null)
                .collect(Collectors.toList());

        return list.size() == 0 ? null : list;
    }

    private int extractTaxonIdFromValue(String value) {
        int taxonId = DEFAULT_TAXON_ID;

        if (value != null) {
            Matcher matcher = TAXON_ID_PATTERN.matcher(value);

            if (matcher.matches()) {
                taxonId = Integer.parseInt(matcher.group(1));
            }
        }

        return taxonId;
    }
}