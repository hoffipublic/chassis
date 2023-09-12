package com.hoffi.chassis.shared.whens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM

// TODO use whenXXX Functions for "isDslElement() when"-Decissions

object WhensDslRef {
    fun <R> whenModelOrModelSubelement(dslRef: IDslRef,
        isModelRef: () -> R,
        isDtoRef: () -> R,
        isDcoRef: () -> R,
        isTableRef: () -> R,
        catching: (DslException) -> Throwable = { Throwable("when on '$dslRef' not exhaustive") }
    ): R {
        when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
        return when (dslRef) {
            is DslRef.model -> isModelRef()
            is DslRef.dto -> isDtoRef()
            is DslRef.dco -> isDcoRef()
            is DslRef.table -> isTableRef()
            else -> throw catching(DslException("neither model, nor (known) modelSubelement"))
        }
    }

    fun <R> whenModelOrModelSubelement(dslRef: IDslRef,
        isModelRef: () -> R,
        isModelSubelementRef: () -> R,
        catching: (DslException) -> Throwable = { Throwable("when on '$dslRef' not exhaustive") }
    ): R {
        when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
        return when (dslRef) {
            is DslRef.model -> isModelRef()
            is DslRef.dto, is DslRef.table, is DslRef.dco -> isModelSubelementRef()
            else -> throw catching(DslException("neither model, nor (known) modelSubelement"))
        }
    }

    fun <R> whenModelSubelement(dslRef: IDslRef,
        isDtoRef: () -> R,
        isTableRef: () -> R,
        isDcoRef: () -> R,
        catching: (DslException) -> Throwable = { Throwable("when on '$dslRef' not exhaustive") }
    ): R {
        when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
        return when (dslRef) {
            is DslRef.dto -> isDtoRef()
            is DslRef.table -> isTableRef()
            is DslRef.dco -> isDcoRef()
            else -> throw catching(DslException("no (known) modelSubelement"))
        }
    }

    fun expandRefToSubelement(
        modelOrModelSubelementRef: DslRef.IModelOrModelSubelement,
        ownModelSubelementRef: DslRef.ISubElementLevel,
    ) : DslRef.IModelSubelement {
        when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
        val modelSubelementRefExpanded: DslRef.IModelSubelement = WhensDslRef.whenModelOrModelSubelement(modelOrModelSubelementRef,
            isModelRef = {
                WhensDslRef.whenModelSubelement(ownModelSubelementRef,
                    isDtoRef = { DslRef.dto(C.DEFAULT, modelOrModelSubelementRef) },
                    isDcoRef = { DslRef.dco(C.DEFAULT, modelOrModelSubelementRef) },
                    isTableRef = { DslRef.table(C.DEFAULT, modelOrModelSubelementRef) }
                ) {
                    DslException("no known model subelement")
                }
            },
            isDtoRef =   { modelOrModelSubelementRef as DslRef.IModelSubelement },
            isDcoRef =   { modelOrModelSubelementRef as DslRef.IModelSubelement },
            isTableRef = { modelOrModelSubelementRef as DslRef.IModelSubelement }
        ) {
            DslException("no known model or model subelement")
        }
        return modelSubelementRefExpanded
    }

    fun <R> whenApigroupElement(dslRef: IDslRef,
        isApiRef: () -> R,
        catching: (DslException) -> Throwable = { Throwable("when on '$dslRef' not exhaustive") }
    ): R {
        return when (dslRef) {
            is DslRef.api -> isApiRef()
            else -> throw catching(DslException("no (known) apigroupElement"))
        }
    }

    fun <R> whenModelgroupElement(dslRef: IDslRef,
        isModelRef: () -> R,
        isFillerRef: () -> R,
        isAllmodelsRef: () -> R,
        catching: (DslException) -> Throwable = { Throwable("when on '$dslRef' not exhaustive") }
    ): R {
        return when (dslRef) {
            is DslRef.model -> isModelRef()
            is DslRef.filler -> isFillerRef()
            is DslRef.allModels -> isAllmodelsRef()
            else -> throw catching(DslException("no (known) modelgroupElement"))
        }
    }

    fun <R> whenGroup(dslRef: IDslRef,
        isModelgroup: () -> R,
        isApigroup: () -> R,
        catching: (DslException) -> Throwable = { Throwable("when on '$dslRef' not exhaustive") }
    ): R {
        return when(dslRef) {
            isModelgroup -> isModelgroup()
            isApigroup -> isApigroup()
            else -> throw catching(DslException("nor (known) Dsl groupelement"))
        }
    }
}
