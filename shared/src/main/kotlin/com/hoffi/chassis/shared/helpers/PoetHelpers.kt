package com.hoffi.chassis.shared.helpers

import com.hoffi.chassis.shared.fix.ENV
import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

object PoetHelpers {
    fun TypeName.nullable() = this.copy(nullable = true)
    fun ClassName.nullable() = this.copy(nullable = true, annotations = this.annotations, tags = this.tags)

    fun TypeSpec.Builder.kdocGenerated(modelClassData: ModelClassData): TypeSpec.Builder {
        return this.addKdoc(
            "%L model: %L\nwith dslRef: %L\ngenerated at %L on %L",
            modelClassData.modelSubElRef.refList.last().functionName,
            modelClassData.modelOrTypeNameString,
            modelClassData.modelSubElRef,
            ENV.generationLocalDateTime,
            ENV.hostname
        )
    }
    fun TypeSpec.Builder.kdocGenerated(string: String): TypeSpec.Builder {
        return this.addKdoc(
            "%L\ngenerated at %L on %L",
            string,
            ENV.generationLocalDateTime,
            ENV.hostname
        )
    }

}
