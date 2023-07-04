package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslClass
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM

data class OtherModelgroupSubelementWithSimpleNameDefault(val modelrefEnumOfReffedElementOrSubelement: MODELREFENUM, val modelgroupDslRef: DslRef.modelgroup)

@ChassisDslMarker
interface IDslApiModelReffing {
    infix fun MODELREFENUM.of(thisModelgroupSubElementSimpleName: String): DslRef.IModelOrModelSubelement // + for super class (referencing this@modelgroup's name of ModelSubElement MODELELEMENT.(DTO|TABLE)
    infix fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault // + for super class
    infix fun OtherModelgroupSubelementWithSimpleNameDefault.withModelName(modelName: String): DslRef.IModelOrModelSubelement
}

/** delegate IDslApiModelReffing to this */
class DslImplModelReffing constructor(val dslClass: ADslClass) : IDslApiModelReffing {

    fun fakeOf(modelelement: MODELREFENUM, thisModelgroupSubElementSimpleName: String): DslRef.IModelOrModelSubelement = modelelement of thisModelgroupSubElementSimpleName

    override infix fun MODELREFENUM.of(thisModelgroupSubElementSimpleName: String): DslRef.IModelOrModelSubelement {
        // TODO see also IDslRef.unaryPlus() of DslExtends DslExtendsBlockImpl
        var elementLevelDslRef = dslClass.selfDslRef.parentRef
        while (elementLevelDslRef !is DslRef.IElementLevel) {
            if (elementLevelDslRef.level == 1) throw DslException("no elementLevel dslRef in parents of ${dslClass.selfDslRef}")
            elementLevelDslRef = elementLevelDslRef.parentRef
        }
        //if (this == MODELREFENUM.MODEL && (elementLevelDslRef == dslClass.selfDslRef.parentRef) ) {
        //    throw DslException("extends directly on model|api|..., we cannot determine what subelement (dto, table, ...) to extend! (use e.g.: '+ (DTO of $this)'")
        //}
        //// so we are an ISubElementLevel here, or an IElementLevel, but MODELELEMENT is NOT Model

        val groupRef = elementLevelDslRef.parentRef
        return when (this) {
            MODELREFENUM.MODEL -> DslRef.model(thisModelgroupSubElementSimpleName, groupRef)
            MODELREFENUM.DTO ->   DslRef.dto(  C.DEFAULT, DslRef.model(thisModelgroupSubElementSimpleName, groupRef))
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, DslRef.model(thisModelgroupSubElementSimpleName, groupRef))
        }
    }

    fun fakeInModelgroup(modelelement: MODELREFENUM, otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault = modelelement inModelgroup otherModelgroupSimpleName

    override infix fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault {
        //if (dslClass.selfDslRef.parentRef is DslRef.IElementLevel && this != MODELREFENUM.MODEL) {
        //    throw DslException("extends:  'MODEL inModelgroup \"$otherModelgroupSimpleName\"' directly on model|api|..., we cannot determine what subelement (dto, table, ...) to extend! (use e.g.: '+ (DTO inModelgroup $otherModelgroupSimpleName)'")
        //}
        //if (dslClass.selfDslRef.parentRef is DslRef.IGroupLevel && this != MODELREFENUM.MODEL) {
        //    throw DslException("extends:  'MODEL inModelgroup \"$otherModelgroupSimpleName\"' directly on modelgroup|apigroup|..., we cannot determine what subelement (dto, table, ...) to extend! (use e.g.: '+ (DTO inModelgroup $otherModelgroupSimpleName)'")
        //}

        //// TODO fix to implement purely via DslRef as other modelgroup might not (yet) have been parsed and therefore might not exist in Ctx yet
        //val dslModelgroup = try {
        //    dslCtx.getModelgroupBySimpleName(otherModelgroupSimpleName)
        //} catch (e: DslCtxException) {
        //    throw DslException("ref: '${dslClass.selfDslRef.parentRef}' ${e.message}")
        //}

        val groupRef = DslRef.modelgroup(otherModelgroupSimpleName, DslDiscriminator("reffed"))
        return OtherModelgroupSubelementWithSimpleNameDefault(this, groupRef)
    }

    fun fakeWithModelName(otherModelgroupSubelementDefault: OtherModelgroupSubelementWithSimpleNameDefault, modelName: String) = otherModelgroupSubelementDefault withModelName modelName

    override infix fun OtherModelgroupSubelementWithSimpleNameDefault.withModelName(modelName: String): DslRef.IModelOrModelSubelement {
        //val modelgroupDslClass = this.modelgroupDslRef
        //// TODO hardcoded: possible only on modelgroup by now
        //val dslModel = modelgroupDslClass.dslModels.firstOrNull { it.simpleName == modelName } ?: throw DslException("ref: '${dslClass.selfDslRef.parentRef} +\"${this.modelgroupDslRef}\" extends '${this.modelrefEnumOfReffedElementOrSubelement}' with simplename '$modelName' ref not found in dslCtx!")
        ////// parent DslClass of extends { }
        ////val elementLevelDslClass = dslExtendsDelegateImpl.dslCtxWrapper.dslCtx.getDslClass(dslExtendsDelegateImpl.parentRef.parentRef)
        //val elementLevelDslClass = dslModel
        //
        //// TODO after refactorings same as above method
        //
        ////// if MODELELEMENT.MODEL, translate to whatever this extends' subelement (dto/table/...) MODELELEMENT is
        ////val modelelementToRef = when (this.defaultOfModelelement) {
        ////    MODELREFENUM.MODEL -> { (dslExtendsDelegateImpl.parent as IDslImplModelAndModelSubElementsCommon).modelElement }
        ////    else -> this.defaultOfModelelement
        ////}

        //val modelelementToRef = this.modelrefEnumOfReffedElementOrSubelement // <-- to make it compile
        val reffedElement = DslRef.model(modelName, this.modelgroupDslRef)
        val dslRef: DslRef.IModelOrModelSubelement = when (this.modelrefEnumOfReffedElementOrSubelement) {
            MODELREFENUM.MODEL -> { reffedElement }
            MODELREFENUM.DTO -> {   DslRef.dto(C.DEFAULT, reffedElement) }
            MODELREFENUM.TABLE -> { DslRef.table(C.DEFAULT, reffedElement) }
        }
        return dslRef
    }
}
