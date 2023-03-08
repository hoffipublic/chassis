package com.hoffi.chassis.dsl.modelgroup.allmodels

import com.hoffi.chassis.chassismodel.dsl.ChassisDslMarker
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.DslRef.DslGroupRefEither.DslModelgroupRef
import com.hoffi.chassis.shared.dsl.DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslAllModelsRef
import com.hoffi.chassis.shared.dsl.IDslClass

interface IAllModels

@ChassisDslMarker
internal class AllModels(val modelDslAllModelsRef: DslAllModelsRef, override val parent: IDslClass) : IAllModels, IDslClass {
    override val selfDslRef: DslRef = modelDslAllModelsRef
    override val parentDslRef: DslRef = parent.selfDslRef
    override val groupDslRef: DslRef.DslGroupRefEither = parent.groupDslRef
}
