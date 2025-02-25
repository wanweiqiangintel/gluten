/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.glutenproject.expression

import io.glutenproject.expression.ConverterUtils.FunctionConfig
import io.glutenproject.expression.ExpressionConverter.replaceWithExpressionTransformer
import io.glutenproject.substrait.expression.{ExpressionBuilder, ExpressionNode, StructLiteralNode}

import org.apache.spark.sql.catalyst.expressions.{Attribute, CreateNamedStruct, GetStructField}
import org.apache.spark.sql.types.IntegerType

import com.google.common.collect.Lists

class GetStructFieldTransformerBase(
    substraitExprName: String,
    childTransformer: ExpressionTransformer,
    ordinal: Int,
    original: GetStructField)
  extends ExpressionTransformer {

  override def doTransform(args: Object): ExpressionNode = {
    val childNode = childTransformer.doTransform(args)
    childNode match {
      case node: StructLiteralNode =>
        return node.getFieldLiteral(ordinal)
      case _ =>
    }

    val ordinalNode = ExpressionBuilder.makeLiteral(ordinal, IntegerType, false)
    val exprNodes = Lists.newArrayList(childNode, ordinalNode)
    val functionMap = args.asInstanceOf[java.util.HashMap[String, java.lang.Long]]
    val fieldDataType = original.dataType
    val functionName = ConverterUtils.makeFuncName(
      substraitExprName,
      Seq(original.child.dataType, fieldDataType),
      FunctionConfig.OPT)
    val functionId = ExpressionBuilder.newScalarFunction(functionMap, functionName)
    val typeNode = ConverterUtils.getTypeNode(original.dataType, original.nullable)
    ExpressionBuilder.makeScalarFunction(functionId, exprNodes, typeNode)
  }
}

class NamedStructTransformerBase(
    substraitExprName: String,
    original: CreateNamedStruct,
    attributeSeq: Seq[Attribute])
  extends ExpressionTransformer {
  override def doTransform(args: Object): ExpressionNode = {
    var childrenTransformers = Seq[ExpressionTransformer]()
    original.children.foreach(
      child =>
        childrenTransformers = childrenTransformers :+
          replaceWithExpressionTransformer(child, attributeSeq))
    val expressionNodes = Lists.newArrayList[ExpressionNode]()
    for (elem <- childrenTransformers) {
      expressionNodes.add(elem.doTransform(args))
    }
    val functionMap = args.asInstanceOf[java.util.HashMap[String, java.lang.Long]]
    val functionName =
      ConverterUtils.makeFuncName(substraitExprName, Seq(original.dataType), FunctionConfig.OPT)
    val functionId = ExpressionBuilder.newScalarFunction(functionMap, functionName)
    val typeNode = ConverterUtils.getTypeNode(original.dataType, original.nullable)
    ExpressionBuilder.makeScalarFunction(functionId, expressionNodes, typeNode)
  }
}
