package org.sunbird.graph.engine.dto;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.common.Identifier;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.service.operation.Neo4JBoltSearchOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessingNode extends Node {

    private List<Relation> newRelations = new ArrayList<>();
    private List<Relation> deletedRelations = new ArrayList<>();
    private Map<String, Object> externalData;

    public ProcessingNode(Node node, Map<String, Object> externalData) {
        setId(node.getId());
        setGraphId(node.getGraphId());
        setIdentifier(node.getIdentifier());
        setMetadata(node.getMetadata());
        setInRelations(node.getInRelations());
        setOutRelations(node.getOutRelations());
        if (CollectionUtils.isNotEmpty(getInRelations())) {
            newRelations.addAll(getInRelations());
        }
        if (CollectionUtils.isNotEmpty(getOutRelations())) {
            newRelations.addAll(getOutRelations());
        }
        this.externalData = externalData;
    }

    public ProcessingNode(String identifier, Node node, Map<String, Object> externalData) {
        setId(node.getId());
        setGraphId(node.getGraphId());
        setIdentifier(identifier);
        setIdentifier(node.getIdentifier());
        setMetadata(node.getMetadata());
        setInRelations(node.getInRelations());
        setOutRelations(node.getOutRelations());
        this.externalData = externalData;
        Node dbNode = getDBNode();
        // TODO: set metadata and relations.
        setMetadata(dbNode);
    }

    public Node getNode() {
        return (Node) this;
    }

    private void setMetadata(Node dbNode) {
        Map<String, Object> dbMetadata = dbNode.getMetadata();
        for (Map.Entry<String, Object> entry: dbMetadata.entrySet()) {
            if (!getMetadata().containsKey(entry.getKey())) {
                getMetadata().put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setRelations(Node dbNode) {

    }

    private Node getDBNode() {
        Node node = Neo4JBoltSearchOperations.getNodeByUniqueId(getGraphId(), getIdentifier(), false, null);
        // TODO: throw client exception if dbNode not exists.
        // TODO: validate nodeType, objectType
        return node;
    }

    public List<Relation> getNewRelations() {
        return newRelations;
    }

    public List<Relation> getDeletedRelations() {
        return deletedRelations;
    }

    public Map<String, Object> getExternalData() {
        return externalData;
    }
}
