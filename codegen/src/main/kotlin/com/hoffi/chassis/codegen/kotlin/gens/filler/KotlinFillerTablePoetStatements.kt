package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.codegen.kotlin.gens.ABaseForCrudAndFiller
import com.hoffi.chassis.shared.parsedata.Property
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec

object KotlinFillerTablePoetStatements {
    fun fillTablePropTypOrPoetType(funSpecBuilder: FunSpec.Builder, funNameInsertOrBatch: ABaseForCrudAndFiller.FunName, tablePoetType: ClassName, sourceVarName: String, prop: Property): FunSpec.Builder =
        funSpecBuilder.addCode(fillTablePropTypOrPoetType(CodeBlock.builder(), funNameInsertOrBatch, tablePoetType, sourceVarName, prop).build())
    fun fillTablePropTypOrPoetType(codeBlockBuilder: CodeBlock.Builder, funNameInsertOrBatch: ABaseForCrudAndFiller.FunName, tablePoetType: ClassName, sourceVarName: String, prop: Property): CodeBlock.Builder =
        codeBlockBuilder.addStatement("%L[%T.%L] = %L.%L", funNameInsertOrBatch.itOrThis(), tablePoetType, prop.name(), funNameInsertOrBatch.sourceOrIt(sourceVarName), prop.name())

    fun fillTablePropOne2OneModelUuid(funSpecBuilder: FunSpec.Builder, funNameInsertOrBatch: ABaseForCrudAndFiller.FunName, tablePoetType: ClassName, sourceVarName: String, prop: Property): FunSpec.Builder =
        funSpecBuilder.addCode(fillTablePropOne2OneModelUuid(CodeBlock.builder(), funNameInsertOrBatch, tablePoetType, sourceVarName, prop).build())
    fun fillTablePropOne2OneModelUuid(codeBlockBuilder: CodeBlock.Builder, funNameInsertOrBatch: ABaseForCrudAndFiller.FunName, tablePoetType: ClassName, sourceVarName: String, prop: Property): CodeBlock.Builder =
        codeBlockBuilder.addStatement("%L[%T.%L] = %L.%L.%L", funNameInsertOrBatch.itOrThis(), tablePoetType, prop.name(postfix = RuntimeDefaults.UUID_PROPNAME), funNameInsertOrBatch.sourceOrIt(sourceVarName), prop.name(), RuntimeDefaults.UUID_PROPNAME)

    fun fillTablePropMany2OneModelUuid(funSpecBuilder: FunSpec.Builder, funNameInsertOrBatch: ABaseForCrudAndFiller.FunName, tablePoetType: ClassName, many2OnePropVarNameUuid: String, prop: Property): FunSpec.Builder =
        funSpecBuilder.addCode(fillTablePropMany2OneModelUuid(CodeBlock.builder(), funNameInsertOrBatch, tablePoetType, many2OnePropVarNameUuid, prop).build())
    fun fillTablePropMany2OneModelUuid(codeBlockBuilder: CodeBlock.Builder, funNameInsertOrBatch: ABaseForCrudAndFiller.FunName, tablePoetType: ClassName, many2OnePropVarNameUuid: String, prop: Property): CodeBlock.Builder =
        codeBlockBuilder.addStatement("%L[%T.%L] = %L.%L", funNameInsertOrBatch.itOrThis(), tablePoetType, many2OnePropVarNameUuid, prop.name(), RuntimeDefaults.UUID_PROPNAME)
}
