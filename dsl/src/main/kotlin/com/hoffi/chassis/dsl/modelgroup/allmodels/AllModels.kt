package com.hoffi.chassis.dsl.modelgroup.allmodels

import com.hoffi.chassis.dsl.internal.ADslClass
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.shared.dsl.DslRef

@ChassisDslMarker
interface IApiAllModels

context(DslCtxWrapper)
internal class AllModels( // TODO Is This class a Delegate or not???
    val simpleName: String,
    val allModelsRef: DslRef.allModels
)
    :   IApiAllModels,
        ADslClass()
{
    override val selfDslRef: DslRef = allModelsRef
}
