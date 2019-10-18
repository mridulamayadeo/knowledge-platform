package org.sunbird.graph.relations

import org.apache.commons.lang3.StringUtils
import org.sunbird.common.dto.{Request, ResponseHandler}
import org.sunbird.common.exception.ServerException
import org.sunbird.graph.common.enums.GraphDACParams
import org.sunbird.graph.dac.mgr.impl.{Neo4JBoltGraphMgrImpl, Neo4JBoltSearchMgrImpl}
import org.sunbird.graph.dac.model.Node
import org.sunbird.graph.exception.GraphRelationErrorCodes
import org.sunbird.graph.schema.DefinitionFactory
//import org.sunbird.graph.service.operation.Neo4JBoltSearchOperations

abstract class AbstractRelation(graphId: String, startNode: Node, endNode: Node, metadata: java.util.Map[String, AnyRef]) extends IRelation {

    protected val graphMgr = new Neo4JBoltGraphMgrImpl
    protected val searchMgr = new Neo4JBoltSearchMgrImpl

    override def createRelation(req: Request):String = {
        val request = new Request(req)
        request.put(GraphDACParams.start_node_id.name, startNode.getIdentifier)
        request.put(GraphDACParams.relation_type.name, getRelationType)
        request.put(GraphDACParams.end_node_id.name, endNode.getIdentifier)
        request.put(GraphDACParams.metadata.name, metadata)

        val res = graphMgr.addRelation(request)
        if (ResponseHandler.checkError(res)) {
            ResponseHandler.getErrorMessage(res)
        } else {
            null
        }
    }

    def validateNodeTypes(node: Node, nodeTypes: List[String]): String = {
        if(!nodeTypes.contains(startNode.getNodeType)) "Node " + node.getIdentifier + " is not a " + nodeTypes
        else null
    }

    def validateObjectTypes(startNodeObjectType: String, endNodeObjectType: String): String = {
        if(StringUtils.isNotBlank(startNodeObjectType) && StringUtils.isNotBlank(endNodeObjectType)) {
            val objectTypes = DefinitionFactory.getDefinition("domain", startNodeObjectType, "1.0").getOutRelationObjectTypes
            if(!objectTypes.contains(getRelationType + ":" + endNodeObjectType)) getRelationType + " is not allowed between " + startNodeObjectType + " and " + endNodeObjectType
            else null
        }
        else null
    }

    def checkCycle(req: Request): String = try {
        val request = new Request(req)
        request.put(GraphDACParams.start_node_id.name, this.endNode.getIdentifier)
        request.put(GraphDACParams.relation_type.name, getRelationType)
        request.put(GraphDACParams.end_node_id.name, this.startNode.getIdentifier)
//        Neo4JBoltSearchOperations.checkCyclicLoop(graphId, this.endNode.getIdentifier, getRelationType(),this.startNode.getIdentifier, request);
        val res = searchMgr.checkCyclicLoop(request)
        if (ResponseHandler.checkError(res)) ResponseHandler.getErrorMessage(res)
        else {
            val loop = res.get(GraphDACParams.loop.name).asInstanceOf[Boolean]
            if (null != loop && loop.booleanValue) {
                val msg = res.get(GraphDACParams.message.name).asInstanceOf[String]
                msg
            }
            else if (StringUtils.equals(startNode.getIdentifier, endNode.getIdentifier)) "Relation '" + getRelationType + "' cannot be created between: " + startNode.getIdentifier + " and " + endNode.getIdentifier
            else null
        }
    } catch {
        case e: Exception =>
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name, "Error occured while validing the relation", e)
    }
}
