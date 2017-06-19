package uk.ac.ebi.quickgo.ontology.traversal;

import com.google.common.base.Preconditions;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.AllDirectedPaths;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;
import uk.ac.ebi.quickgo.ontology.model.AncestorGraph;
import uk.ac.ebi.quickgo.ontology.model.OntologyRelationType;
import uk.ac.ebi.quickgo.ontology.model.OntologyRelationship;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * This class represents an ontology graph whose vertices are ontology terms,
 * and edges are the relationships between terms.
 * <p>
 * Created 18/05/16
 *
 * @author Edd
 */
public class OntologyGraph implements OntologyGraphTraversal {
    static final String MOLECULAR_FUNCTION_STOP_NODE = "GO:0003674";
    static final String BIOLOGICAL_PROCESS_STOP_NODE = "GO:0008150";
    static final String CELLULAR_COMPONENT_STOP_NODE = "GO:0005575";

    private static final List<String> STOP_NODES =
            Arrays.asList(MOLECULAR_FUNCTION_STOP_NODE,
                    BIOLOGICAL_PROCESS_STOP_NODE,
                    CELLULAR_COMPONENT_STOP_NODE);

    private final DirectedGraph<String, OntologyRelationship> ontology;
    private final Map<String, Set<OntologyRelationship>> ancestorEdgesMap = new HashMap<>();

    public OntologyGraph() {
        ontology = new DirectedMultigraph<>(new ClassBasedEdgeFactory<>(OntologyRelationship.class));
    }

    private static String getOppositeVertex(Graph<String, OntologyRelationship> graph, OntologyRelationship edge,
                                            String vertex) {
        String source = graph.getEdgeSource(edge);
        String target = graph.getEdgeTarget(edge);
        if (vertex.equals(source)) {
            return target;
        } else if (vertex.equals(target)) {
            return source;
        } else {
            throw new IllegalArgumentException(
                    "No such graph vertex: " + vertex);
        }
    }

    public Set<OntologyRelationship> getEdges() {
        return ontology.edgeSet();
    }

    public Set<String> getVertices() {
        return ontology.vertexSet();
    }

    public void addRelationships(Collection<? extends OntologyRelationship> relationships) {
        Preconditions.checkArgument(relationships != null, "Relationships to add to the graph cannot be null");

        // populate graph with edges, whilst recording the vertices
        relationships.forEach(
                oEdge -> {
                    if (!ontology.containsVertex(oEdge.child)) {
                        ontology.addVertex(oEdge.child);
                    }
                    if (!ontology.containsVertex(oEdge.parent)) {
                        ontology.addVertex(oEdge.parent);
                    }
                    ontology.addEdge(
                            oEdge.child,
                            oEdge.parent,
                            new OntologyRelationship(oEdge.child, oEdge.parent, oEdge.relationship));
                }
        );
    }

    @Override
    public List<List<OntologyRelationship>> paths(
            Set<String> startingVertices,
            Set<String> endingVertices,
            OntologyRelationType... relations) {

        Preconditions.checkArgument(!isNullOrEmpty(startingVertices), "Starting vertices cannot be null/empty.");
        Preconditions.checkArgument(!isNullOrEmpty(endingVertices), "Ending vertices cannot be null/empty.");
        errorIfAllStartingVerticesAreEndingVertices(startingVertices, endingVertices);

        Set<OntologyRelationType> relationsSet = createRelevantRelationsSet(relations);
        List<List<OntologyRelationship>> interestingPaths =
                calculateInterestingPaths(startingVertices, endingVertices, relationsSet);

        AllDirectedPaths<String, OntologyRelationship> allPaths = new AllDirectedPaths<>(ontology);
        List<GraphPath<String, OntologyRelationship>> v1v2Paths =
                allPaths.getAllPaths(startingVertices, endingVertices, true, null);
        removeInvalidPaths(relationsSet, v1v2Paths);

        interestingPaths.addAll(v1v2Paths.stream().map(GraphPath::getEdgeList).collect(Collectors.toList()));

        return interestingPaths;
    }

    private void removeInvalidPaths(Set<OntologyRelationType> relationsSet,
            List<GraphPath<String, OntologyRelationship>> v1v2Paths) {
        List<GraphPath<String, OntologyRelationship>> invalidPaths = new ArrayList<>();

        for (GraphPath<String, OntologyRelationship> path : v1v2Paths) {
            if (!relationsSet
                    .containsAll(path.getEdgeList().stream().map(e -> e.relationship).collect(Collectors.toList()))) {
                invalidPaths.add(path);
            }
        }

        v1v2Paths.removeAll(invalidPaths);
    }

