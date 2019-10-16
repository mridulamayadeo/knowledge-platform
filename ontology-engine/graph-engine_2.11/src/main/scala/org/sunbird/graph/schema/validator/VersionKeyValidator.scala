package org.sunbird.graph.schema.validator

import java.util.Date

import org.apache.commons.lang3.StringUtils
import org.sunbird.common.dto.{Property, Request}
import org.sunbird.common.exception.{ClientException, ResponseCode}
import org.sunbird.common.{DateUtils, Platform}
import org.sunbird.graph.common.enums.GraphDACParams
import org.sunbird.graph.dac.enums.SystemNodeTypes
import org.sunbird.graph.dac.model.Node
import org.sunbird.graph.schema.IDefinitionNode
import org.sunbird.graph.service.common.{DACConfigurationConstants, NodeUpdateMode}
import org.sunbird.graph.service.operation.Neo4JBoltSearchOperations

import scala.concurrent.{ExecutionContext, Future}

trait VersionKeyValidator extends IDefinitionNode {

    private val graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY)

    @throws[Exception]
    abstract override def validate(node: Node)(implicit ec: ExecutionContext): Future[Node] = {
        if(!isValidVersionkey(node)) throw new ClientException(ResponseCode.CLIENT_ERROR.name, "Invalid version Key")
        super.validate(node)
    }

    def isValidVersionkey(node: Node): Boolean =  {
        val versionCheckMode = {
            if(schemaValidator.getConfig.hasPath("versionCheckMode")) schemaValidator.getConfig.getString("versionCheckMode")
            else NodeUpdateMode.OFF.name
        }
        if(StringUtils.equalsIgnoreCase(NodeUpdateMode.OFF.name, versionCheckMode)) {
            true
        }
        else{
            if (node.getNodeType.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name)) {
                if (StringUtils.isNotBlank(versionCheckMode)) { // from Local cache
                    val storedVersionKey: String = null //RedisStoreUtil.getNodeProperty(graphId, nodeId, GraphDACParams.versionKey.name());
                    validateUpdateOperation(node.getGraphId, node, storedVersionKey)
                }
                else { // from graph - fall back
                    validateUpdateOperation(node.getGraphId, node, null)
                }
            }else{
                true
            }
        }
    }

    def validateUpdateOperation(getGraphId: String, node: Node, storedVersionKey: String): Boolean = {
        val versionKey: String = node.getMetadata.get(GraphDACParams.versionKey.name).asInstanceOf[String]
        if(StringUtils.isBlank(versionKey) && StringUtils.isNotBlank(storedVersionKey))
            throw new ClientException("BLANK_VERSION", "Error! Version Key cannot be Blank. | [Node Id: " + node.getIdentifier + "]")

        if (StringUtils.equals(graphPassportKey, versionKey)) {
            node.getMetadata.put(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name, DateUtils.formatCurrentDate)
            return true
        }

        val graphVersionKey: String = {
            if(StringUtils.isBlank(storedVersionKey)){
                getVersionKeyFromDB(node.getIdentifier)
            }else{
                storedVersionKey
            }
        }
        StringUtils.equalsIgnoreCase(versionKey, graphVersionKey)
    }


    def getVersionKeyFromDB(identifier: String): String = {
        val versionKeyProp:Property = Neo4JBoltSearchOperations.getNodeProperty("domain", identifier, "versionKey", new Request())
        val versionKey: String = versionKeyProp.getPropertyValue.asInstanceOf[String]
        if(StringUtils.isNotBlank(versionKey))
            versionKey
        else
            String.valueOf(new Date().getTime)
    }

}
