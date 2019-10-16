package org.sunbird.graph.schema.validator

import org.sunbird.graph.schema.IDefinitionNode
import org.sunbird.graph.dac.model.Node

import scala.concurrent.{ExecutionContext, Future}

trait SchemaValidator extends IDefinitionNode {

    @throws[Exception]
    abstract override def validate(node: Node)(implicit ec: ExecutionContext): Future[Node] = {
        schemaValidator.validate(node.getMetadata)
        super.validate(node)
    }
}
