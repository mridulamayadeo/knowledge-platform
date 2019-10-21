package org.sunbird.graph.dac.mgr.impl;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.GraphDACErrorCodes;
import org.sunbird.graph.dac.mgr.IGraphDACSearchMgr;
import org.sunbird.graph.service.operation.Neo4JBoltSearchOperations;


import java.util.Map;

public class Neo4JBoltSearchMgrImpl extends BaseDACManager implements IGraphDACSearchMgr {

    @Override
	public Response checkCyclicLoop(Request request) {
        String graphId = (String) request.getContext().get(GraphDACParams.graph_id.name());
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
