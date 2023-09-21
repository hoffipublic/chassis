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
    infix fun MODELREFENUM.of(thisModelgroupSubElementRef: IDslRef): DslRef.IModelOrModelSubelement
    infix fun MODELREFENUM.of(thisModelgroupsModelSimpleName: String): DslRef.IModelOrModelSubelement // + for super class (referencing this@modelgroup's name of ModelSubElement MODELELEMENT.(DTO|TABLE)
    infix fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault // + for super class
    infix fun OtherModelgroupSubelementWithSimpleNameDefault.withModelName(modelName: String): IDslRef
}

/** delegate IDslApiModelReffing to this */
class DslImplModelReffing(val dslClass: ADslClass) : IDslApiModelReffing {

    fun fakeOf(modelelement: MODELREFENUM, thisModelgroupSubElementRef: IDslRef): DslRef.IModelOrModelSubelement = modelelement of thisModelgroupSubElementRef

    override infix fun MODELREFENUM.of(thisModelgroupSubElementRef: IDslRef): DslRef.IModelOrModelSubelement {
        if (thisModelgroupSubElementRef !is DslRef.IModelSubelement) throw DslException("is no subelement level ref $thisModelgroupSubElementRef")
        val modelRef = DslRef.modelRefFrom(dslClass.selfDslRef)
        val dslRef: IDslRef = when (this) {
            MODELREFENUM.MODEL -> modelRef
            MODELREFENUM.DTO ->   DslRef.dto(  C.DEFAULT, modelRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, modelRef)
            MODELREFENUM.DCO ->   DslRef.dco(  C.DEFAULT, modelRef)
        }
        return dslRef as DslRef.IModelOrModelSubelement
    }

    fun fakeOf(modelelement: MODELREFENUM, thisModelgroupsModelSimpleName: String): DslRef.IModelOrModelSubelement = modelelement of thisModelgroupsModelSimpleName

    override infix fun MODELREFENUM.of(thisModelgroupsModelSimpleName: String): DslRef.IModelOrModelSubelement {
        val groupRef = DslRef.groupRefFrom(dslClass.selfDslRef)
        val otherModelRef = DslRef.model(thisModelgroupsModelSimpleName, groupRef)
        val dslRef: IDslRef =  when (this) {
            MODELREFENUM.MODEL -> otherModelRef
            MODELREFENUM.DTO ->   DslRef.dto(  C.DEFAULT, otherModelRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, otherModelRef)
            MODELREFENUM.DCO ->   DslRef.dco(  C.DEFAULT, otherModelRef)
        }
        return dslRef as DslRef.IModelOrModelSubelement
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
            MODELREFENUM.DCO -> {   DslRef.dco(C.DEFAULT, reffedElement) }
        }
        return dslRef
    }

    companion object {
        fun defaultSubElementOfModelNamed(modelSimpleName: String, dslClass: ADslClass): DslRef.ISubElementLevel {
            val (groupRef, _, subelementLevelRef) = DslRef.groupAndElementAndSubelementLevelDslRef(dslClass.selfDslRef)
            if (subelementLevelRef == null) throw DslException("$dslClass not under a sub(!)element (dto, tableFor, ...)")
            val dslRef: DslRef.ISubElementLevel = WhensDslRef.whenModelSubelement(subelementLevelRef,
                isDtoRef =   { DslRef.dto(C.DEFAULT, DslRef.model(modelSimpleName, groupRef)) },
                isDcoRef =   { DslRef.dco(C.DEFAULT, DslRef.model(modelSimpleName, groupRef)) },
                isTableRef = { DslRef.table(C.DEFAULT, DslRef.model(modelSimpleName, groupRef)) },
            ) {
                DslException("$dslClass has is not under or has no subelement named $modelSimpleName")
            }
            return dslRef
        }
    }
}
