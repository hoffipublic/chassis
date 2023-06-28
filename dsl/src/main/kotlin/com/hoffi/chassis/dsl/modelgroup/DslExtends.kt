package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslCtxException
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.*
import com.hoffi.chassis.shared.EitherTypeOrDslRef
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.Extends
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===

/** props/fields and "direct/non-inner-dsl-block" funcs inside dsl block */
@ChassisDslMarker
interface IDslApiExtendsProps : IDslApiModelReffing { // TODO ModelReffing via delegated class (see DslGatherPropertiesDelegateImpl)
    var replaceSuperclass: Boolean
    var replaceSuperInterfaces: Boolean
    operator fun KClass<*>.unaryPlus()  // + for super class
    operator fun IDslRef.unaryPlus()    // + for super class
    /** inherit from same SubElement Type (e.g. dto/table/...) with simpleName C.DEFAULT, of an element (e.g. model) in the same group which has this name */
    operator fun String.unaryPlus()    // + for super class (referencing this@modelgroup's name of ModelSubElement of this@modelsubelement(thisSimpleName)
    //operator fun DslRef.model.MODELELEMENT.unaryPlus() // + for super class
    operator fun KClass<*>.unaryMinus() // - for super interfaces
    operator fun IDslRef.unaryMinus() // - for super interfaces
    operator fun IDslApiExtendsProps.minusAssign(kClass: KClass<*>) // exclude from super class/interfaces
    operator fun IDslApiExtendsProps.minusAssign(dslRef: DslRef) // exclude from super class/interfaces
    operator fun String.not()
    operator fun IDslApiExtendsProps.rem(docs: CodeBlock)
}
/** the "outermost" dsl block fun, that opens up this new "scope-hierarchy" (doesn't hold gathered DSL data by itself) */
@ChassisDslMarker
interface IDslApiExtendsDelegate {
    /** default dsl block's simpleName */
    // context(DslCtxWrapper) // see https://youtrack.jetbrains.com/issue/KT-57409/context-receivers-fail-if-implemented-via-delegated-interface
    @DslBlockOn(DslModel::class, DslDto::class, DslTable::class) // IDE clickable shortcuts to implementing @ChassisDslMarker classes
    fun extends(simpleName: String = C.DEFAULT, block: IDslApiExtendsBlock.() -> Unit)
}
/** would contain "inner" nested Dsl block scopes, and implements the props/directFuns */
@ChassisDslMarker
interface IDslApiExtendsBlock : IDslApiExtendsProps {
}

// === Impl Interfaces (extend IDslApis plus methods and props that should not be visible from the DSL ===

interface IDslImplExtendsProps : IDslApiExtendsProps {
    /** contains its (simpleName specific) data holder */
    var extends: Extends
}
interface IDslImplExtendsDelegate : IDslApiExtendsDelegate {
    /** contains all (simpleName specific) data holders */
    var theExtendBlocks: MutableMap<String, DslExtendsBlockImpl>
}
/** the (per simpleName) inner implementation of the DslBlock */
interface IDslImplExtendsBlock : IDslImplExtendsProps, IDslApiExtendsBlock

// === classes that implement IDslImpl lambda block functions

/** outer scope */
context(DslCtxWrapper)
class DslExtendsDelegateImpl(
    simpleNameOfParentDslBlock: String,
    parentRef: IDslRef,
) : ADslDelegateClass(simpleNameOfParentDslBlock, parentRef), IDslImplExtendsDelegate {
    val log = LoggerFactory.getLogger(javaClass)
    override fun toString(): String = "${super@DslExtendsDelegateImpl.toString()}->[theExtends=$theExtendBlocks]"

    override val selfDslRef = DslRef.extends(simpleNameOfParentDslBlock, parentRef)

    /** different gathered dsl data holder for different simpleName's inside the BlockImpl's */
    override var theExtendBlocks: MutableMap<String, DslExtendsBlockImpl> = mutableMapOf()

    /** DslBlock funcs always operate on IDslApi interfaces */
    // context(DslCtxWrapper) // see https://youtrack.jetbrains.com/issue/KT-57409/context-receivers-fail-if-implemented-via-delegated-interface
    override fun extends(simpleName: String, block: IDslApiExtendsBlock.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            dslCtx.PASS_4_REFERENCING -> {
                val dslImpl = theExtendBlocks.getOrPut(simpleName) { DslExtendsBlockImpl(simpleName, this) }
                dslImpl.apply(block)
            }
            else -> {}
        }
    }
}

// === impl object callable _inside_ the lambda block fun ===