    @Override
    public List<String> ancestors(Set<String> baseVertices, OntologyRelationType... relations) {
        Preconditions.checkArgument(!isNullOrEmpty(baseVertices), "Base vertices cannot be null/empty.");

        Set<String> ancestorsFound = new HashSet<>();
        if (relations.length == 0) {
            relations = new OntologyRelationType[1];
            relations[0] = OntologyRelationType.UNDEFINED;
        }

        for (String base : baseVertices) {
            for (OntologyRelationship relation : getAncestorEdges(base)) {
                if (relation.relationship.hasTransitiveType(relations)) {
                    ancestorsFound.add(relation.parent);
                }
            }
        }

        return ancestorsFound.stream().collect(Collectors.toList());
    }

    @Override public Set<OntologyRelationship> parents(String baseVertex, OntologyRelationType... relations) {
        Preconditions.checkArgument(baseVertex != null && !baseVertex.trim().isEmpty(),
                "Base vertex cannot be null or empty");

        Set<OntologyRelationType> relationsSet = createRelevantRelationsSet(relations);

        return ontology.outgoingEdgesOf(baseVertex).stream()
                .filter(ontologyRelationship -> relationsSet.contains(ontologyRelationship.relationship))
                .collect(toSet());
    }

    @Override
    public List<String> descendants(Set<String> topVertices, OntologyRelationType... relations) {
        Preconditions.checkArgument(!isNullOrEmpty(topVertices), "Top vertices cannot be null/empty.");

        Set<String> descendantsFound = new HashSet<>();
        descendantsFound.addAll(topVertices); // as with ancestors, include itself (indicating IDENTITY relationship)

        Set<OntologyRelationType> relationsSet = createRelevantRelationsSet(relations);
        descendants(topVertices, descendantsFound, relationsSet);
        return descendantsFound.stream().collect(Collectors.toList());
    }

    @Override public Set<OntologyRelationship> children(String topVertex, OntologyRelationType... relations) {
        Preconditions.checkArgument(topVertex != null && !topVertex.trim().isEmpty(),
                "Top vertex cannot be null or empty");

        Set<OntologyRelationType> relationsSet = createRelevantRelationsSet(relations);

        return ontology.incomingEdgesOf(topVertex).stream()
                .filter(ontologyRelationship -> relationsSet.contains(ontologyRelationship.relationship))
                .collect(toSet());
    }

    @Override public AncestorGraph subGraph(Set<String> baseVertices, Set<String> stopNodes,
            OntologyRelationType... relations) {
        Preconditions.checkArgument(!isNullOrEmpty(baseVertices), "Starting vertices cannot be null/empty.");

        if(stopNodes.isEmpty()){
            stopNodes.addAll(STOP_NODES);
        }

        Set<OntologyRelationType> relationsSet = createRelevantRelationsSet(relations);
        OntologyRelationType[] targetRelations = relationsSet.toArray(new OntologyRelationType[]{});

        Set<String> ancestorsFound = new HashSet<>();
        Set<OntologyRelationship> edgesFound = new HashSet<>();

        AncestorGraph ancestorGraph = new AncestorGraph(edgesFound, ancestorsFound);

        for (String base : baseVertices) {
            if(ontology.containsVertex(base)) {
                ancestorGraph.vertices.add(base);

                if (!stopNodes.contains(base)) {
                    addParentsToAncestorGraph(base, stopNodes, targetRelations, ancestorGraph);
                }
            }
        }
        return ancestorGraph;
    }

    private void addParentsToAncestorGraph(String base, Set<String> stopNodes, OntologyRelationType[] targetRelations,
            AncestorGraph ancestorGraph) {
        Set<OntologyRelationship> parents = null;
        try {
            parents = this.parents(base, targetRelations);
        } catch (Exception e) {
           return;
        }

        if (!parents.isEmpty()) {
            Set<String> parentVertices = parents.stream()
                                                .map(p -> p.parent)
                                                .collect(toSet());
            AncestorGraph parentSubGraph = this.subGraph(parentVertices, stopNodes, targetRelations);
            ancestorGraph.edges.addAll(parentSubGraph.edges);
            ancestorGraph.vertices.addAll(parentSubGraph.vertices);
        }
        ancestorGraph.edges.addAll(parents);
    }

    @Override public int hashCode() {
        int result = ontology != null ? ontology.hashCode() : 0;
        result = 31 * result + (ancestorEdgesMap != null ? ancestorEdgesMap.hashCode() : 0);
        return result;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OntologyGraph that = (OntologyGraph) o;

        if (ontology != null ? !ontology.equals(that.ontology) : that.ontology != null) {
            return false;
        }
        return ancestorEdgesMap != null ? ancestorEdgesMap.equals(that.ancestorEdgesMap) :
                that.ancestorEdgesMap == null;
    }

