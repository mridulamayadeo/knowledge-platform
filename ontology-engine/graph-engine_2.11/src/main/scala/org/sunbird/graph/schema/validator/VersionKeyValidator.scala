package org.sunbird.graph.schema.validator

import java.util.Date

import org.apache.commons.lang3.StringUtils
import org.sunbird.common.exception.{ClientException, ResponseCode}
import org.sunbird.common.{DateUtils, Platform}
import org.sunbird.graph.common.enums.GraphDACParams
import org.sunbird.graph.dac.enums.SystemNodeTypes
import org.sunbird.graph.dac.model.Node
import org.sunbird.graph.schema.IDefinitionNode
import org.sunbird.graph.service.common.{DACConfigurationConstants, NodeUpdateMode}
import org.sunbird.graph.service.operation.SearchAsyncOperations

import scala.concurrent.{ExecutionContext, Future}

trait VersionKeyValidator extends IDefinitionNode {

    private val graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY)

    @throws[Exception]
    abstract override def validate(node: Node, operation: String)(implicit ec: ExecutionContext): Future[Node] = {
        if(!(operation.equalsIgnoreCase("create"))){
            isValidVersionkey(node).map(isValid => {
                if(!isValid)throw new ClientException(ResponseCode.CLIENT_ERROR.name, "Invalid version Key")
                else super.validate(node, operation)
            }).flatMap(f => f)
        } else {
            super.validate(node, operation)
        }
    }

    def isValidVersionkey(node: Node)(implicit ec: ExecutionContext): Future[Boolean] =  {
        val versionCheckMode = {
            if(schemaValidator.getConfig.hasPath("versionCheckMode")) schemaValidator.getConfig.getString("versionCheckMode")
            else NodeUpdateMode.OFF.name
        }
        if(StringUtils.equalsIgnoreCase(NodeUpdateMode.OFF.name, versionCheckMode)) {
            Future{true}
        }
        else{
            if (node.getNodeType.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name)) {
                val storedVersionKey: String = null //RedisStoreUtil.getNodeProperty(graphId, nodeId, GraphDACParams.versionKey.name());
                validateUpdateOperation(node.getGraphId, node, storedVersionKey)
            }else{
                Future{true}
            }
        }
    }

    def validateUpdateOperation(getGraphId: String, node: Node, storedVersionKey: String)(implicit ec: ExecutionContext): Future[Boolean] = {
        val versionKey: String = node.getMetadata.get(GraphDACParams.versionKey.name).asInstanceOf[String]
        if(StringUtils.isBlank(versionKey))
            throw new ClientException("BLANK_VERSION", "Error! Version Key cannot be Blank. | [Node Id: " + node.getIdentifier + "]")

        if (StringUtils.equals(graphPassportKey, versionKey)) {
            node.getMetadata.put(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name, DateUtils.formatCurrentDate)
            Future{true}
        } else {
            val graphVersionKey = {
                if(StringUtils.isBlank(storedVersionKey)){
                    getVersionKeyFromDB(node.getIdentifier, node.getGraphId)
                }else{
                    Future{storedVersionKey}
                }
            }

            graphVersionKey.map(key => {
                StringUtils.equalsIgnoreCase(versionKey, key)
            })
        }
    }


    def getVersionKeyFromDB(identifier: String, graphId: String)(implicit ec: ExecutionContext): Future[String] = {
        SearchAsyncOperations.getNodeProperty(graphId, identifier, "versionKey").map(property => {
            val versionKey: String = property.getPropertyValue.asInstanceOf[String]
            if(StringUtils.isNotBlank(versionKey))
                versionKey
            else
                String.valueOf(new Date().getTime)
        })
    }

}
