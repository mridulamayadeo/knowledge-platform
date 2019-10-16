package org.sunbird.graph.engine

import org.sunbird.common.dto.{Request, Response, ResponseHandler}
import org.sunbird.graph.nodes.DataNode

import scala.concurrent.{ExecutionContext, Future}

object NodeManager {

    val graphId = "domain"

    @throws[Exception]
    def createDataNode(request: Request)(implicit ec: ExecutionContext): Future[Response] = {
        request.getContext.put("graph_id", graphId)
        request.getContext.put("version", "1.0")
        DataNode.create(request).map(node => {
            val response = ResponseHandler.OK()
            response.put("node_id", node.getIdentifier)
            response.put("versionKey", node.getMetadata.get("versionKey"))
            response
        })
    }
}