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

    private final DirectedAcyclicGraph<CmdiVertex, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
    private final Map<String, CmdiVertex> vertexIdMap = new HashMap<>();
    private final Set<CmdiVertex> foundVerticesSet = new HashSet<>();
    private Set<String> occurringMdSelfLinks = new HashSet<>();

    /**
     * Adds new vertex to graph, used to remember all CMDI files that were
     * actually seen
     *
     * @param mdSelfLink extracted MdSelfLink from CMDI file
     */
    public synchronized void addResource(String mdSelfLink) {
        String normalizedMdSelfLink = StringUtils.normalizeIdString(mdSelfLink);
        if (!vertexIdMap.containsKey(normalizedMdSelfLink)) {
            CmdiVertex newVertex = new CmdiVertex(normalizedMdSelfLink);
            vertexIdMap.put(normalizedMdSelfLink, newVertex);
        }

        if (!foundVerticesSet.contains(vertexIdMap.get(normalizedMdSelfLink))) {
            graph.addVertex(vertexIdMap.get(normalizedMdSelfLink));
            foundVerticesSet.add(vertexIdMap.get(normalizedMdSelfLink));
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
    public synchronized void addEdge(String sourceVertexId, String targetVertexId) {
        String normalizedSourceVertexId = StringUtils.normalizeIdString(sourceVertexId);
        String normalizedTargetVertexId = StringUtils.normalizeIdString(targetVertexId);

        // Omit adding edges to nodes that do not occur in the harvester set
        if (!occurringMdSelfLinks.contains(normalizedSourceVertexId)) {
            return;
        }

        // add vertices
        if (!vertexIdMap.containsKey(normalizedSourceVertexId)) {
            CmdiVertex sourceVertex = new CmdiVertex(normalizedSourceVertexId);
            vertexIdMap.put(normalizedSourceVertexId, sourceVertex);
            graph.addVertex(sourceVertex);
        }

        if (!vertexIdMap.containsKey(normalizedTargetVertexId)) {
            CmdiVertex targetVertex = new CmdiVertex(normalizedTargetVertexId);
            vertexIdMap.put(normalizedTargetVertexId, targetVertex);
            graph.addVertex(targetVertex);
        }

        // add edge
        try {
            graph.addEdge(vertexIdMap.get(normalizedSourceVertexId), vertexIdMap.get(normalizedTargetVertexId));
            updateDepthValues(vertexIdMap.get(normalizedSourceVertexId), new HashSet<CmdiVertex>());
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
    private void updateDepthValues(CmdiVertex startVertex, Set<CmdiVertex> alreadySeenVerticesSet) {
        alreadySeenVerticesSet = new HashSet<>();
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

    private void backwardUpdate(CmdiVertex updateVertex, int newDepth, Set<CmdiVertex> alreadySeenVerticesSet) {
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

    public synchronized DirectedAcyclicGraph<CmdiVertex, DefaultEdge> getResourceGraph() {
        return graph;
    }

    public synchronized Set<CmdiVertex> getFoundVertices() {
        return foundVerticesSet;
    }

    public synchronized CmdiVertex getVertex(String vertexId) {
        return vertexIdMap.get(vertexId);
    }

    public synchronized Map<String, CmdiVertex> getVertexIdMap() {
        return vertexIdMap;
    }

    /**
     * Get all vertices that are source of an edge where targetVertex is target.
     * In other words get all resource vertices that are part of resource
     * targetVertex.
     *
     * @param targetVertex
     * @return List of vertices that are source of an edge where targetVertex is
     * target
     */
    public synchronized List<String> getIncomingVertexNames(CmdiVertex targetVertex) {
        List<String> vertexNamesList = new ArrayList<>();
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
     * In other words get all resource vertices of which resource sourceVertex
     * is part of.
     *
     * @param sourceVertex
     * @return List of vertices that are target of an edge where sourceVertex is
     * source
     */
    public synchronized List<String> getOutgoingVertexNames(CmdiVertex sourceVertex) {
        List<String> vertexNamesList = new ArrayList<>();
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
     * Set set of all MdSelfLinks that actually occur in the processed
     * collection. Will be used to omit the creation of edges to non-existing
     * nodes.
     *
     * @param occurringMdSelfLinks
     */
    public synchronized void setOccurringMdSelfLinks(Set<String> occurringMdSelfLinks) {
        this.occurringMdSelfLinks = occurringMdSelfLinks;
    }

    /**
     * Get some statistics about the resource hierarchy graph
     *
     * @param maxBrokenEdges maximal number of examples of broken edges (=source
     * or target vertex is missing) included in the output
     * @return some statistics about the resource hierarchy graph
     */
    public String printStatistics(int maxBrokenEdges) {
        StringBuilder sb = new StringBuilder();

        // vertex + edge sets size
        sb.append("vertices: ").append(foundVerticesSet.size()).append(", edges: ").append(graph.edgeSet().size());

        // count broken + valid edges
        int count = 0;
        Iterator<DefaultEdge> edgeIter = graph.edgeSet().iterator();
        HashSet<DefaultEdge> brokenEdgeSet = new HashSet<>();
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

    /**
     * Stores all information of a vertex (=CMDI file) in the CMDI hierarchy
     * graph
     *
     * @author Thomas Eckart
     */
    public static class CmdiVertex {

        private final String id;
        private int hierarchyWeight = 0;
        private boolean wasImported = false;

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

        public void setWasImported(boolean wasImported) {
            this.wasImported = wasImported;
        }

        public boolean getWasImported() {
            return wasImported;
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

}
