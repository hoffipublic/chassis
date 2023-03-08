package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.dsl.ChassisDslMarker
import com.hoffi.chassis.shared.dsl.DslRef

@ChassisDslMarker
interface IDslGatherProperties {
    fun gatherPropertiesOf(modelSubelement: DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither, alsoOfSuperclasses: Boolean = false)
}
@ChassisDslMarker
class DslGatherPropertiesImpl(var modelgroupOrElement: DslRef.DslGroupRefEither = DslRef.DslGroupRefEither.NULL, var alsoOfSuperclasses: Boolean = false) :
    IDslGatherProperties {
    override fun gatherPropertiesOf(modelSubelement: DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither, alsoOfSuperclasses: Boolean) {
        this.modelgroupOrElement = modelSubelement
        this.alsoOfSuperclasses = alsoOfSuperclasses
    }
}
