/*
 * Copyright (C) 2015 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores relationship graph between CMDI files based on ResourceProxys.
 * Generates weight for every file based on its position in the hierarchy
 * (weight = number of edges to a top node)
 *
 * @author Thomas Eckart
 */
public class ResourceStructureGraph {

    protected final static Logger LOG = LoggerFactory.getLogger(ResourceStructureGraph.class);

    private static DirectedAcyclicGraph<CmdiVertex, DefaultEdge> graph = new DirectedAcyclicGraph<CmdiVertex, DefaultEdge>(
            DefaultEdge.class);
    private static Map<String, CmdiVertex> vertexIdMap = new HashMap<String, CmdiVertex>();
    private static Set<CmdiVertex> foundVerticesSet = new HashSet<CmdiVertex>();

    /**
     * Adds new vertex to graph, used to remember all CMDI files that were
     * actually seen
     *
     * @param mdSelfLink extracted MdSelfLink from CMDI file
     */
    public static void addResource(String mdSelfLink) {
        if (!vertexIdMap.containsKey(mdSelfLink)) {
            CmdiVertex newVertex = new CmdiVertex(StringUtils.normalizeIdString(mdSelfLink));
            vertexIdMap.put(mdSelfLink, newVertex);
        }

        if (!foundVerticesSet.contains(vertexIdMap.get(mdSelfLink))) {
            graph.addVertex(vertexIdMap.get(mdSelfLink));
            foundVerticesSet.add(vertexIdMap.get(mdSelfLink));
        } else {
            LOG.debug("Duplicate resource vertex mdSelfLink: " + mdSelfLink);
        }
    }

    /**
     * Add new edge to graph
     *
     * @param sourceVertexId source vertex ID (=isPart)
     * @param targetVertexId target vertex ID (=hasPart)
     */
    public static void addEdge(String sourceVertexId, String targetVertexId) {
        // add vertices
        if (!vertexIdMap.containsKey(sourceVertexId)) {
            CmdiVertex sourceVertex = new CmdiVertex(StringUtils.normalizeIdString(sourceVertexId));
            vertexIdMap.put(sourceVertexId, sourceVertex);
            graph.addVertex(sourceVertex);
        }

        if (!vertexIdMap.containsKey(targetVertexId)) {
            CmdiVertex targetVertex = new CmdiVertex(StringUtils.normalizeIdString(targetVertexId));
            vertexIdMap.put(targetVertexId, targetVertex);
            graph.addVertex(targetVertex);
        }

        // add edge
        try {
            graph.addEdge(vertexIdMap.get(sourceVertexId), vertexIdMap.get(targetVertexId));
            updateDepthValues(vertexIdMap.get(sourceVertexId), new HashSet<CmdiVertex>());
        } catch (IllegalArgumentException cfe) {
            // was a cycle -> ignore
            LOG.debug("Found cycle\t" + sourceVertexId + "\t" + targetVertexId);
        }
    }

    /**
     * Update depth weights for startVertex and related vertices (in both
     * hierarchy directions)
     *
     * @param startVertex
     * @param alreadySeenVerticesSet set of already seen vertices to avoid
     * infinite loops for cycles
     */
    private static void updateDepthValues(CmdiVertex startVertex, Set<CmdiVertex> alreadySeenVerticesSet) {
        alreadySeenVerticesSet = new HashSet<CmdiVertex>();
        alreadySeenVerticesSet.add(startVertex);

        // upwards, is part of other resource -> use decremented minimal value
        Set<DefaultEdge> outgoingEdgeSet = graph.outgoingEdgesOf(startVertex);
        if (!outgoingEdgeSet.isEmpty()) {
            Iterator<DefaultEdge> edgeIter = outgoingEdgeSet.iterator();
            CmdiVertex maxVertex = new CmdiVertex("DUMMY");
            maxVertex.setHierarchyWeight(1);
            while (edgeIter.hasNext()) {
                CmdiVertex targetVertex = graph.getEdgeTarget(edgeIter.next());
                if (targetVertex.getHierarchyWeight() <= maxVertex.getHierarchyWeight()) {
                    maxVertex = targetVertex;
                }
            }
            if (maxVertex.getHierarchyWeight() <= startVertex.getHierarchyWeight()) {
                LOG.debug("UP UPDATE  \t" + startVertex.getId() + "\t" + maxVertex.getId() + "\t"
                        + startVertex.getHierarchyWeight() + " --> " + (maxVertex.getHierarchyWeight() - 1));
                alreadySeenVerticesSet.add(maxVertex);
                startVertex.setHierarchyWeight(maxVertex.getHierarchyWeight() - 1);
            }
        }

        // downwards, has other resources as part -> decrement their depth value if smaller
        Set<DefaultEdge> incomingEdgeSet = graph.incomingEdgesOf(startVertex);
        if (!incomingEdgeSet.isEmpty()) {
            Iterator<DefaultEdge> edgeIter = incomingEdgeSet.iterator();
            while (edgeIter.hasNext()) {
                backwardUpdate(graph.getEdgeSource(edgeIter.next()), startVertex.getHierarchyWeight() - 1, alreadySeenVerticesSet);
            }
        }
    }

