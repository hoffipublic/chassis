package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslCtxException
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslClass
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM

data class OtherModelgroupSubelementDefault(val defaultOfModelelement: MODELREFENUM, val dslModelgroup: DslModelgroup)

@ChassisDslMarker
interface IDslApiModelReffing {
    infix fun MODELREFENUM.of(thisModelgroupSubElementSimpleName: String): IDslRef // + for super class (referencing this@modelgroup's name of ModelSubElement MODELELEMENT.(DTO|TABLE)
    infix fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementDefault // + for super class
    infix fun OtherModelgroupSubelementDefault.withModelName(modelName: String): IDslRef
}

/** delegate IDslApiModelReffing to this */
context(DslCtxWrapper)
class DslImplModelReffing constructor(val dslClass: ADslClass) : IDslApiModelReffing {

    fun fakeOf(modelelement: MODELREFENUM, thisModelgroupSubElementSimpleName: String): IDslRef = modelelement of thisModelgroupSubElementSimpleName

    override infix fun MODELREFENUM.of(thisModelgroupSubElementSimpleName: String): IDslRef {
        var elementLevelDslRef = dslClass.selfDslRef.parentRef
        while (elementLevelDslRef !is DslRef.IElementLevel) {
            if (elementLevelDslRef.level == 1) throw DslException("no elementLevel dslRef in parents of ${dslClass.selfDslRef}")
            elementLevelDslRef = elementLevelDslRef.parentRef
        }
        if (this == MODELREFENUM.MODEL && (elementLevelDslRef == dslClass.selfDslRef.parentRef) ) {
            throw DslException("extends directly on model|api|..., we cannot determine what subelement (dto, table, ...) to extend! (use e.g.: '+ (DTO of $this)'")
        }
        // so we are an ISubElementLevel here, or an IElementLevel, but MODELELEMENT is NOT Model

        return when (this) {
            MODELREFENUM.MODEL -> throw DslException("should have been catched above")
            MODELREFENUM.DTO ->   DslRef.dto(  C.DEFAULT, DslRef.model(thisModelgroupSubElementSimpleName, elementLevelDslRef.parentRef))
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, DslRef.model(thisModelgroupSubElementSimpleName, elementLevelDslRef.parentRef))
        }
    }

    fun fakeInModelgroup(modelelement: MODELREFENUM, otherModelgroupSimpleName: String): OtherModelgroupSubelementDefault = modelelement inModelgroup otherModelgroupSimpleName

    override infix fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementDefault {
        if (dslClass.selfDslRef.parentRef is DslRef.IElementLevel && this != MODELREFENUM.MODEL) {
            throw DslException("extends:  'MODEL inModelgroup \"$otherModelgroupSimpleName\"' directly on model|api|..., we cannot determine what subelement (dto, table, ...) to extend! (use e.g.: '+ (DTO inModelgroup $otherModelgroupSimpleName)'")
        }
        if (dslClass.selfDslRef.parentRef is DslRef.IGroupLevel && this != MODELREFENUM.MODEL) {
            throw DslException("extends:  'MODEL inModelgroup \"$otherModelgroupSimpleName\"' directly on modelgroup|apigroup|..., we cannot determine what subelement (dto, table, ...) to extend! (use e.g.: '+ (DTO inModelgroup $otherModelgroupSimpleName)'")
        }

        // TODO fix to implement purely via DslRef as other modelgroup might not (yet) have been parsed and therefore might not exist in Ctx yet
        val dslModelgroup = try {
            dslCtx.getModelgroupBySimpleName(otherModelgroupSimpleName)
        } catch (e: DslCtxException) {
            throw DslException("ref: '${dslClass.selfDslRef.parentRef}' ${e.message}")
        }

        return OtherModelgroupSubelementDefault(this, dslModelgroup)
    }

    fun fakeWithModelName(otherModelgroupSubelementDefault: OtherModelgroupSubelementDefault, modelName: String) = otherModelgroupSubelementDefault withModelName modelName
    override infix fun OtherModelgroupSubelementDefault.withModelName(modelName: String): IDslRef {
        val modelgroupDslClass = this.dslModelgroup
        // TODO hardcoded: possible only on modelgroup by now
        val dslModel = modelgroupDslClass.dslModels.firstOrNull { it.simpleName == modelName } ?: throw DslException("ref: '${dslClass.selfDslRef.parentRef} +\"${this.dslModelgroup}\" extends '${this.defaultOfModelelement}' with simplename '$modelName' ref not found in dslCtx!")
        //// parent DslClass of extends { }
        //val elementLevelDslClass = dslExtendsDelegateImpl.dslCtxWrapper.dslCtx.getDslClass(dslExtendsDelegateImpl.parentRef.parentRef)
        val elementLevelDslClass = dslModel

        // TODO after refactorings same as above method

        //// if MODELELEMENT.MODEL, translate to whatever this extends' subelement (dto/table/...) MODELELEMENT is
        //val modelelementToRef = when (this.defaultOfModelelement) {
        //    MODELREFENUM.MODEL -> { (dslExtendsDelegateImpl.parent as IDslImplModelAndModelSubElementsCommon).modelElement }
        //    else -> this.defaultOfModelelement
        //}
        val modelelementToRef = this.defaultOfModelelement // <-- to make it compile
        val subelementLevelRef = when (modelelementToRef) {
            MODELREFENUM.MODEL -> { dslModel.selfDslRef }
            MODELREFENUM.DTO -> {   DslRef.dto(C.DEFAULT, elementLevelDslClass.selfDslRef) }
            MODELREFENUM.TABLE -> { DslRef.table(C.DEFAULT, elementLevelDslClass.selfDslRef) }
        }
        return subelementLevelRef
    }
}

