package org.sunbird.graph.dac.mgr.impl;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.enums.GraphDACParams;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.dac.enums.GraphDACErrorCodes;
import org.sunbird.graph.dac.mgr.IGraphDACSearchMgr;
import org.sunbird.graph.dac.model.Filter;
import org.sunbird.graph.dac.model.MetadataCriterion;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.SearchConditions;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.service.operation.Neo4JBoltSearchOperations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Neo4JBoltSearchMgrImpl extends BaseDACManager implements IGraphDACSearchMgr {
	
    @Override
	public Response getNodeById(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        Long nodeId = (Long) request.get(GraphDACParams.node_id.name());
        Boolean getTags = (Boolean) request.get(GraphDACParams.get_tags.name());
        if (!validateRequired(nodeId))
            throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        try {
			Node node = Neo4JBoltSearchOperations.getNodeById(graphId, nodeId, getTags, request);
			return OK(GraphDACParams.node.name(), node);
        } catch (Exception e) {
			return ERROR(e);
        }
    }

    @Override
	public Response getNodeByUniqueId(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        Boolean getTags = (Boolean) request.get(GraphDACParams.get_tags.name());
        if (!validateRequired(nodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				Node node = Neo4JBoltSearchOperations.getNodeByUniqueId(graphId, nodeId, getTags, request);
				return OK(GraphDACParams.node.name(), node);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }

	@Override
	public Response getNodesByUniqueIds(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> nodeIds = (List<String>) request.get(GraphDACParams.node_ids.name());
		if (!validateRequired(nodeIds)) {
			throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_LIST_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing");
		} else {
			SearchCriteria searchCriteria = new SearchCriteria();
			MetadataCriterion mc = null;
			if (nodeIds.size() == 1)
				mc = MetadataCriterion
						.create(Arrays.asList(new Filter("identifier", SearchConditions.OP_EQUAL, nodeIds.get(0))));
			else
				mc = MetadataCriterion.create(Arrays.asList(new Filter("identifier", SearchConditions.OP_IN, nodeIds)));
			
			searchCriteria.addMetadata(mc);
			searchCriteria.setCountQuery(false);
            try {
				List<Node> nodes = Neo4JBoltSearchOperations.getNodeByUniqueIds(graphId, searchCriteria);
				return OK(GraphDACParams.node_list.name(), nodes);
            } catch (Exception e) {
				return ERROR(e);
            }
		}
	}

    @Override
	public Response checkCyclicLoop(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CHECK_LOOP_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				Map<String, Object> voMap = Neo4JBoltSearchOperations.checkCyclicLoop(graphId, startNodeId,
						relationType, endNodeId, request);
				return OK(voMap);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }

}
