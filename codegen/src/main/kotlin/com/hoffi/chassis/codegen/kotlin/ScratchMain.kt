package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.dsl.GenException
import com.squareup.kotlinpoet.*

fun main() {
    val nameNprop = "normalProp"
    val nameSNprop = "normalPropSuper"
    val nameSCprop = "superConstrProp"

    // ===========
    // baseclass
    // ===========

    val poetType = ClassName("com.hoffi.scratch", "ScratchClass")
    val builder = TypeSpec.classBuilder(poetType)
    //val pC = PropertySpec.builder(nameSCprop, String::class.asClassName())
    val pN = PropertySpec.builder(nameNprop, String::class.asClassName())
    //builder.addProperty(pC.initializer(nameNprop).build()) // merge prop into primary Constructor (C)
    //builder.addProperty(pC.build())                   // normal (non-constructor) Prooperty  (C)
    builder.addProperty(pN.build())                   // normal (non-constructor) Prooperty  (N)

    val constrBuilder = FunSpec.constructorBuilder()
    val paramBuilderPC = ParameterSpec.builder(nameSCprop, String::class.asClassName())
    constrBuilder.addParameter(paramBuilderPC.build())
    builder.primaryConstructor(constrBuilder.build())

    builder.addSuperclassConstructorParameter(nameSCprop)

    // ===========
    // superclass
    // ===========

    val superType = ClassName("com.hoffi.scratch", "ScratchSuper")
    val superBuilder = TypeSpec.classBuilder(superType)
    val superPC = PropertySpec.builder(nameSCprop, String::class.asClassName())
    val superPN = PropertySpec.builder(nameSNprop, String::class.asClassName())
    superBuilder.addProperty(superPC.initializer(nameSCprop).build())
    superBuilder.addProperty(superPN.build())

    val superConstrBuilder = FunSpec.constructorBuilder()
    val superParamBuilderSPC = ParameterSpec.builder(nameSCprop, String::class.asClassName())
    superConstrBuilder.addParameter(superParamBuilderSPC.build())
    superBuilder.primaryConstructor(superConstrBuilder.build())

    builder.superclass(superType)


    val typeSpec = builder.build()
    val typeSpecSuper = superBuilder.build()

    // ====================================================================
    val fileSpecBuilder = FileSpec.builder(poetType)
    fileSpecBuilder.addType(typeSpec)
    fileSpecBuilder.addType(typeSpecSuper)
    val fileSpec = fileSpecBuilder.build()
    val out = StringBuilder()
    try {
        fileSpec.writeTo(out)
    } catch(e: Exception) {
        throw GenException(e.message ?: "unknown error", e)
    }
    println(out)
}
