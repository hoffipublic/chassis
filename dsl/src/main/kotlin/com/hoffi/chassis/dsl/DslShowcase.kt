package com.hoffi.chassis.dsl

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.*
import com.hoffi.chassis.dsl.modelgroup.DslDto
import com.hoffi.chassis.dsl.modelgroup.DslModel
import com.hoffi.chassis.dsl.modelgroup.DslTable
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import org.slf4j.LoggerFactory

/** usage:
 * The interfaces and classes herein define a DSL "sub block" that can be "attached" by delegation to any @ChassisDslMarker class.
 *
 * Conventions:
 *
 * - IDslApiXxx interfaces are "exposed" in the DSL [@DslBlockOn(DslXxx::class)] funcs lambda clauses,
 *   e.g. fun showcase(simpleName: String, block: IDslApiShowcaseBlock.() -> Unit)
 * - IDslImplXxx interfaces implement the above IDslApiXxx interfaces and contain props/fields/funcs that are use by Impls
 *   and SHOULD NOT be exposed to the DSL
 * - As/If the "outermost" sub-DSL-"structure" can appear multiple times with different simpleNames, e.g.:
 *   outer {
 *     showcase {
 *       // simpleName = "default"
 *     }
 *     showcase("special") {
 *       // simpleName = "special"
 *     }
 *     ...
 *   }
 *   we need to have TWO DslApi interfaces (and DslImpl's and Impl's),
 *   - DslApiXxx      (the scope opening one that is attached/delegated to by the @ChassisDslMarker class that wants to "use" it
 *   - DslApiXxxBlock (the scope actually implementing the Dsl xxx props/fields/funcs/subDsl sub-DSL-structure
 * - in the "multiple" case, the IDslImplXxx must maintain a MutableMap<simpleNameString, DslXxxBlockImpl>
 *   which also comes in handy, as if this map isEmpty, then the xxx block was not present on the parent.
 * - and the DslXxxBlockImpl must get "its" DslXxxBlockImpl to operate on
 *   (either it IS the object from the map itself or it has  to get it via its constructor
 *
 * - classes actually implementing the DslImpl interfaces follow the convention `DslXxx(Block)?Impl
 *
 * Usage:
 *
 * other @ChassisDslMarker classes that want to make use of the xxx sub-DSL-structure, by
 * - a) exposing the DslApiXxx in their own DslApiSomeparent (meaning adding DslApiXxx to the interfaces that DslApiSomeparent extends)
 *   and then just can delegate to an instance of the DslXxxImpl, e.g. by interface delegation via constructor arg default value:
 * - b) DslSomeparentImpl(
 *        ...,
 *        val xxxImpl: DslXxxImpl = DslXxxImpl(simpleName, DslRef.xxx(simpleName, someparentRef)
 *      ) : IDslImplSomeparent, IDslImplXxx by xxxImpl
 *
 * Accessing the payload data inside the DslXxxBlockImpl:
 *
 * let's say you have an @ChassisDslMarker instance, e.g.:
 *     val element: IDslClass = aDslRun.dslCtx.allModelInstances.entries.first { it.key == dslRef }.value
 *     val model = element as DslModel
 *     val xxxPropsDefault = model.xxxImpl.theXxxProps["default"]?.xxxProps ?: XxxProps()
 *     val derivedData = xxxPropsDefault.dslDerivedData
 *     println("derivedData: '$derivedData'")
 *
 */

/** gathered dsl data holder */
class ShowcaseProps(
    var forGen: Int = 0,
    var dslDerivedData: String = C.DEFAULT
) {
    override fun toString() = "ShowcaseProps(forGen=$forGen, dslDerivedData='$dslDerivedData')"
    override fun equals(other: Any?) = if (this === other) true else { if (other !is ShowcaseProps) false else forGen == other.forGen }
    override fun hashCode() = forGen
}

// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===