/** inner scope */
context(DslCtxWrapper)
class DslExtendsBlockImpl(val simpleName: String, val dslExtendsDelegateImpl: DslExtendsDelegateImpl) : IDslImplExtendsBlock {
    override fun toString() = "(replaceSuperclass=$replaceSuperclass,replaceSuperInterfaces=$replaceSuperInterfaces,$extends"
    override var extends: Extends = Extends(simpleName)
    override var replaceSuperclass: Boolean
        get() = extends.replaceSuperclass
        set(value) { extends.replaceSuperclass = value }
    override var replaceSuperInterfaces: Boolean
        get() = extends.replaceSuperInterfaces
        set(value) { extends.replaceSuperInterfaces = value }

    override fun KClass<*>.unaryPlus() {
        if (this.java.isInterface) { throw DslException("extends { unaryPlus(+) Some::class only is for classes, NOT for interfaces!") }
        if (extends.replaceSuperclass) {
            extends.typeClassOrDslRef = EitherTypeOrDslRef.EitherKClass(this.asClassName(), false)
        } else {
            if (extends.typeClassOrDslRef is EitherTypeOrDslRef.ExtendsNothing) {
                extends.typeClassOrDslRef = EitherTypeOrDslRef.EitherKClass(this.asClassName(), false)
            } else {
                throw DslException("${dslExtendsDelegateImpl.parentRef} already extends ${extends.typeClassOrDslRef}")
            }
        }
    }

    override fun IDslRef.unaryPlus() {
        // TODO make sure DslRef is a class and NOT an interface
        if (extends.replaceSuperclass) {
            extends.typeClassOrDslRef = EitherTypeOrDslRef.EitherDslRef(this)
        } else {
            if (extends.typeClassOrDslRef is EitherTypeOrDslRef.ExtendsNothing) {
                extends.typeClassOrDslRef = EitherTypeOrDslRef.EitherDslRef(this)
            } else {
                throw DslException("${dslExtendsDelegateImpl.parentRef} already extends ${extends.typeClassOrDslRef}")
            }
        }
    }

    /** inherit from same SubElement Type (e.g. dto/table/...) with simpleName C.DEFAULT, of an element (e.g. model) in the same group which has this name */
    override operator fun String.unaryPlus() {
        val (dslModelgroup: DslModelgroup, dslModel: DslModel, onSubelementClass: ADslClass?) = when (dslExtendsDelegateImpl.parentRef) {
            is DslRef.ISubElementLevel -> Triple(dslCtx.ctxObj<DslModelgroup>(dslExtendsDelegateImpl.parentRef.parentRef.parentRef), dslCtx.ctxObj<DslModel>(dslExtendsDelegateImpl.parentRef.parentRef), dslCtx.ctxObj<ADslClass>(dslExtendsDelegateImpl.parentRef))
            is DslRef.IElementLevel    -> Triple(dslCtx.ctxObj<DslModelgroup>(dslExtendsDelegateImpl.parentRef.parentRef), dslCtx.ctxObj<DslModel>(dslExtendsDelegateImpl.parentRef), null)
            else -> throw DslException("extends on neither IElementLevel nor on ISubElementLevel")
        }
        // TODO hardcoded: possible only on modelgroup by now
        val reffedModel = dslModelgroup.dslModels.firstOrNull { it.simpleName == this }
        if (reffedModel != null) {
            reffedModel.selfDslRef.unaryPlus()
        } else {
            if (onSubelementClass == null) throw DslException("as extends on Element (model) I cannot deduce which subelement type you want to reference")
            else when (onSubelementClass) {
                is DslDto -> dslModel.dslDtos[this]?.selfDslRef?.unaryPlus() ?: throw DslException("no '$this' dto found in $dslModel")
                is DslTable -> dslModel.dslTables[this]?.selfDslRef?.unaryPlus() ?: throw DslException("no '$this' table found in $dslModel")
            }
        }
    }

    override fun KClass<*>.unaryMinus() {
        if ( ! this.java.isInterface) { throw DslException("extends { unaryMinus(-) Some::class only is for interfaces, NOT for classes!") }
        if (extends.replaceSuperInterfaces) {
            extends.superInterfaces.clear()
            extends.superInterfaces.add(EitherTypeOrDslRef.EitherKClass(this.asClassName(), true))
        } else {
            extends.superInterfaces.add(EitherTypeOrDslRef.EitherKClass(this.asClassName(), true))
        }
    }

    override fun IDslRef.unaryMinus() {
        // TODO make sure DslRef is an interface and NOT a class
        if (extends.replaceSuperInterfaces) {
            extends.superInterfaces.clear()
            extends.superInterfaces.add(EitherTypeOrDslRef.EitherDslRef(this))
        } else {
            extends.superInterfaces.add(EitherTypeOrDslRef.EitherDslRef(this))
        }
    }

