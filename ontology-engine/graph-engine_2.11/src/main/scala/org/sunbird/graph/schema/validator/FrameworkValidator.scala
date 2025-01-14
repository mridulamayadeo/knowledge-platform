package org.sunbird.graph.schema.validator

import java.util

import org.sunbird.cache.impl.CategoryCache
import org.sunbird.common.exception.ClientException
import org.sunbird.graph.dac.model.Node
import org.sunbird.graph.schema.IDefinitionNode

import scala.collection.JavaConverters._
import scala.collection.Map
import scala.concurrent.{ExecutionContext, Future}

trait FrameworkValidator extends IDefinitionNode {
  val categoryCache: CategoryCache = new CategoryCache()
  @throws[Exception]
  abstract override def validate(node: Node, operation: String)(implicit ec: ExecutionContext): Future[Node] = {
    val fwCategories: List[String] = schemaValidator.getConfig.getStringList("frameworkCategories").asScala.toList
    val framework: String = node.getMetadata.getOrDefault("framework", "").asInstanceOf[String]
    if (null != fwCategories && fwCategories.nonEmpty && framework.nonEmpty) {
      //prepare data for validation
      val fwMetadata: Map[String, AnyRef] = node.getMetadata.asScala.filterKeys(key => fwCategories.contains(key))
      //validate data from cache
      if (fwMetadata.nonEmpty) {
        val errors: util.List[String] = new util.ArrayList[String]
        for (cat: String <- fwMetadata.keys) {
          val value: AnyRef = fwMetadata.get(cat).get
          val list: List[String] = categoryCache.getList(categoryCache.getKey(framework, cat)).asScala.toList
          val result: Boolean = value match {
            case value: String => list.contains(value)
            case value: util.List[String] => list.asJava.containsAll(value)
            case value: Array[String] => value.forall(term => list.contains(term))
            case _ => throw new ClientException("CLIENT_ERROR", "Validation errors.", util.Arrays.asList("Please provide correct value for [" + cat + "]"))
          }

          if (!result)
            errors.add("Metadata " + cat + " should belong from:" + list.asJava)
        }
        if (!errors.isEmpty)
          throw new ClientException("CLIENT_ERROR", "Validation errors.", errors)
      }
    }
    super.validate(node, operation)
  }

}
