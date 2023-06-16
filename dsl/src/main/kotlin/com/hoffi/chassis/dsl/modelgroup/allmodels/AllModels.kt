package com.hoffi.chassis.dsl.modelgroup.allmodels

import com.hoffi.chassis.dsl.internal.ADslClass
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.shared.dsl.DslRef

interface IApiAllModels

context(DslCtxWrapper)
@ChassisDslMarker
internal class AllModels( // TODO Is This class a Delegate or not???
    val simpleName: String,
    val allModelsRef: DslRef.allModels
)
    :   IApiAllModels,
        ADslClass()
{
    override val selfDslRef: DslRef = allModelsRef
}