    // TODO really not sure if this works ... definitely depends on EitherTypeOrDslRef's implementation of equals and hashCode throughout all its sealed cases
    override fun IDslApiExtendsProps.minusAssign(kClass: KClass<*>) {
        when (this@DslExtendsBlockImpl.extends.typeClassOrDslRef) {
            is EitherTypeOrDslRef.EitherDslRef -> { }
            is EitherTypeOrDslRef.EitherKClass -> {
                if ((this@DslExtendsBlockImpl.extends.typeClassOrDslRef as EitherTypeOrDslRef.EitherKClass).typeName == kClass) {
                    this@DslExtendsBlockImpl.extends.typeClassOrDslRef = EitherTypeOrDslRef.NOTHING
                } else {
                    val superInterface = this@DslExtendsBlockImpl.extends.superInterfaces.firstOrNull { it is EitherTypeOrDslRef.EitherKClass && it.isInterface && it.typeName == kClass.asTypeName() }
                    if ( superInterface != null ) {
                        this@DslExtendsBlockImpl.extends.superInterfaces.remove(superInterface)
                    }
                }
            }
            is EitherTypeOrDslRef.ExtendsNothing -> { }
        }
    }

    // TODO really not sure if this works ... definitely depends on EitherTypeOrDslRef's implementation of equals and hashCode throughout all its sealed cases
    override fun IDslApiExtendsProps.minusAssign(dslRef: DslRef) {
        when (this@DslExtendsBlockImpl.extends.typeClassOrDslRef) {
            is EitherTypeOrDslRef.EitherDslRef -> {
                if ((this@DslExtendsBlockImpl.extends.typeClassOrDslRef as EitherTypeOrDslRef.EitherDslRef).dslRef == dslRef) {
                    this@DslExtendsBlockImpl.extends.typeClassOrDslRef = EitherTypeOrDslRef.NOTHING
                } else {
                    val superInterface = this@DslExtendsBlockImpl.extends.superInterfaces.firstOrNull { it is EitherTypeOrDslRef.EitherDslRef && it.isInterface && it.dslRef == dslRef }
                    if ( superInterface != null ) {
                        this@DslExtendsBlockImpl.extends.superInterfaces.remove(superInterface)
                    }
                }
            }
            is EitherTypeOrDslRef.EitherKClass -> { }
            is EitherTypeOrDslRef.ExtendsNothing -> { }
        }
    }

    override fun String.not() {
        TODO("Not yet implemented")
    }

    override fun IDslApiExtendsProps.rem(docs: CodeBlock) {
        TODO("Not yet implemented")
    }

    // ===================
    // === ModelRefing ===
    // ===================

