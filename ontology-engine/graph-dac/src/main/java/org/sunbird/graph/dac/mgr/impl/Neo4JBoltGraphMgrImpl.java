package org.sunbird.graph.dac.mgr.impl;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.enums.GraphDACParams;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.dac.enums.GraphDACErrorCodes;
import org.sunbird.graph.dac.mgr.IGraphDACGraphMgr;
import org.sunbird.graph.service.operation.Neo4JBoltGraphOperations;


public class Neo4JBoltGraphMgrImpl extends BaseDACManager implements IGraphDACGraphMgr {

	@Override
	public Response addRelation(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeId, relationType, endNodeId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {

			try {
				Neo4JBoltGraphOperations.createRelation(graphId, startNodeId, endNodeId, relationType, request);
				return OK(GraphDACParams.graph_id.name(), graphId);
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

}
