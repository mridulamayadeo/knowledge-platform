package org.sunbird.graph.dac.mgr;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;

public interface IGraphDACSearchMgr {

	Response getNodeById(Request request);

	Response getNodeByUniqueId(Request request);

	Response getNodesByUniqueIds(Request request);

	Response checkCyclicLoop(Request request);
    

}
