package com.hoffi.chassis.shared.helpers

import com.hoffi.chassis.chassismodel.ENV
import com.hoffi.chassis.shared.codegen.GenCtx
import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.hoffi.chassis.shared.shared.CrudData
import com.hoffi.chassis.shared.shared.FillerData
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

object PoetHelpers {
    operator fun ClassName.plus(postfix: String) = ClassName(this.packageName, this.simpleName + postfix)
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
    fun TypeSpec.Builder.kdocGeneratedFiller(genCtx: GenCtx, fillerData: FillerData): TypeSpec.Builder {
        val modelClassData = genCtx.genModel(fillerData.targetDslRef)
        return this.addKdoc(
            "Filler for %L model: %L\nwith dslRef: %L\ngenerated at %L on %L",
            modelClassData.modelSubElRef.refList.last().functionName,
            modelClassData.modelOrTypeNameString,
            modelClassData.modelSubElRef,
            ENV.generationLocalDateTime,
            ENV.hostname
        )
    }
    fun TypeSpec.Builder.kdocGeneratedCrud(genCtx: GenCtx, crudData: CrudData): TypeSpec.Builder {
        val modelClassData = genCtx.genModel(crudData.targetDslRef)
        return this.addKdoc(
            "CRUD ${crudData.crud} for %L model: %L\nwith dslRef: %L\ngenerated at %L on %L",
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
