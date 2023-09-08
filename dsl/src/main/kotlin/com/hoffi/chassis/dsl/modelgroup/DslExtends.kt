package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslDelegateClass
import com.hoffi.chassis.dsl.internal.DslBlockOn
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.internal.IDslApi
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType.Companion.createPoetType
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.Extends
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.hoffi.chassis.shared.whens.WhensDslRef
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asTypeName
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===

/** props/fields and "direct/non-inner-dsl-block" funcs inside dsl block */
interface IDslApiExtendsProps : IDslApi, IDslApiModelReffing { // TODO ModelReffing via delegated class (see DslGatherPropertiesDelegateImpl)
    var replaceSuperclass: Boolean
    var replaceSuperInterfaces: Boolean
    operator fun KClass<*>.unaryPlus()  // + for super class
    operator fun IDslRef.unaryPlus()    // + for super class
    /** inherit from same SubElement Type (e.g. dto/table/...) with simpleName C.DEFAULT, of an element (e.g. model) in the same group which has this name */
    operator fun String.unaryPlus()    // + for super class (referencing this@modelgroup's name of ModelSubElement of this@modelsubelement(thisSimpleName)
    //operator fun DslRef.model.MODELELEMENT.unaryPlus() // + for super class
    operator fun KClass<*>.unaryMinus() // - for super interfaces
    operator fun IDslRef.unaryMinus() // - for super interfaces
    operator fun String.unaryMinus() // - for super interfaces
    operator fun IDslApiExtendsProps.minusAssign(kClass: KClass<*>) // exclude from super class/interfaces
    operator fun IDslApiExtendsProps.minusAssign(dslRef: DslRef) // exclude from super class/interfaces
    operator fun String.not()
    operator fun IDslApiExtendsProps.rem(docs: CodeBlock)
}
/** the "outermost" dsl block fun, that opens up this new "scope-hierarchy" (doesn't hold gathered DSL data by itself) */
interface IDslApiExtendsDelegate : IDslApi {
    /** default dsl block's simpleName */
    // context(DslCtxWrapper) // see https://youtrack.jetbrains.com/issue/KT-57409/context-receivers-fail-if-implemented-via-delegated-interface
    @DslBlockOn(DslModel::class, DslDto::class, DslTable::class) // IDE clickable shortcuts to implementing @ChassisDslMarker classes
    fun extends(simpleName: String = C.DEFAULT, block: IDslApiExtendsBlock.() -> Unit)
}
/** would contain "inner" nested Dsl block scopes, and implements the props/directFuns */
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
            dslCtx.PASS_5_REFERENCING -> {
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
        if (this.java.isInterface) throw DslException("extends { unaryPlus(+) Some::class only is for classes, NOT for interfaces!")
        extends.superclassHasBeenSet = true
        if (extends.replaceSuperclass) {
            extends.typeClassOrDslRef = this.createPoetType()
        } else {
            if (extends.typeClassOrDslRef is EitherTypOrModelOrPoetType.NOTHING) {
                extends.typeClassOrDslRef = this.createPoetType()
            } else {
                throw DslException("${dslExtendsDelegateImpl.delegateRef} already extends ${extends.typeClassOrDslRef}")
            }
        }
    }

    override fun IDslRef.unaryPlus() {
        val refTarget: DslRef.IModelOrModelSubelement = WhensDslRef.whenModelOrModelSubelement (this,
            isModelRef = { this as DslRef.model },
            isDtoRef =   { this as DslRef.dto},
            isTableRef = { this as DslRef.table }
        ) {
            DslException(" in '${dslExtendsDelegateImpl.selfDslRef}' referring to '$this'")
        }

        val isInterface = dslCtx.isInterface(refTarget, dslExtendsDelegateImpl) // TODO might be wrong afterwards if MODEL to MODEL ref AND subelement has a (different) kind
        if (isInterface) throw DslException("extends { unaryPlus(+) Some::class only is for class, NOT for interfaces!")
        extends.superclassHasBeenSet = true
        if (extends.replaceSuperclass) {
            extends.typeClassOrDslRef = EitherTypOrModelOrPoetType.EitherModel(refTarget, Initializer.REFFED)
        } else {
            if (extends.typeClassOrDslRef is EitherTypOrModelOrPoetType.NOTHING) {
                extends.typeClassOrDslRef = EitherTypOrModelOrPoetType.EitherModel(refTarget, Initializer.REFFED)
            } else {
                throw DslException("${dslExtendsDelegateImpl.delegateRef} already extends ${extends.typeClassOrDslRef}")
            }
        }
    }

    /** inherit from same SubElement Type (e.g. dto/table/...) with simpleName C.DEFAULT, of an element (e.g. model) in the same group which has this name */
    override operator fun String.unaryPlus() {
        val refTarget: DslRef.IModelOrModelSubelement = WhensDslRef.whenModelOrModelSubelement(dslExtendsDelegateImpl.parentDslRef,
            isModelRef = { DslRef.modelRefFrom(dslExtendsDelegateImpl.selfDslRef, swappedModelSimpleName = this) },
            isDtoRef =   { DslRef.dtoRefFrom(dslExtendsDelegateImpl.selfDslRef, C.DEFAULT, swappedModelSimpleName = this) },
            isTableRef = { DslRef.tableRefFrom(dslExtendsDelegateImpl.selfDslRef, C.DEFAULT, swappedModelSimpleName = this) },
        ) {
            DslException("unknown model or modelSubelement")
        }
        refTarget.unaryPlus()
    }

    override fun KClass<*>.unaryMinus() {
        if ( ! this.java.isInterface) throw DslException("extends { unaryMinus(-) Some::class only is for interfaces, NOT for classes!")
        if (extends.replaceSuperInterfaces) {
            extends.superInterfaces.clear()
            extends.superInterfaces.add(this.createPoetType())
        } else {
            extends.superInterfaces.add(this.createPoetType())
        }
    }

    override fun IDslRef.unaryMinus() {
        val refTarget: DslRef.IModelOrModelSubelement = WhensDslRef.whenModelOrModelSubelement (this,
            isModelRef = { this as DslRef.model },
            isDtoRef =   { this as DslRef.dto},
            isTableRef = { this as DslRef.table }
        ) {
            DslException(" in '${dslExtendsDelegateImpl.selfDslRef}' referring to '$this'")
        }

        val isInterface = dslCtx.isInterface(refTarget, dslExtendsDelegateImpl)
        if ( ! isInterface) throw DslException("extends { unaryMinus(-) someDslRef } only is for interfaces, NOT for classes! '${dslExtendsDelegateImpl.selfDslRef}' is reffing non-interface '$this'")
        if (extends.replaceSuperInterfaces) {
            extends.superInterfaces.clear()
            extends.superInterfaces.add(EitherTypOrModelOrPoetType.EitherModel(refTarget, Initializer.REFFED))
        } else {
            extends.superInterfaces.add(EitherTypOrModelOrPoetType.EitherModel(refTarget, Initializer.REFFED))
        }
    }

    /** inherit from same SubElement Type (e.g. dto/table/...) with simpleName C.DEFAULT, of an element (e.g. model) in the same group which has this name */
    override operator fun String.unaryMinus() {
        var elementLevelDslRef = dslExtendsDelegateImpl.parentDslRef
        while (elementLevelDslRef !is DslRef.IElementLevel) {
            if (elementLevelDslRef.level == 1) throw DslException("no elementLevel dslRef in parents of ${dslExtendsDelegateImpl.selfDslRef}")
            elementLevelDslRef = elementLevelDslRef.parentDslRef
        }
        val groupRef = elementLevelDslRef.parentDslRef

        val refTarget: DslRef.IModelOrModelSubelement = WhensDslRef.whenModelOrModelSubelement(dslExtendsDelegateImpl.parentDslRef,
            isModelRef = { DslRef.model(this, groupRef) },
            isDtoRef =   { DslRef.dto(C.DEFAULT, DslRef.model(this, groupRef)) },
            isTableRef = { DslRef.table(C.DEFAULT, DslRef.model(this, groupRef)) },
        ) {
            DslException("unknown model or modelSubelement")
        }
        refTarget.unaryMinus()
    }

    // TODO really not sure if this works ... definitely depends on EitherTypeOrDslRef's implementation of equals and hashCode throughout all its sealed cases
    override fun IDslApiExtendsProps.minusAssign(kClass: KClass<*>) {
        when (this@DslExtendsBlockImpl.extends.typeClassOrDslRef) {
            is EitherTypOrModelOrPoetType.EitherModel -> { }
            is EitherTypOrModelOrPoetType.EitherPoetType -> {
                if ((this@DslExtendsBlockImpl.extends.typeClassOrDslRef as EitherTypOrModelOrPoetType.EitherPoetType).poetType == kClass.asTypeName()) {
                    this@DslExtendsBlockImpl.extends.typeClassOrDslRef = EitherTypOrModelOrPoetType.NOTHING
                } else {
                    val superInterface = this@DslExtendsBlockImpl.extends.superInterfaces.firstOrNull { it is EitherTypOrModelOrPoetType.EitherPoetType && it.isInterface && it.poetType == kClass.asTypeName() }
                    if ( superInterface != null ) {
                        this@DslExtendsBlockImpl.extends.superInterfaces.remove(superInterface)
                    }
                }
            }
            is EitherTypOrModelOrPoetType.NOTHING -> { }
            is EitherTypOrModelOrPoetType.EitherTyp -> throw DslException("cannot extend a predefined (more or less primitive) TYP")
        }
    }

    // TODO really not sure if this works ... definitely depends on EitherTypeOrDslRef's implementation of equals and hashCode throughout all its sealed cases
    override fun IDslApiExtendsProps.minusAssign(dslRef: DslRef) {
        when (this@DslExtendsBlockImpl.extends.typeClassOrDslRef) {
            is EitherTypOrModelOrPoetType.EitherModel -> {
                if ((this@DslExtendsBlockImpl.extends.typeClassOrDslRef as EitherTypOrModelOrPoetType.EitherModel).modelSubElementRef == dslRef) {
                    this@DslExtendsBlockImpl.extends.typeClassOrDslRef = EitherTypOrModelOrPoetType.NOTHING
                } else {
                    val superInterface = this@DslExtendsBlockImpl.extends.superInterfaces.firstOrNull { it is EitherTypOrModelOrPoetType.EitherModel && it.isInterface && it.modelSubElementRef == dslRef }
                    if ( superInterface != null ) {
                        this@DslExtendsBlockImpl.extends.superInterfaces.remove(superInterface)
                    }
                }
            }
            is EitherTypOrModelOrPoetType.EitherPoetType -> { }
            is EitherTypOrModelOrPoetType.NOTHING -> { }
            is EitherTypOrModelOrPoetType.EitherTyp -> throw DslException("cannot extend a predefined (more or less primitive) TYP")
        }
    }

    override fun String.not() {
        TODO("Not yet implemented")
    }

    override fun IDslApiExtendsProps.rem(docs: CodeBlock) {
        TODO("Not yet implemented")
    }

    // ====================
    // === ModelReffing ===
    // ====================

    val modelReffing = DslImplModelReffing(dslExtendsDelegateImpl)

    override fun MODELREFENUM.of(thisModelgroupSubElementRef: IDslRef): DslRef.IModelOrModelSubelement {
        return modelReffing.fakeOf(this, thisModelgroupSubElementRef)
    }

    override fun MODELREFENUM.of(thisModelgroupsModelSimpleName: String): DslRef.IModelOrModelSubelement {
        return modelReffing.fakeOf(this, thisModelgroupsModelSimpleName)
    }

    override fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault {
        return modelReffing.fakeInModelgroup(this, otherModelgroupSimpleName)
    }

    override fun OtherModelgroupSubelementWithSimpleNameDefault.withModelName(modelName: String): IDslRef {
        return modelReffing.fakeWithModelName(this, modelName)
    }
}