/** props/fields and "direct/non-inner-dsl-block" funcs inside dsl block */
@ChassisDslMarker
interface IDslApiShowcaseProps {
    var dslProp: Int
    operator fun String.unaryPlus()
    operator fun String.unaryMinus()
    operator fun IDslApiShowcaseProps.minusAssign(s: String)
    operator fun String.not()
    operator fun IDslApiShowcaseProps.rem(rem: String)
}
/** the "outermost" dsl block fun, that opens up this new "scope-hierarchy" (doesn't hold gathered DSL data by itself) */
@ChassisDslMarker
interface IDslApiShowcaseDelegate {
    /** default dsl block's simpleName */
    @DslBlockOn(DslModel::class, DslDto::class, DslTable::class) // IDE clickable shortcuts to implementing @ChassisDslMarker classes
    fun showcase(simpleName: String = C.DEFAULT, block: IDslApiShowcaseBlock.() -> Unit)
}
/** would contain "inner" nested Dsl block scopes, and implements the props/directFuns */
@ChassisDslMarker
interface IDslApiShowcaseBlock : IDslApiShowcaseProps {
}

// === Impl Interfaces (extend IDslApis plus methods and props that should not be visible from the DSL ===

interface IDslImplShowcaseProps : IDslApiShowcaseProps {
    /** contains its (simpleName specific) data holder */
    var showcaseProps: ShowcaseProps
}
interface IDslImplShowcaseDelegate : IDslApiShowcaseDelegate {
    /** contains all (simpleName specific) data holders */
    var theShowcaseBlocks: MutableMap<String, DslShowcaseBlockImpl>
}
/** the (per simpleName) inner implementation of the DslBlock */
interface IDslImplShowcaseBlock : IDslImplShowcaseProps, IDslApiShowcaseBlock

// === classes that implement IDslImpl lambda block functions

/** outer scope */
context(DslCtxWrapper)
class DslShowcaseDelegateImpl(
    simpleNameOfParentDslBlock: String,
    parentRef: IDslRef
) : ADslDelegateClass(simpleNameOfParentDslBlock, parentRef), IDslImplShowcaseDelegate {
    val log = LoggerFactory.getLogger(javaClass)
    override val selfDslRef = DslRef.showcase(simpleNameOfParentDslBlock, parentRef)

    /** different gathered dsl data holder for different simpleName's inside the BlockImpl's */
    override var theShowcaseBlocks: MutableMap<String, DslShowcaseBlockImpl> = mutableMapOf()

    /** DslBlock funcs always operate on IDslApi interfaces */
    override fun showcase(simpleName: String, block: IDslApiShowcaseBlock.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            dslCtx.PASS_1_BASEMODELS -> {
                val dslImpl = theShowcaseBlocks.getOrPut(simpleName) { DslShowcaseBlockImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            else -> {}
        }
    }
}

// === impl object callable _inside_ the lambda block fun ===

/** inner scope */
context(DslCtxWrapper)
class DslShowcaseBlockImpl(
    val simpleName: String,
    parentRef: IDslRef // that should be the Delegate of this and NOT the parentRef in the Dsl
)
    : ADslClass(), IDslImplShowcaseBlock
{
    override val selfDslRef = DslRef.showcase(simpleName, parentRef)

    override var dslProp: Int = -1
    override var showcaseProps = ShowcaseProps()

    override fun String.unaryPlus() {
        showcaseProps.dslDerivedData = this
    }
    override fun String.unaryMinus() {
        showcaseProps.dslDerivedData = this
    }

    override fun IDslApiShowcaseProps.minusAssign(s: String) {
        this@DslShowcaseBlockImpl.showcaseProps.dslDerivedData = s
    }

    override fun String.not() {
        showcaseProps.dslDerivedData = "before '${showcaseProps.dslDerivedData}' got '$this' -> opposite of what it was before"
    }

    override fun IDslApiShowcaseProps.rem(rem: String) {
        this@DslShowcaseBlockImpl.showcaseProps.dslDerivedData += " % $rem"
    }
}
