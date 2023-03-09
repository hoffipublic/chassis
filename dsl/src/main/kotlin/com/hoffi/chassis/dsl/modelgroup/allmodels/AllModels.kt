package com.hoffi.chassis.dsl.modelgroup.allmodels

import com.hoffi.chassis.chassismodel.dsl.ChassisDslMarker
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslClass
import com.hoffi.chassis.shared.dsl.IDslRef

interface IAllModels

@ChassisDslMarker
internal class AllModels(val modelDslAllModelsRef: DslRef.allModels, override val parent: IDslClass) : IAllModels, IDslClass {
    override val selfDslRef: DslRef = modelDslAllModelsRef
    override val parentDslRef: IDslRef = parent.selfDslRef
    override val groupDslRef: DslRef.IGroupLevel = parent.groupDslRef
    override fun toString() = selfDslRef.toString()
}
