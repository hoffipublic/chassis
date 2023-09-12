package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.shared.helpers.PoetHelpers.kdocGenerated
import com.hoffi.chassis.shared.parsedata.ModelClassDataFromDsl
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import org.slf4j.LoggerFactory

abstract class AHasPropertys(modelClassDataFromDsl: ModelClassDataFromDsl) : AKotlinGen(modelClassDataFromDsl)

context(GenCtxWrapper)
abstract class AKotlinGenClass(modelClassDataFromDsl: ModelClassDataFromDsl) : AHasPropertys(modelClassDataFromDsl) {
    override fun toString() = "${this::class.simpleName}(${modelClassDataFromDsl})"
    private val log = LoggerFactory.getLogger(javaClass)

    init {
        kotlinGenCtx.putKotlinGenClass(modelClassDataFromDsl.modelSubElRef, this)
    }

    var builder = when (modelClassDataFromDsl.kind) {
        TypeSpec.Kind.OBJECT -> {
            TypeSpec.objectBuilder(modelClassDataFromDsl.poetType)
        }
        TypeSpec.Kind.INTERFACE -> {
            TypeSpec.interfaceBuilder(modelClassDataFromDsl.poetType)
        }
        else -> {
            TypeSpec.classBuilder(modelClassDataFromDsl.poetType)
        }
    }.apply {
        kdocGenerated(modelClassDataFromDsl)
        addSuperinterface(RuntimeDefaults.WAS_GENERATED_INTERFACE_ClassName)
    }
    val constructorBuilder = FunSpec.constructorBuilder()

    var companionBuilder: TypeSpec.Builder? = null
    fun getOrCreateCompanion(): TypeSpec.Builder = companionBuilder ?: TypeSpec.companionObjectBuilder().also { companionBuilder = it }

    fun generate(out: Appendable? = null): TypeSpec {
        val fileSpecBuilder = FileSpec.builder(modelClassDataFromDsl.modelClassName.poetType)
        val typeSpec = builder.build()
        val fileSpec = fileSpecBuilder.addType(typeSpec).build()
        if (out != null) {
            fileSpec.writeTo(out)
        } else {
            try {
                val targetPathWithoutPackageAndFile = (modelClassDataFromDsl.modelClassName.basePath/modelClassDataFromDsl.modelClassName.path).toNioPath()
                log.info("writing: $targetPathWithoutPackageAndFile/${modelClassDataFromDsl.modelClassName.poetType.toString().replace('.', '/')}.kt")
                fileSpec.writeTo(targetPathWithoutPackageAndFile)
            } catch (e: Exception) {
                throw GenException(e.message ?: "unknown error", e)
            }
        }
        return typeSpec
    }
}
