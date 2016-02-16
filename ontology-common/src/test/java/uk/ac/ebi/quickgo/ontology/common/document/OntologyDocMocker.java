package uk.ac.ebi.quickgo.ontology.common.document;

import java.util.ArrayList;
import java.util.Arrays;

import static uk.ac.ebi.quickgo.common.converter.FlatFieldBuilder.newFlatFieldFromDepth;
import static uk.ac.ebi.quickgo.common.converter.FlatFieldLeaf.newFlatFieldLeaf;

/**
 * Class to create mocked objects of different {@code docType}s, which are valid according to {@link OntologyDocument}.
 *
 * Created 03/11/15
 * @author Edd
 */
public final class OntologyDocMocker {
    public static final int FLAT_FIELD_DEPTH = 0;

    private OntologyDocMocker(){}

    public static OntologyDocument createGODoc(String id, String name) {
        OntologyDocument od = createOBODoc(id, name);
        od.ontologyType = OntologyType.GO.name();
        od.usage = "Unrestricted";
        od.aspect = "Process";

        return od;
    }

    public static OntologyDocument createECODoc(String id, String name) {
        OntologyDocument od = createOBODoc(id, name);
        od.ontologyType = OntologyType.ECO.name();

        return od;
    }

    public static OntologyDocument createOBODoc(String id, String name) {
        OntologyDocument od = new OntologyDocument();
        od.id = id;
        od.name = name;
        od.ancestors = Arrays.asList("GO:0000100", "GO:0000200");
        od.definition = "The chemical reactions and pathways involving creatine (N-(aminoiminomethyl)" +
                "-N-methylglycine), a compound synthesized from the amino acids arginine, glycine, and methionine " +
                "that occurs in muscle.";
        od.subsets = Arrays.asList("goslim_pombe",
                "goslim_generic",
                "goslim_yeast",
                "goslim_chembl");
        od.isObsolete = true;
        od.replacedBy = "GO:0000002";
        od.considers = Arrays.asList("GO:0000003", "GO:0000004");
        od.comment = "Note that protein targeting encompasses the transport of the protein to " +
                "the specified location, and may also include additional steps such as protein processing.";
        od.children = Arrays.asList("GO:0000011", "GO:0000012");
        od.synonymNames = Arrays.asList("creatine anabolism", "crayola testarossa");
        od.secondaryIds = Arrays.asList("GO:0000003", "GO:0000004");

        // ------ nested, stored fields, which require reconstructing -------
        // example synonyms
        od.synonyms = new ArrayList<>();
        od.synonyms.add(
                newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                        .addField(newFlatFieldLeaf("creatine anabolism"))
                        .addField(newFlatFieldLeaf("exact"))
                        .buildString()
        );
        od.synonyms.add(
                newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                        .addField(newFlatFieldLeaf("crayola testarossa"))
                        .addField(newFlatFieldLeaf("inprecise"))
                        .buildString()
        );

        // example history
        od.history = new ArrayList<>();
        od.history.add(
                newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                        .addField(newFlatFieldLeaf("Gonna do something like it's ... "))
                        .addField(newFlatFieldLeaf("11:59, 31 Dec, 1999"))
                        .addField(newFlatFieldLeaf("PARTY!"))
                        .addField(newFlatFieldLeaf("Must be done"))
                        .addField(newFlatFieldLeaf("Textual description"))
                        .buildString()
        );
        od.history.add(
                newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                        .addField(newFlatFieldLeaf("History name"))
                        .addField(newFlatFieldLeaf("Tuesday next week"))
                        .addField(newFlatFieldLeaf("PARTY!"))
                        .addField(newFlatFieldLeaf("Must be done"))
                        .addField(newFlatFieldLeaf("Okay"))
                        .buildString()
        );

        // example xrefs
        od.xrefs = new ArrayList<>();
        od.xrefs.add(newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                .addField(newFlatFieldLeaf("InterPro"))
                .addField(newFlatFieldLeaf("IPR031034"))
                .addField(newFlatFieldLeaf("Creatinine amidohydrolase"))
                .buildString());
        od.xrefs.add(newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                .addField(newFlatFieldLeaf("AnotherXref"))
                .addField(newFlatFieldLeaf("IPR031035"))
                .addField(newFlatFieldLeaf("Pickled Onions"))
                .buildString());

        // example taxonomy constraints
        // format: ancestorId|ancestorName|relationship|taxId|taxIdType|taxName|pubMedId1&pubMedId2|blacklist
        od.taxonConstraints = new ArrayList<>();
        od.taxonConstraints.add(newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                .addField(newFlatFieldLeaf("GO:0005623"))
                .addField(newFlatFieldLeaf("cell"))
                .addField(newFlatFieldLeaf("only_in_taxon"))
                .addField(newFlatFieldLeaf("131567"))
                .addField(newFlatFieldLeaf("NCBITaxon"))
                .addField(newFlatFieldLeaf("cellular organisms"))
                .addField(newFlatFieldFromDepth(3)
                        .addField(newFlatFieldLeaf("PMID:00000001"))
                        .addField(newFlatFieldLeaf("PMID:00000002")))
                .buildString());
        od.taxonConstraints.add(newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                .addField(newFlatFieldLeaf("GO:0005624"))
                .addField(newFlatFieldLeaf("cell"))
                .addField(newFlatFieldLeaf("only_in_taxon"))
                .addField(newFlatFieldLeaf("131568"))
                .addField(newFlatFieldLeaf("NCBITaxon"))
                .addField(newFlatFieldLeaf("cellular organisms"))
                .addField(newFlatFieldFromDepth(3)
                        .addField(newFlatFieldLeaf("PMID:00000003"))
                        .addField(newFlatFieldLeaf("PMID:00000004")))
                .buildString());

        // example blacklist
        // format: geneProductId|geneProductDB|reason|category|method
        od.blacklist = new ArrayList<>();
        od.blacklist.add(newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                .addField(newFlatFieldLeaf("GO:0000001"))                   //id
                .addField(newFlatFieldLeaf("NOT-qualified manual"))         //category
                .addField(newFlatFieldLeaf("protein"))                      //entity type
                .addField(newFlatFieldLeaf("A5I1R9"))                       //entity id
                .addField(newFlatFieldLeaf("441771"))                       //taxon id
                .addField(newFlatFieldLeaf("A5I1R9_CLOBH"))                 //entity name
                .addField(newFlatFieldLeaf("GO:0007005"))                   //ancestor go id
                .addField(newFlatFieldLeaf("1 NOT-qualified manual etc"))   //reason
                .addField(newFlatFieldLeaf("IER12345"))
                .buildString());
        od.blacklist.add(newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                .addField(newFlatFieldLeaf("GO:0000001"))                   //id
                .addField(newFlatFieldLeaf("IS-qualified manual"))         //category
                .addField(newFlatFieldLeaf("protein"))                      //entity type
                .addField(newFlatFieldLeaf("B5I1R9"))                       //entity id
                .addField(newFlatFieldLeaf("441771"))                       //taxon id
                .addField(newFlatFieldLeaf("B5I1R9_CLOBH"))                 //entity name
                .addField(newFlatFieldLeaf("GO:0007006"))                   //ancestor go id
                .addField(newFlatFieldLeaf("1 NOT-qualified manual etc"))   //reason
                .addField(newFlatFieldLeaf("IER12346"))
                .buildString());

        // example xontology relations
        // format: xId|xTerm|xNamespace|xUrl|xRelation
        od.xRelations = new ArrayList<>();
        od.xRelations.add(newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                .addField(newFlatFieldLeaf("CHEBI:16919"))
                .addField(newFlatFieldLeaf("creatine"))
                .addField(newFlatFieldLeaf("CHEBI"))
                .addField(newFlatFieldLeaf("http://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:16919"))
                .addField(newFlatFieldLeaf("has_participant"))
                .buildString());
        od.xRelations.add(newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                .addField(newFlatFieldLeaf("CHEBI:16920"))
                .addField(newFlatFieldLeaf("creatiney"))
                .addField(newFlatFieldLeaf("CHEBI"))
                .addField(newFlatFieldLeaf("http://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:16920"))
                .addField(newFlatFieldLeaf("has_participant"))
                .buildString());

        // annotation guidelines
        // format: description|url
        od.annotationGuidelines = new ArrayList<>();
        od.annotationGuidelines.add(newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                .addField(newFlatFieldLeaf("description 0"))
                .addField(newFlatFieldLeaf("http://www.guardian.co.uk"))
                .buildString()
        );
        od.annotationGuidelines.add(newFlatFieldFromDepth(FLAT_FIELD_DEPTH)
                .addField(newFlatFieldLeaf("description 1"))
                .addField(newFlatFieldLeaf("http://www.pinkun.com"))
                .buildString()
        );

        return od;
    }
}