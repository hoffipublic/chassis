package com.hoffi.chassis.dsl.eitherdecissions

import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslClass
import com.hoffi.chassis.dsl.internal.DslCtx
import com.hoffi.chassis.dsl.internal.dslCtxWrapperFake
import com.hoffi.chassis.dsl.internal.globalDslCtx
import com.hoffi.chassis.dsl.modelgroup.DslDto
import com.hoffi.chassis.dsl.modelgroup.DslModel
import com.hoffi.chassis.dsl.modelgroup.DslTable
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef

// TODO use whenXXX Functions for "isDslElement() when"-Decissions

fun <R> whenModelOrModelSubelement(dslRef: IDslRef, isModelRef: () -> R, isDtoRef: () -> R, isTableRef: () -> R): R {
    return when (dslRef) {
        is DslRef.model -> isModelRef()
        is DslRef.dto -> isDtoRef()
        is DslRef.table -> isTableRef()
        else -> { throw DslException("neither model, nor (known) modelSubelement") }
    }
}

fun <R> whenModelOrModelSubelement(dslRef: IDslRef, isModelRef: () -> R, isModelSubelementRef: () -> R): R {
    return when (dslRef) {
        is DslRef.model -> isModelRef()
        is DslRef.dto, is DslRef.table -> isModelSubelementRef()
        else -> { throw DslException("neither model, nor (known) modelSubelement") }
    }
}

fun <R> whenModelOrModelSubelement(modelOrModelSubelement: ADslClass, isDslModel: () -> R, isModelSubelement: () -> R): R {
    return when (modelOrModelSubelement) {
        is DslModel -> isDslModel()
        is DslDto, is DslTable -> isModelSubelement()
        else -> { throw DslException("neither model, nor (known) modelSubelement") }
    }
}

fun main() {
    println("creating global Fake DslCtx ...")
    globalDslCtx = DslCtx.NULL
    println("creating global Fake DslCtx ... ready")
    with(dslCtxWrapperFake) {
        println("creating DslDto ...")
        val dslDto = DslDto("testing", DslRef.dto("testingSimpleName", IDslRef.NULL))
        println("creating DslDto ... ready")

        whenModelOrModelSubelement(dslDto,
            isDslModel = { println("it was a Model") },
            isModelSubelement = { println("it was a modelSubelement") }
        )
    }
}