    private List<List<OntologyRelationship>> calculateInterestingPaths(Set<String> startingVertices,
            Set<String> endingVertices, Set<OntologyRelationType> relationsSet) {
        List<List<OntologyRelationship>> interestingPaths = new ArrayList<>();
        List<OntologyRelationship> immediateSuccessors = successorEdges(startingVertices, endingVertices, relationsSet);
        if (immediateSuccessors.size() > 0) {
            immediateSuccessors.stream()
                               .map(Collections::singletonList)
                               .forEach(interestingPaths::add);
        }
        return interestingPaths;
    }

    private Set<String> errorIfAllStartingVerticesAreEndingVertices(Set<String> baseVertices, Set<String> topVertices) {
        Set<String> startEndIntersection = new HashSet<>(baseVertices);
        startEndIntersection.retainAll(topVertices);

        if (!startEndIntersection.isEmpty()) {
            throw new IllegalArgumentException("Cannot find paths intersecting start/end vertex sets:\n"
                                                       + "\tstarting vertices: " + baseVertices
                                                       + "\tending vertices: " + topVertices);
        }

        return startEndIntersection;
    }

    private boolean vertexIsStopNode(String vertex, Set<String> stopNodes) {
        return stopNodes.contains(vertex);
    }

    private boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    private Set<OntologyRelationship> getAncestorEdges(String vertex) {
        if (!ancestorEdgesMap.containsKey(vertex)) {
            Set<OntologyRelationship> ancestorEdgesOfV = new HashSet<>();

            ancestorEdgesOfV.add(new OntologyRelationship(vertex, vertex, OntologyRelationType.IDENTITY));

            if (isNotStopNode(vertex)) {
                for (OntologyRelationship successorEdge : ontology.outgoingEdgesOf(vertex)) {
                    for (OntologyRelationship grandparentSuccessorEdge : getAncestorEdges(successorEdge.parent)) {
                        OntologyRelationship combinedRelationship =
                                OntologyRelationship.combineRelationships(successorEdge, grandparentSuccessorEdge);
                        if (combinedRelationship.relationship != OntologyRelationType.UNDEFINED) {
                            ancestorEdgesOfV.add(combinedRelationship);
                        }
                    }
                }
            }

            ancestorEdgesMap.put(vertex, ancestorEdgesOfV);
        }
        return ancestorEdgesMap.get(vertex);
    }

    private boolean isNotStopNode(String id) {
        return !STOP_NODES.contains(id);
    }

    /**
     * <p>
     * Finds the outgoing, i.e., successor, edges originating from a {@link Collection} of starting vertices,
     * whose target vertices must be in a specified {@link Collection} of vertices, and whose target vertices
     * are reached by a specified {@link Set} of {@link OntologyRelationType}s.
     * <p>
     * Note that in this context, an edge is the formal name of an {@link OntologyRelationship}.
     * @param fromVertices the starting vertices
     * @param toVertices the target vertices
     * @param relations the edge labels over which starting vertices can reach target vertices
     * @return the {@link List} of {@link OntologyRelationship} instances defining the edges between {@code
     * fromVertices}
     *         to {@code toVertices} via {@code relations}
     */
    private List<OntologyRelationship> successorEdges(
            Collection<String> fromVertices,
            Collection<String> toVertices,
            Set<OntologyRelationType> relations) {
        List<OntologyRelationship> successors = new ArrayList<>();

        fromVertices.forEach(
                from ->
                        ontology.outgoingEdgesOf(from).stream()
                                .filter(e -> relations.contains(e.relationship) && toVertices.contains(e.parent))
                                .forEach(successors::add)
        );

        return successors;
    }

    private HashSet<OntologyRelationType> createRelevantRelationsSet(OntologyRelationType... relations) {
        return new HashSet<>(Arrays.asList(relations.length == 0 ? OntologyRelationType.values() : relations));
    }

    private Set<String> getRelatives(String base, Set<OntologyRelationship> relativeEdges,
            Set<OntologyRelationType> relations) {
        Set<String> relatives = new HashSet<>();

        relativeEdges.forEach(
                e -> {
                    if (relations.contains(e.relationship)) {
                        relatives.add(getOppositeVertex(ontology, e, base));
                    }
                }
        );

        return relatives;
    }

    private void descendants(Set<String> topVertices, Set<String> currentDescendants, Set<OntologyRelationType>
            relations) {
        for (String top : topVertices) {
            Set<String> descendants = getRelatives(top, ontology.incomingEdgesOf(top), relations);

            currentDescendants.addAll(descendants);
            descendants(descendants, currentDescendants, relations);
        }
    }
}
