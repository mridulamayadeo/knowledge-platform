package org.sunbird.graph.schema.validator

import org.sunbird.graph.dac.model.Node
import org.sunbird.graph.schema.IDefinitionNode

import scala.concurrent.{ExecutionContext, Future}

trait SchemaValidator extends IDefinitionNode {

    @throws[Exception]
    abstract override def validate(node: Node, operation: String)(implicit ec: ExecutionContext): Future[Node] = {
        val result = schemaValidator.validate(node.getMetadata)
        if(operation.equalsIgnoreCase("create")) {
            node.setMetadata(result.getMetadata)
        }
        super.validate(node, operation)
    }
}
