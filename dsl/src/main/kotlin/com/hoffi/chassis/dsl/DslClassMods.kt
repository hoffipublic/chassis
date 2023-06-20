package com.hoffi.chassis.dsl

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.ADslDelegateClass
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import org.slf4j.LoggerFactory

/** gathered dsl data holder */
class ClassModProps(
    var forGen: Int = 0,
    var dslDerivedData: String = C.DEFAULT
) {
    override fun toString() = "ClassModProps(forGen=$forGen, dslDerivedData='$dslDerivedData')"
    override fun equals(other: Any?) = if (this === other) true else { if (other !is ClassModProps) false else forGen == other.forGen }
    override fun hashCode() = forGen
}

// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===

/** props/fields and "direct/non-inner-dsl-block" funcs inside dsl block */
@ChassisDslMarker
interface IDslApiClassModProps {
    var dslProp: Int
    operator fun String.unaryPlus()
    operator fun String.unaryMinus()
    operator fun IDslApiClassModProps.minusAssign(s: String)
    operator fun String.not()
    operator fun IDslApiClassModProps.rem(rem: String)
}
/** the "outermost" dsl block fun, that opens up this new "scope-hierarchy" (doesn't hold gathered DSL data by itself) */
@ChassisDslMarker
interface IDslApiClassModsDelegate {
    /** default dsl block's simpleName */
    fun classMods(simpleName: String = C.DEFAULT, block: IDslApiClassModsBlock.() -> Unit)
}
/** would contain "inner" nested Dsl block scopes, and implements the props/directFuns */
@ChassisDslMarker
interface IDslApiClassModsBlock : IDslApiClassModProps {
}

// === Impl Interfaces (extend IDslApis plus methods and props that should not be visible from the DSL ===

interface IDslImplClassModProps : IDslApiClassModProps {
    /** contains its (simpleName specific) data holder */
    var classModProps: ClassModProps
}
interface IDslImplClassModsDelegate : IDslApiClassModsDelegate {
    /** contains all (simpleName specific) data holders */
    var theClassModProps: MutableMap<String, DslClassModsBlockImpl>
}
/** the (per simpleName) inner implementation of the DslBlock */
interface IDslImplClassModsBlock : IDslImplClassModProps, IDslApiClassModsBlock

// === classes that implement IDslImpl lambda block functions

/** outer scope */
context(DslCtxWrapper)
class DslClassModsDelegateImpl(
    simpleNameOfParentDslBlock: String,
    parentRef: IDslRef
) : ADslDelegateClass(simpleNameOfParentDslBlock, parentRef), IDslImplClassModsDelegate {
    val log = LoggerFactory.getLogger(javaClass)
    override fun toString() = "${super@DslClassModsDelegateImpl.toString()}->[$theClassModProps]"

    override val selfDslRef = DslRef.classMods(simpleNameOfParentDslBlock, parentRef)

    /** different gathered dsl data holder for different simpleName's inside the BlockImpl's */
    override var theClassModProps: MutableMap<String, DslClassModsBlockImpl> = mutableMapOf()

    /** DslBlock funcs always operate on IDslApi interfaces */
    override fun classMods(simpleName: String, block: IDslApiClassModsBlock.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        val simpleNameClassMods = theClassModProps.getOrPut(simpleName) { DslClassModsBlockImpl(simpleName, DslRef.classMods(simpleName, parentRef)) }
        simpleNameClassMods.apply(block)
    }
}

// === impl object callable _inside_ the lambda block fun ===

/** inner scope */
class DslClassModsBlockImpl(val simpleName: String, val classModsRef: DslRef.classMods) : IDslImplClassModsBlock {
    override var dslProp: Int = -1
    override var classModProps = ClassModProps()

    override fun String.unaryPlus() {
        classModProps.dslDerivedData = this
    }
    override fun String.unaryMinus() {
        classModProps.dslDerivedData = this
    }

    override fun IDslApiClassModProps.minusAssign(s: String) {
        this@DslClassModsBlockImpl.classModProps.dslDerivedData = s
    }

    override fun String.not() {
        classModProps.dslDerivedData = "before '${classModProps.dslDerivedData}' got '$this' -> opposite of what it was before"
    }

    override fun IDslApiClassModProps.rem(rem: String) {
        this@DslClassModsBlockImpl.classModProps.dslDerivedData += " % $rem"
    }
}
