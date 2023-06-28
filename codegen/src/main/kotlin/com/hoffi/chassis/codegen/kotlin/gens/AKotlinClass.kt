package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

abstract class AKotlinClass(val modelClassData: ModelClassData) {
    abstract var builder: TypeSpec.Builder

    fun generate(): TypeSpec {
        val fileSpecBuilder = FileSpec.builder(modelClassData.modelClassName.poetType)
        val typeSpec = builder.build()
        val fileSpec = fileSpecBuilder.addType(typeSpec).build()
        fileSpec.writeTo((modelClassData.modelClassName.basePath / modelClassData.modelClassName.path).toNioPath())
        return typeSpec
    }
}
