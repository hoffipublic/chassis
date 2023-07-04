package com.hoffi.chassis.dsl.whens

import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslClass
import com.hoffi.chassis.dsl.modelgroup.DslDto
import com.hoffi.chassis.dsl.modelgroup.DslModel
import com.hoffi.chassis.dsl.modelgroup.DslTable

// TODO use whenXXX Functions for "isDslElement() when"-Decissions
object WhensModelgroup {
    fun <R> whenModelOrModelSubelement(modelOrModelSubelement: ADslClass,
        isDslModel: () -> R,
        isModelSubelement: () -> R,
        catching: (DslException) -> Throwable = { Throwable("when on '$modelOrModelSubelement' not exhaustive") }
    ): R {
        return when (modelOrModelSubelement) {
            is DslModel -> isDslModel()
            is DslDto, is DslTable -> isModelSubelement()
            else -> {
                throw catching(DslException("neither model, nor (known) modelSubelement"))
            }
        }
    }
}
