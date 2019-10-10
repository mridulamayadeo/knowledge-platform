package org.sunbird.graph.nodes

import java.util

import org.apache.commons.collections4.{CollectionUtils, MapUtils}
import org.apache.commons.lang3.StringUtils
import org.sunbird.common.dto.{Request, Response, ResponseHandler}
import org.sunbird.common.exception.ResponseCode
import org.sunbird.graph.dac.model.{Node, Relation}
import org.sunbird.graph.engine.RelationManager
import org.sunbird.graph.engine.dto.ProcessingNode
import org.sunbird.graph.external.ExternalPropsManager
import org.sunbird.graph.model.IRelation
import org.sunbird.graph.model.relation.RelationHandler
import org.sunbird.graph.schema.{DefinitionFactory, DefinitionNode}
import org.sunbird.graph.service.operation.NodeAsyncOperations
import org.sunbird.parseq.Task

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}


object DataNode {
    @throws[Exception]
    def create(request: Request)(implicit ec: ExecutionContext): Future[Node] = {
        val graphId:String = request.getContext.get("graph_id").asInstanceOf[String]
        val version:String = request.getContext.get("version").asInstanceOf[String]
        val definition = DefinitionFactory.getDefinition(graphId, request.getObjectType, version)
        val validationResult = validate(request.getRequest, definition)
        val response = NodeAsyncOperations.addNode(graphId, validationResult.getNode)
        response.map(result => {
            val futureList = Task.parallel[Response](
                saveExternalProperties(validationResult.getIdentifier, validationResult.getExternalData, request.getContext, request.getObjectType),
                updateRelations(graphId, validationResult, request.getContext))
            futureList.map(list => result)
        }).flatMap(f => f)

    }

    @throws[Exception]
    private def validate(input: util.Map[String, AnyRef], definition: DefinitionNode): ProcessingNode = {
        val node = definition.getNode(input)
        definition.validate(node)
        node
    }

    private def saveExternalProperties(identifier: String, externalProps: util.Map[String, AnyRef], context: util.Map[String, AnyRef], objectType: String)(implicit ec: ExecutionContext): Future[Response] = {
        if (MapUtils.isNotEmpty(externalProps)) {
            externalProps.put("identifier", identifier)
            val request = new Request(context, externalProps, "", objectType)
            ExternalPropsManager.saveProps(request)
        } else {
            Future(new Response)
        }
    }
    
    private def updateRelations(graphId: String, node: ProcessingNode, context: util.Map[String, AnyRef])(implicit ec: ExecutionContext) : Future[Response] = {
        val relations: util.List[Relation] = node.getNewRelations
        if (CollectionUtils.isNotEmpty(relations)) {
            val relationList: List[IRelation] = relations.toList.map(relation =>
                RelationHandler.getRelation(graphId, node.getRelationNode(relation.getStartNodeId),
                    relation.getRelationType, node.getRelationNode(relation.getEndNodeId), new util.HashMap()))
            val request: Request = new Request
            request.setContext(context)
            request.put("relations", relationList)
            RelationManager.createNewRelations(request)
        } else {
            Future(new Response)
        }
    }
}