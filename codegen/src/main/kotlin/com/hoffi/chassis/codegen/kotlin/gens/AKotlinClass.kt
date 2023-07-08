package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.codegen.GenCtxWrapper
import com.hoffi.chassis.shared.helpers.PoetHelpers.kdocGenerated
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.hoffi.chassis.shared.parsedata.Property
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

abstract class AHasPropertys(val modelClassData: ModelClassData)

context(GenCtxWrapper)
abstract class AKotlinClass(modelClassData: ModelClassData) : AHasPropertys(modelClassData) {
    override fun toString() = "${this::class.simpleName}(${modelClassData})"

    var builder = when (modelClassData.kind) {
        TypeSpec.Kind.OBJECT -> {
            TypeSpec.objectBuilder(modelClassData.poetType)
        }
        TypeSpec.Kind.INTERFACE -> {
            TypeSpec.interfaceBuilder(modelClassData.poetType)
        }
        else -> {
            TypeSpec.classBuilder(modelClassData.poetType)
        }
    }.apply {
        kdocGenerated(modelClassData)
        addSuperinterface(com.hoffi.chassis.shared.fix.RuntimeDefaults.WAS_GENERATED_INTERFACE_ClassName)
        if (modelClassData.kind != TypeSpec.Kind.OBJECT && modelClassData.kind != TypeSpec.Kind.INTERFACE) {
            //addNullCompanion() // TODO("addNullCompanion()")
        }
    }
    val constructorBuilder = FunSpec.constructorBuilder()


    val superclassPropsMap: MutableMap<String, Property> by lazy {
        val allInclSuperclassesProps: MutableMap<String, Property> = mutableMapOf()
        var extendsEither: EitherTypOrModelOrPoetType? = modelClassData.extends[C.DEFAULT]?.typeClassOrDslRef
        var extendsModel: EitherTypOrModelOrPoetType.EitherModel? = null
        if (extendsEither != null && extendsEither is EitherTypOrModelOrPoetType.EitherModel) {
            extendsModel = extendsEither
        }
        while (extendsModel != null) {
            val genModel: GenModel = genCtx.genModel(extendsModel.modelSubElementRef)
            allInclSuperclassesProps.putAll(genModel.allProps)
            extendsEither = genModel.extends[C.DEFAULT]?.typeClassOrDslRef
            extendsModel = if (extendsEither != null && extendsEither is EitherTypOrModelOrPoetType.EitherModel) extendsEither else null
        }
        allInclSuperclassesProps
    }
    val propsInclSuperclassPropsMap: MutableMap<String, Property> by lazy { (modelClassData.allProps + superclassPropsMap).toMutableMap() }

    fun generate(out: Appendable? = null): TypeSpec {
        val fileSpecBuilder = FileSpec.builder(modelClassData.modelClassName.poetType)
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
