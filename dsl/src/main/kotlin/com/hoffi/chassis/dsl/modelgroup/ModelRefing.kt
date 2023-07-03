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
class DslImplModelReffing(val dslClass: ADslClass) : IDslApiModelReffing {
    fun fakeOf(modelelement: MODELREFENUM, thisModelgroupSubElementSimpleName: String): IDslRef = modelelement of thisModelgroupSubElementSimpleName
    override fun MODELREFENUM.of(thisModelgroupSubElementSimpleName: String): IDslRef {
        if (dslClass.selfDslRef.parentRef.parentRef is DslRef.IElementLevel && this == MODELREFENUM.MODEL) {
            throw DslException("extends directly on model|api|..., we cannot determine what subelement (dto, table, ...) to extend! (use e.g.: '+ (DTO of $this)'")
        }
        // so we are an ISubElementLevel here, or an IElementLevel, but MODELELEMENT is NOT Model
        val modelgroupDslClass =  if (dslClass.selfDslRef.parentRef.parentRef is DslRef.IElementLevel) {
            dslCtx.ctxObj<DslModelgroup>(dslClass.selfDslRef.parentRef.parentRef.parentRef)
        } else {
            dslCtx.ctxObj<DslModelgroup>(dslClass.selfDslRef.parentRef.parentRef.parentRef.parentRef)
        }
        // TODO hardcoded: possible only on modelgroup by now
        val dslModel = modelgroupDslClass.dslModels.firstOrNull { it.simpleName == thisModelgroupSubElementSimpleName } ?: throw DslException("ref: '${dslClass.selfDslRef.parentRef} +\"$thisModelgroupSubElementSimpleName\" no model with simpleName \"$thisModelgroupSubElementSimpleName\" found in that modelgroup")
        //// parent DslClass of extends { }
        //val elementLevelDslClass = dslExtendsDelegateImpl.dslCtxWrapper.dslCtx.getDslClass(dslExtendsDelegateImpl.parentRef.parentRef)
        val elementLevelDslClass = dslModel

        // TODO after refactorings
        // I think we have to get some(!?) named DslExtendsDelegate of the ElementLevel that we are referring to
        // and take the MODELELEMENT THAT one is referring to

        //// if MODELELEMENT.MODEL, translate to whatever this extends' subelement (dto/table/...) MODELELEMENT is
        //val modelelementToRef = when (this) {
        //    MODELREFENUM.MODEL -> { (dslExtendsDelegateImpl.parent as IDslImplModelAndModelSubElementsCommon).modelElement }
        //    else -> this
        //}
        val modelelementToRef = this // <-- to make it compile
        val subelementLevelRef = when (modelelementToRef) {
            MODELREFENUM.MODEL -> { throw DslException("should have been handled by above's when()") }
            MODELREFENUM.DTO -> {   DslRef.dto(C.DEFAULT, elementLevelDslClass.selfDslRef) }
            MODELREFENUM.TABLE -> { DslRef.table(C.DEFAULT, elementLevelDslClass.selfDslRef) }
        }

//        if (extends.replaceSuperclass) {
//            extends.typeClassOrDslRef = EitherTypeOrDslRef.EitherDslRef(subelementLevelRef)
//        } else {
//            if (extends.typeClassOrDslRef is EitherTypeOrDslRef.ExtendsNothing) {
//                extends.typeClassOrDslRef = EitherTypeOrDslRef.EitherDslRef(subelementLevelRef)
//            } else {
//                throw DslException("${dslExtendsDelegateImpl.parentRef} already extends ${extends.typeClassOrDslRef}")
//            }
//        }
        return subelementLevelRef
    }

    fun fakeInModelgroup(modelelement: MODELREFENUM, otherModelgroupSimpleName: String): OtherModelgroupSubelementDefault = modelelement inModelgroup otherModelgroupSimpleName
    override fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementDefault {
        if (dslClass.selfDslRef.parentRef is DslRef.IElementLevel && this != MODELREFENUM.MODEL) {
            throw DslException("extends:  'MODEL inModelgroup \"$otherModelgroupSimpleName\"' directly on model|api|..., we cannot determine what subelement (dto, table, ...) to extend! (use e.g.: '+ (DTO inModelgroup $otherModelgroupSimpleName)'")
        }
        if (dslClass.selfDslRef.parentRef is DslRef.IGroupLevel && this != MODELREFENUM.MODEL) {
            throw DslException("extends:  'MODEL inModelgroup \"$otherModelgroupSimpleName\"' directly on modelgroup|apigroup|..., we cannot determine what subelement (dto, table, ...) to extend! (use e.g.: '+ (DTO inModelgroup $otherModelgroupSimpleName)'")
        }

        val dslModelgroup = try {
            dslCtx.getModelgroupBySimpleName(otherModelgroupSimpleName)
        } catch (e: DslCtxException) {
            throw DslException("ref: '${dslClass.selfDslRef.parentRef}' ${e.message}")
        }

        return OtherModelgroupSubelementDefault(this, dslModelgroup)
    }

    fun fakeWithModelName(otherModelgroupSubelementDefault: OtherModelgroupSubelementDefault, modelName: String) = otherModelgroupSubelementDefault withModelName modelName
    override fun OtherModelgroupSubelementDefault.withModelName(modelName: String): IDslRef {
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

