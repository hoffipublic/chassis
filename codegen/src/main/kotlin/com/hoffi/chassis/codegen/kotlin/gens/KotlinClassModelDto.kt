package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.codegen.GenCtxWrapper
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

context(GenCtxWrapper)
class KotlinClassModelDto(val dtoModel: GenModel.DtoModel)
    : AKotlinClass(dtoModel)
{
    override fun toString() = "${this::class.simpleName}(${dtoModel})"

    override var builder = when (dtoModel.kind) {
        TypeSpec.Kind.OBJECT -> {
            TypeSpec.objectBuilder(dtoModel.poetType as ClassName)
        }
        TypeSpec.Kind.INTERFACE -> {
            TypeSpec.interfaceBuilder(dtoModel.poetType as ClassName)
        }
        else -> {
            TypeSpec.classBuilder(dtoModel.poetType as ClassName)
        }
    }.apply {
    }

    fun build(): TypeSpec.Builder {
        buildExtends()
        buildConstructors()
        buildPropertys()
        buildAuxiliaryFunctions()
        return builder
    }

    fun buildExtends() {
        val extends = modelClassData.extends["default"]
        if (extends != null && extends.typeClassOrDslRef != EitherTypOrModelOrPoetType.NOTHING) {
            builder.superclass(extends.typeClassOrDslRef.modelClassName.poetType)
            when (extends.typeClassOrDslRef) {
                is EitherTypOrModelOrPoetType.EitherModel -> {
                    val eitherModel = extends.typeClassOrDslRef as EitherTypOrModelOrPoetType.EitherModel
                    val reffedModel = genCtx.genModel(eitherModel.modelSubElementRef)
                    for (superConstrProp: Property in reffedModel.propertys.values.filter { Tag.CONSTRUCTOR in it.tags }) {
                        builder.addSuperclassConstructorParameter(superConstrProp.name)
                    }
                }
                is EitherTypOrModelOrPoetType.EitherPoetType -> TODO()
                is EitherTypOrModelOrPoetType.EitherTyp -> {}
                is EitherTypOrModelOrPoetType.NOTHING -> {}
            }
        }
    }

    fun buildConstructors() {

    }

    fun buildPropertys() {
        for (prop in modelClassData.propertys.values) {
            prop.validate(this)
            val kotlinProp = KotlinPropertyDto(prop)
            val propSpec = kotlinProp.build()
            builder.addProperty(propSpec)
        }
        for (prop in modelClassData.gatheredPropertys.values) {
            prop.validate(this)
            val kotlinProp = KotlinPropertyDto(prop)
            val propSpec = kotlinProp.build()
            builder.addProperty(propSpec)
        }
    }

    fun buildFunctions() {

    }

    fun buildAuxiliaryFunctions() {

    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KotlinClassModelDto) return false
        return dtoModel == other.dtoModel
    }

    override fun hashCode() = dtoModel.hashCode()
}
