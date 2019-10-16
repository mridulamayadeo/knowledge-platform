package org.sunbird.graph.nodes

import java.util

import org.apache.commons.collections4.{CollectionUtils, MapUtils}
import org.sunbird.common.JsonUtils
import org.sunbird.common.dto.{Request, Response}
import org.sunbird.graph.dac.model.{Node, Relation}
import org.sunbird.graph.engine.RelationManager
import org.sunbird.graph.external.ExternalPropsManager
import org.sunbird.graph.relations.{IRelation, RelationHandler}
import org.sunbird.graph.schema.{DefinitionFactory, DefinitionNode}
import org.sunbird.graph.service.operation.NodeAsyncOperations
import org.sunbird.parseq.Task

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}


object DataNode {
    @throws[Exception]
    def create(request: Request)(implicit ec: ExecutionContext): Future[Node] = {
        val graphId: String = request.getContext.get("graph_id").asInstanceOf[String]
        val version: String = request.getContext.get("version").asInstanceOf[String]
        val definition = DefinitionFactory.getDefinition(graphId, request.getObjectType, version)
        val validationResult = validate(request.getRequest, definition)
        validationResult.map(processingNode => {
            val response = NodeAsyncOperations.addNode(graphId, processingNode.getNode)
            response.map(result => {
                val futureList = Task.parallel[Response](
                    saveExternalProperties(processingNode.getIdentifier, processingNode.getExternalData, request.getContext, request.getObjectType),
                    updateRelations(graphId, processingNode, request.getContext))
                futureList.map(list => result)
            }).flatMap(f => f)
        }).flatMap(f => f)
    }

    @throws[Exception]
    private def validate(input: util.Map[String, AnyRef], definition: DefinitionNode)(implicit ec: ExecutionContext): Future[Node] = {
        val node = definition.getNode(input)
        definition.validate(node)
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
    
    private def updateRelations(graphId: String, node: Node, context: util.Map[String, AnyRef])(implicit ec: ExecutionContext) : Future[Response] = {
        val relations: util.List[Relation] = node.getAddedRelations
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