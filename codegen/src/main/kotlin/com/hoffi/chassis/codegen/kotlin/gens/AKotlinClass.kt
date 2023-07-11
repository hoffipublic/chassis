package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.shared.helpers.PoetHelpers.kdocGenerated
import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

abstract class AHasPropertys(val modelClassData: ModelClassData)

context(GenCtxWrapper)
abstract class AKotlinClass(modelClassData: ModelClassData) : AHasPropertys(modelClassData) {
    override fun toString() = "${this::class.simpleName}(${modelClassData})"

    init {
        kotlinGenCtx.putKotlinGenClass(modelClassData.modelSubElRef, this)
    }

    var builder = when (modelClassData.kind) {
        TypeSpec.Kind.OBJECT -> {
            TypeSpec.objectBuilder(modelClassData.poetType as ClassName)
        }
        TypeSpec.Kind.INTERFACE -> {
            TypeSpec.interfaceBuilder(modelClassData.poetType as ClassName)
        }
        else -> {
            TypeSpec.classBuilder(modelClassData.poetType as ClassName)
        }
    }.apply {
        kdocGenerated(modelClassData)
        addSuperinterface(RuntimeDefaults.WAS_GENERATED_INTERFACE_ClassName)
        if (modelClassData.kind != TypeSpec.Kind.OBJECT && modelClassData.kind != TypeSpec.Kind.INTERFACE) {
            //addNullCompanion() // TODO("addNullCompanion()")
        }
    }
    val constructorBuilder = FunSpec.constructorBuilder()

    var companionBuilder: TypeSpec.Builder? = null
    fun getOrCreateCompanion(): TypeSpec.Builder = companionBuilder ?: TypeSpec.companionObjectBuilder().also { companionBuilder = it }

    fun generate(out: Appendable? = null): TypeSpec {
        val fileSpecBuilder = FileSpec.builder(modelClassData.modelClassName.poetType as ClassName)
        val typeSpec = builder.build()
        val fileSpec = fileSpecBuilder.addType(typeSpec).build()
        if (out != null) {
            fileSpec.writeTo(out)
        } else {
            try {
                fileSpec.writeTo((modelClassData.modelClassName.basePath / modelClassData.modelClassName.path).toNioPath())
            } catch (e: Exception) {
                throw GenException(e.message ?: "unknown error", e)
            }
        }
        return typeSpec
    }
}
