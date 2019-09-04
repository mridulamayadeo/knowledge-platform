package org.sunbird.graph.util;

import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.dac.model.Filter;
import org.sunbird.graph.dac.model.MetadataCriterion;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.SearchConditions;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.service.operation.Neo4JBoltSearchOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class provides utility methods for node validation
 * @author Kumar Gauraw
 */
public class NodeValidator {

    /**
     * This method validates whether given identifiers exist in the given graph or not.
     * @param graphId
     * @param identifiers
     * @return List<Node>
     */
    public static List<Node> validate(String graphId, List<String> identifiers) {
        List<Map<String, Object>> result;
        List<Node> nodes = getDataNodes(graphId, identifiers);
        if (nodes.size() != identifiers.size()) {
            List<String> invalidIds = identifiers.stream().filter(id -> nodes.stream().noneMatch(node -> node.getIdentifier().equals(id)))
                    .collect(Collectors.toList());
            throw new ResourceNotFoundException(GraphEngineErrorCodes.ERR_INVALID_NODE.name(), "Node Not Found With Identifier " + invalidIds);
        } else {
            return nodes;
        }
    }

    /**
     * This method fetch and return list of Node object for given graph & identifiers
     * @param graphId
     * @param identifiers
     * @return List<Node>
     */
    private static List<Node> getDataNodes(String graphId, List<String> identifiers) {
        List<Node> nodes = new ArrayList<>();
        SearchCriteria searchCriteria = new SearchCriteria();
        MetadataCriterion mc = null;
        if (identifiers.size() == 1) {
            mc = MetadataCriterion
                    .create(Arrays.asList(new Filter("identifier", SearchConditions.OP_EQUAL, identifiers.get(0))));
        } else {
            mc = MetadataCriterion.create(Arrays.asList(new Filter("identifier", SearchConditions.OP_IN, identifiers)));
        }
        searchCriteria.addMetadata(mc);
        searchCriteria.setCountQuery(false);
        try {
            nodes = Neo4JBoltSearchOperations.getNodeByUniqueIds(graphId, searchCriteria);
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_PROCESSING_ERROR.name(), "Unable To Fetch Nodes From Graph. Exception is: " + e.getMessage());

        }
        return nodes;
    }
}