    override fun DslRef.model.MODELELEMENT.of(thisModelgroupSubElementSimpleName: String): IDslRef {
        if (dslExtendsDelegateImpl.parentRef.parentRef is DslRef.IElementLevel && this == DslRef.model.MODELELEMENT.MODEL) {
            throw DslException("extends directly on model|api|..., we cannot determine what subelement (dto, table, ...) to extend! (use e.g.: '+ (DTO of $this)'")
        }
        // so we are an ISubElementLevel here, or an IElementLevel, but MODELELEMENT is NOT Model
        val modelgroupDslClass =  if (dslExtendsDelegateImpl.parentRef.parentRef is DslRef.IElementLevel) {
            dslCtx.ctxObj<DslModelgroup>(dslExtendsDelegateImpl.parentRef.parentRef.parentRef)
        } else {
            dslCtx.ctxObj<DslModelgroup>(dslExtendsDelegateImpl.parentRef.parentRef.parentRef.parentRef)
        }
        // TODO hardcoded: possible only on modelgroup by now
        val dslModel = modelgroupDslClass.dslModels.firstOrNull { it.simpleName == thisModelgroupSubElementSimpleName } ?: throw DslException("ref: '${dslExtendsDelegateImpl.parentRef} +\"$thisModelgroupSubElementSimpleName\" no model with simpleName \"$thisModelgroupSubElementSimpleName\" found in that modelgroup")
        //// parent DslClass of extends { }
        //val elementLevelDslClass = dslExtendsDelegateImpl.dslCtxWrapper.dslCtx.getDslClass(dslExtendsDelegateImpl.parentRef.parentRef)
        val elementLevelDslClass = dslModel

        // TODO after refactorings
        // I think we have to get some(!?) named DslExtendsDelegate of the ElementLevel that we are referring to
        // and take the MODELELEMENT THAT one is referring to

        //// if MODELELEMENT.MODEL, translate to whatever this extends' subelement (dto/table/...) MODELELEMENT is
        //val modelelementToRef = when (this) {
        //    DslRef.model.MODELELEMENT.MODEL -> { (dslExtendsDelegateImpl.parent as IDslImplModelAndModelSubElementsCommon).modelElement }
        //    else -> this
        //}
        val modelelementToRef = this // <-- to make it compile
        // sentinel exhaustive when to get a compile error here, when any of the following enums is altered
        when (DslRef.MODELGROUP_MODEL_SUBELEMENTLEVEL.any) {DslRef.MODELGROUP_MODEL_SUBELEMENTLEVEL.DTO, DslRef.MODELGROUP_MODEL_SUBELEMENTLEVEL.TABLE -> {} }
        when (DslRef.APIGROUP_API_SUBELEMENTLEVEL.any) {DslRef.APIGROUP_API_SUBELEMENTLEVEL.APIFUN -> {} }
        val subelementLevelRef = when (modelelementToRef) {
            DslRef.model.MODELELEMENT.MODEL -> { throw DslException("should have been handled by above's when()") }
            DslRef.model.MODELELEMENT.DTO -> {   DslRef.dto(C.DEFAULT, elementLevelDslClass.selfDslRef) }
            DslRef.model.MODELELEMENT.TABLE -> { DslRef.table(C.DEFAULT, elementLevelDslClass.selfDslRef) }
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

    override fun DslRef.model.MODELELEMENT.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementDefault {
        if (dslExtendsDelegateImpl.parentRef is DslRef.IElementLevel && this != DslRef.model.MODELELEMENT.MODEL) {
            throw DslException("extends:  'MODEL inModelgroup \"$otherModelgroupSimpleName\"' directly on model|api|..., we cannot determine what subelement (dto, table, ...) to extend! (use e.g.: '+ (DTO inModelgroup $otherModelgroupSimpleName)'")
        }

        val dslModelgroup = try {
            dslCtx.getModelgroupBySimpleName(otherModelgroupSimpleName)
        } catch (e: DslCtxException) {
            throw DslException("ref: '${dslExtendsDelegateImpl.parentRef}' ${e.message}")
        }

        return OtherModelgroupSubelementDefault(this, dslModelgroup)
    }

    override fun OtherModelgroupSubelementDefault.withModelName(modelName: String): IDslRef {
        val modelgroupDslClass = this.dslModelgroup
        // TODO hardcoded: possible only on modelgroup by now
        val dslModel = modelgroupDslClass.dslModels.firstOrNull { it.simpleName == modelName } ?: throw DslException("ref: '${dslExtendsDelegateImpl.parentRef} +\"${this.dslModelgroup}\" extends '${this.defaultOfModelelement}' with simplename '$modelName' ref not found in dslCtx!")
        //// parent DslClass of extends { }
        //val elementLevelDslClass = dslExtendsDelegateImpl.dslCtxWrapper.dslCtx.getDslClass(dslExtendsDelegateImpl.parentRef.parentRef)
        val elementLevelDslClass = dslModel

        // TODO after refactorings same as above method

        //// if MODELELEMENT.MODEL, translate to whatever this extends' subelement (dto/table/...) MODELELEMENT is
        //val modelelementToRef = when (this.defaultOfModelelement) {
        //    DslRef.model.MODELELEMENT.MODEL -> { (dslExtendsDelegateImpl.parent as IDslImplModelAndModelSubElementsCommon).modelElement }
        //    else -> this.defaultOfModelelement
        //}
        val modelelementToRef = this.defaultOfModelelement // <-- to make it compile
        // sentinel exhaustive when to get a compile error here, when any of the following enums is altered
        when (DslRef.MODELGROUP_MODEL_SUBELEMENTLEVEL.any) {DslRef.MODELGROUP_MODEL_SUBELEMENTLEVEL.DTO, DslRef.MODELGROUP_MODEL_SUBELEMENTLEVEL.TABLE -> {} }
        when (DslRef.APIGROUP_API_SUBELEMENTLEVEL.any) {DslRef.APIGROUP_API_SUBELEMENTLEVEL.APIFUN -> {} }
        val subelementLevelRef = when (modelelementToRef) {
            DslRef.model.MODELELEMENT.MODEL -> { dslModel.selfDslRef }
            DslRef.model.MODELELEMENT.DTO -> {   DslRef.dto(C.DEFAULT, elementLevelDslClass.selfDslRef) }
            DslRef.model.MODELELEMENT.TABLE -> { DslRef.table(C.DEFAULT, elementLevelDslClass.selfDslRef) }
        }
        return subelementLevelRef
    }
}