    private static void backwardUpdate(CmdiVertex updateVertex, int newDepth, Set<CmdiVertex> alreadySeenVerticesSet) {
        if (!alreadySeenVerticesSet.contains(updateVertex) && updateVertex.getHierarchyWeight() > newDepth) {
            alreadySeenVerticesSet.add(updateVertex);
            LOG.debug("DOWN UPDATE\t" + updateVertex.getId() + "\t" + updateVertex.getHierarchyWeight() + " --> "
                    + newDepth);
            updateVertex.setHierarchyWeight(newDepth);

            Set<DefaultEdge> incomingEdgeSet = graph.incomingEdgesOf(updateVertex);
            if (!incomingEdgeSet.isEmpty()) {
                Iterator<DefaultEdge> edgeIter = incomingEdgeSet.iterator();
                while (edgeIter.hasNext()) {
                    backwardUpdate(graph.getEdgeSource(edgeIter.next()), newDepth - 1, alreadySeenVerticesSet);
                }
            }
        }
    }

    public static DirectedAcyclicGraph<CmdiVertex, DefaultEdge> getResourceGraph() {
        return graph;
    }

    public static Set<CmdiVertex> getFoundVertices() {
        return foundVerticesSet;
    }

    /**
     * Get all vertices that are source of an edge where targetVertex is target.
     * In other words get all vertices that are part of targetVertex.
     *
     * @param targetVertex
     * @return List of vertices that are source of an edge where targetVertex is
     * target
     */
    public static List<String> getIncomingVertexNames(CmdiVertex targetVertex) {
        List<String> vertexNamesList = new ArrayList<String>();
        Set<DefaultEdge> incomingEdges = graph.incomingEdgesOf(targetVertex);
        Iterator<DefaultEdge> edgeIter = incomingEdges.iterator();
        while (edgeIter.hasNext()) {
            DefaultEdge edge = edgeIter.next();
            if (getFoundVertices().contains(graph.getEdgeSource(edge))) {
                vertexNamesList.add(graph.getEdgeSource(edge).getId());
            }
        }

        return vertexNamesList;
    }

    /**
     * Get all vertices that are target of an edge where sourceVertex is source.
     * In other words get all vertices of which sourceVertex is part of.
     *
     * @param sourceVertex
     * @return List of vertices that are target of an edge where sourceVertex is
     * source
     */
    public static List<String> getOutgoingVertexNames(CmdiVertex sourceVertex) {
        List<String> vertexNamesList = new ArrayList<String>();
        Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(sourceVertex);
        Iterator<DefaultEdge> edgeIter = outgoingEdges.iterator();
        while (edgeIter.hasNext()) {
            DefaultEdge edge = edgeIter.next();
            if (getFoundVertices().contains(graph.getEdgeTarget(edge))) {
                vertexNamesList.add(graph.getEdgeTarget(edge).getId());
            }
        }

        return vertexNamesList;
    }

    /**
     * Reset resource hierarchy graph (= deleting vertices + edges)
     */
    public static void clearResourceGraph() {
        vertexIdMap = new HashMap<String, CmdiVertex>();
        foundVerticesSet = new HashSet<CmdiVertex>();
        graph = new DirectedAcyclicGraph<CmdiVertex, DefaultEdge>(DefaultEdge.class);
    }

    /**
     * Get some statistics about the resource hierarchy graph
     *
     * @param maxBrokenEdges maximal number of examples of broken edges (=source
     * or target vertex is missing) included in the output
     * @return some statistics about the resource hierarchy graph
     */
    public static String printStatistics(int maxBrokenEdges) {
        StringBuilder sb = new StringBuilder();

        // vertex + edge sets size
        sb.append("vertices: ").append(foundVerticesSet.size()).append(", edges: ").append(graph.edgeSet().size());

        // count broken + valid edges
        int count = 0;
        Iterator<DefaultEdge> edgeIter = graph.edgeSet().iterator();
        HashSet<DefaultEdge> brokenEdgeSet = new HashSet<DefaultEdge>();
        while (edgeIter.hasNext()) {
            DefaultEdge edge = edgeIter.next();
            if (foundVerticesSet.contains(graph.getEdgeTarget(edge)) && foundVerticesSet.contains(graph.getEdgeSource(edge))) {
                count++;
            } else {
                brokenEdgeSet.add(edge);
            }
        }
        sb.append(", broken edges: ").append(brokenEdgeSet.size());

        // show some broken edges
        if (maxBrokenEdges != 0) {
            int counter = 0;
            Iterator<DefaultEdge> brokenEdgeIter = brokenEdgeSet.iterator();
            while (brokenEdgeIter.hasNext()) {
                if ((++counter) <= maxBrokenEdges) {
                    DefaultEdge brokenEdge = brokenEdgeIter.next();
                    LOG.debug("Broken edge: " + graph.getEdgeSource(brokenEdge) + " --> "
                            + graph.getEdgeTarget(brokenEdge));
                } else {
                    break;
                }
            }
        }

        // valid edges
        sb.append(", valid edges: ").append(count);
        return sb.toString();
    }
}

/**
 * Stores all information of a vertex (=CMDI file) in the CMDI hierarchy graph
 *
 * @author Thomas Eckart
 */
class CmdiVertex {

    private final String id;
    private int hierarchyWeight = 0;

    public CmdiVertex(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setHierarchyWeight(int hierarchyWeight) {
        this.hierarchyWeight = hierarchyWeight;
    }

    public int getHierarchyWeight() {
        return hierarchyWeight;
    }

    @Override
    public String toString() {
        return id + " (" + hierarchyWeight + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CmdiVertex)) {
            return false;
        }
        CmdiVertex other = (CmdiVertex) obj;
        if (this.id.equals(other.id)) {
            return true;
        }
        if (this.id == null && other.id != null) {
            return false;
        }
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
