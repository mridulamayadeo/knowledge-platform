package org.sunbird.graph.service.request.validator;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.service.common.NodeUpdateMode;
import org.sunbird.telemetry.logger.TelemetryManager;

//import org.sunbird.graph.cache.util.RedisStoreUtil;

public class Neo4jBoltValidator extends Neo4JBoltDataVersionKeyValidator {

	public boolean validateUpdateOperation(String graphId, Node node) {

		String nodeId = node.getIdentifier();
		String nodeType = node.getNodeType();
		String nodeObjType = node.getObjectType();

		// return as validate if node type is other than data node
		if (!nodeType.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name()))
			return true;

		String versionCheckMode = null; // RedisStoreUtil.getNodeProperty(graphId, nodeObjType, GraphDACParams.versionCheckMode.name());
		TelemetryManager.log(
				"Version Check Mode in Local Cache: " + versionCheckMode + " for Object Type: " + node.getObjectType());

		if (StringUtils.isNotBlank(versionCheckMode)) {// from Local cache
			// versionCheckMode is from Local cache, check versionKey in Redis
			// or graph
			if (!StringUtils.equalsIgnoreCase(NodeUpdateMode.OFF.name(), versionCheckMode)) {
				String storedVersionKey = null;//RedisStoreUtil.getNodeProperty(graphId, nodeId, GraphDACParams.versionKey.name());
				return validateUpdateOperation(graphId, node, versionCheckMode, storedVersionKey);
			}
		} else {
			// from graph - fall back
			// check both versionCheckMode and versionKey in graph
			return validateUpdateOperation(graphId, node, versionCheckMode, null);
		}

		return true;
	}

}
