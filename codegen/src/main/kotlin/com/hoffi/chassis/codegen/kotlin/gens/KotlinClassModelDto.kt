package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.shared.EitherTypeOrDslRef
import com.hoffi.chassis.shared.codegen.GenCtxWrapper
import com.hoffi.chassis.shared.parsedata.EitherModel
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

context(GenCtxWrapper)
class KotlinClassModelDto(val dtoModel: EitherModel.DtoModel)
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

    fun build() {
        buildExtends()
        buildConstructors()
        buildPropertys()
        buildAuxiliaryFunctions()
    }

    fun buildExtends() {
        val extends = modelClassData.extends["default"]
        if (extends != null && extends.typeClassOrDslRef != EitherTypeOrDslRef.NOTHING) {
                //builder.superclass(extends.classPoetType)
                //for (superConstrProp: Property in superclassModel.propertys.values.filter { Tag.CONSTRUCTOR in it.tags }) {
                //    builder.addSuperclassConstructorParameter(superConstrProp.name)
                //}
            }
        }

    fun buildConstructors() {

    }

    fun buildPropertys() {

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
