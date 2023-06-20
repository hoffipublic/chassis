package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.shared.dsl.DslRef

data class OtherModelgroupSubelementDefault(val defaultOfModelelement: DslRef.model.MODELELEMENT, val dslModelgroup: DslModelgroup)

@ChassisDslMarker
interface IDslApiModelRefing {
    infix    fun DslRef.model.MODELELEMENT.of(thisModelgroupSubElementSimpleName: String): DslRef // + for super class (referencing this@modelgroup's name of ModelSubElement MODELELEMENT.(DTO|TABLE)
    infix    fun DslRef.model.MODELELEMENT.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementDefault // + for super class
    infix    fun OtherModelgroupSubelementDefault.withModelName(modelName: String): DslRef
}

/** delegate IDslApiModelRefing to this */
interface IDslImplModelRefing {
}

