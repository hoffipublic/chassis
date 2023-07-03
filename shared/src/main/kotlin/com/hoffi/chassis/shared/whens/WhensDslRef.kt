package com.hoffi.chassis.shared.whens

import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef

// TODO use whenXXX Functions for "isDslElement() when"-Decissions

object WhensDslRef {
    fun <R> whenModelOrModelSubelement(
        dslRef: IDslRef,
        isModelRef: () -> R,
        isDtoRef: () -> R,
        isTableRef: () -> R,
        catching: (DslException) -> Unit = {}
    ): R {
        return when (dslRef) {
            is DslRef.model -> isModelRef()
            is DslRef.dto -> isDtoRef()
            is DslRef.table -> isTableRef()
            else -> throw DslException("neither model, nor (known) modelSubelement")
        }
    }

    fun <R> whenModelOrModelSubelement(dslRef: IDslRef, isModelRef: () -> R, isModelSubelementRef: () -> R, catching: (DslException) -> Unit = {}): R {
        return when (dslRef) {
            is DslRef.model -> isModelRef()
            is DslRef.dto, is DslRef.table -> isModelSubelementRef()
            else -> throw DslException("neither model, nor (known) modelSubelement")
        }
    }

    fun <R> whenModelSubelement(dslRef: IDslRef, isDtoRef: () -> R, isTableRef: () -> R, catching: (DslException) -> Unit = {}): R {
        return when (dslRef) {
            is DslRef.dto -> isDtoRef()
            is DslRef.table -> isTableRef()
            else -> throw DslException("no (known) modelSubelement")
        }
    }

    fun <R> whenApigroupElement(dslRef: IDslRef, isApiRef: () -> R, catching: (DslException) -> Unit = {}): R {
        return when (dslRef) {
            is DslRef.api -> isApiRef()
            else -> throw DslException("no (known) apigroupElement")
        }
    }

    fun <R> whenModelgroupElement(dslRef: IDslRef, isModelRef: () -> R, isFillerRef: () -> R, isAllmodelsRef: () -> R, catching: (DslException) -> Unit = {}): R {
        return when (dslRef) {
            is DslRef.model -> isModelRef()
            is DslRef.filler -> isFillerRef()
            is DslRef.allModels -> isAllmodelsRef()
            else -> throw DslException("no (known) modelgroupElement")
        }
    }

    fun <R> whenGroup(dslRef: IDslRef, isModelgroup: () -> R, isApigroup: () -> R, catching: (DslException) -> Unit = {}): R {
        return when(dslRef) {
            isModelgroup -> isModelgroup()
            isApigroup -> isApigroup()
            else -> throw DslException("nor (known) Dsl groupelement")
        }
    }
}
