package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslClass
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.hoffi.chassis.shared.whens.WhensDslRef

data class OtherModelgroupSubelementWithSimpleNameDefault(val modelrefEnumOfReffedElementOrSubelement: MODELREFENUM, val modelgroupDslRef: DslRef.modelgroup)

@ChassisDslMarker
interface IDslApiModelReffing {
    infix fun MODELREFENUM.of(thisModelgroupSubElementRef: IDslRef): IDslRef
    infix fun MODELREFENUM.of(thisModelgroupSubElementSimpleName: String): IDslRef // + for super class (referencing this@modelgroup's name of ModelSubElement MODELELEMENT.(DTO|TABLE)
    infix fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault // + for super class
    infix fun OtherModelgroupSubelementWithSimpleNameDefault.withModelName(modelName: String): IDslRef
}

/** delegate IDslApiModelReffing to this */
class DslImplModelReffing constructor(val dslClass: ADslClass) : IDslApiModelReffing {

    fun fakeOf(modelelement: MODELREFENUM, thisModelgroupSubElementRef: IDslRef): IDslRef = modelelement of thisModelgroupSubElementRef

    override infix fun MODELREFENUM.of(thisModelgroupSubElementRef: IDslRef): IDslRef {
        if (thisModelgroupSubElementRef !is DslRef.IModelSubelement) throw DslException("is no subelement level ref $thisModelgroupSubElementRef")
        val (groupRef, elementLevelRef, _) = groupElementAndSubelementLevelDslRef(dslClass)
        val simpleName = elementLevelRef.simpleName
        val dslRef: IDslRef = when (this) {
            MODELREFENUM.MODEL -> DslRef.model(simpleName, groupRef)
            MODELREFENUM.DTO ->   DslRef.dto(  C.DEFAULT, DslRef.model(simpleName, groupRef))
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, DslRef.model(simpleName, groupRef))
        }
        return dslRef
    }

    fun fakeOf(modelelement: MODELREFENUM, thisModelgroupSubElementSimpleName: String): IDslRef = modelelement of thisModelgroupSubElementSimpleName

    override infix fun MODELREFENUM.of(thisModelgroupSubElementSimpleName: String): IDslRef {
        val (groupRef, _, _) = groupElementAndSubelementLevelDslRef(dslClass)
        val dslRef: IDslRef =  when (this) {
            MODELREFENUM.MODEL -> DslRef.model(thisModelgroupSubElementSimpleName, groupRef)
            MODELREFENUM.DTO ->   DslRef.dto(  C.DEFAULT, DslRef.model(thisModelgroupSubElementSimpleName, groupRef))
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, DslRef.model(thisModelgroupSubElementSimpleName, groupRef))
        }
        return dslRef
    }


    fun fakeInModelgroup(modelelement: MODELREFENUM, otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault = modelelement inModelgroup otherModelgroupSimpleName

    override infix fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault {
        val groupRef = DslRef.modelgroup(otherModelgroupSimpleName, DslDiscriminator("reffed"))
        return OtherModelgroupSubelementWithSimpleNameDefault(this, groupRef)
    }

    fun fakeWithModelName(otherModelgroupSubelementDefault: OtherModelgroupSubelementWithSimpleNameDefault, modelName: String): IDslRef = otherModelgroupSubelementDefault withModelName modelName

    override infix fun OtherModelgroupSubelementWithSimpleNameDefault.withModelName(modelName: String): IDslRef {
        val reffedElement = DslRef.model(modelName, this.modelgroupDslRef)
        val dslRef: DslRef.IModelOrModelSubelement = when (this.modelrefEnumOfReffedElementOrSubelement) {
            MODELREFENUM.MODEL -> { reffedElement }
            MODELREFENUM.DTO -> {   DslRef.dto(C.DEFAULT, reffedElement) }
            MODELREFENUM.TABLE -> { DslRef.table(C.DEFAULT, reffedElement) }
        }
        return dslRef
    }

    companion object {
        fun groupElementAndSubelementLevelDslRef(dslRef: IDslRef): Triple<IDslRef, IDslRef, IDslRef?> {
            return DslRef.groupElementAndSubelementLevelDslRef(dslRef)
        }
        fun groupElementAndSubelementLevelDslRef(dslClass: ADslClass): Triple<IDslRef, IDslRef, IDslRef?> {
            return DslRef.groupElementAndSubelementLevelDslRef(dslClass.selfDslRef)
        }
        fun defaultSubElementWithName(simpleName: String, dslClass: ADslClass): DslRef.ISubElementLevel {
            val (groupRef, _, subelementLevelRef) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslClass)
            if (subelementLevelRef == null) throw DslException("$dslClass not under a sub(!)element (dto, table, ...)")
            val dslRef: DslRef.ISubElementLevel = WhensDslRef.whenModelSubelement(subelementLevelRef,
                isDtoRef =   { DslRef.dto(C.DEFAULT, DslRef.model(simpleName, groupRef)) },
                isTableRef = { DslRef.table(C.DEFAULT, DslRef.model(simpleName, groupRef)) },
            ) {
                DslException("$dslClass has is not under or has no subelement named $simpleName")
            }
            return dslRef
        }
    }
}
