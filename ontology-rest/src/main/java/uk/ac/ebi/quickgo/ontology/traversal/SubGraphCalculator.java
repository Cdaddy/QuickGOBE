package uk.ac.ebi.quickgo.ontology.traversal;

import uk.ac.ebi.quickgo.ontology.model.graph.AncestorEdge;
import uk.ac.ebi.quickgo.ontology.model.graph.AncestorGraph;
import uk.ac.ebi.quickgo.ontology.model.OntologyRelationship;
import uk.ac.ebi.quickgo.ontology.model.graph.AncestorGraphRequest;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the calculation of a edges for a single vertex, and the creation of further instances for vertexes
 * attached to those edges.
 * @author Tony Wardell
 * Date: 19/06/2017
 * Time: 10:11
 * Created with IntelliJ IDEA.
 */
class SubGraphCalculator {

    private static Logger LOGGER = LoggerFactory.getLogger(SubGraphCalculator.class);

    static Trampoline<AncestorGraph> createTrampoline(AncestorGraphRequest request, AncestorGraph<String> ancestorGraph,
            OntologyGraph ontologyGraph) {
        String target = request.targetVertices.pollFirst();
        if (Objects.nonNull(target)) {

            //Process this node if it hasn't already been considered.
            if (!ancestorGraph.vertices.contains(target)) {
                ancestorGraph.vertices.add(target);

                //if target is not a stop node look for parents
                if (!request.stopVertices.contains(target)) {

                    try {
                        Set<OntologyRelationship> parents = ontologyGraph.parents(target, request.targetRelations);
                        addParentsToWorkQueue(request, parents);
                        ancestorGraph.edges.addAll(mapOntologyRelationshipsToAncestorEdges(parents));

                    } catch (Exception e) {
                        LOGGER.error("SubGraphCalculator#createTrampoline looked up parents for " + target + " but " +
                                             "received exception ", e);
                    }
                }
            }

            if(!request.targetVertices.isEmpty()) {
                return new Trampoline<AncestorGraph>() {
                    public Optional<Trampoline<AncestorGraph>> nextTrampoline() {
                        return Optional.of(createTrampoline(request,
                                                            ancestorGraph,
                                                            ontologyGraph));
                    }
                };
            }

        }
        return new Trampoline<AncestorGraph>() {
            public AncestorGraph getValue() { return ancestorGraph; }
        };

    }

    private static Set<AncestorEdge> mapOntologyRelationshipsToAncestorEdges(Set<OntologyRelationship> parents) {
        Set<AncestorEdge> edgeSet = new HashSet<>();
        parents.stream()
               .map(or -> new AncestorEdge(or.child, or.relationship.toString(), or.parent))
               .forEach(edgeSet::add);
        return edgeSet;
    }

    private static void addParentsToWorkQueue(AncestorGraphRequest request, Set<OntologyRelationship> parents) {
        parents.stream()
               .map(p -> p.parent)
               .forEach(request.targetVertices::addLast);
    }
}
