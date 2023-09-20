---
layout: page
title: DSL Block Delegation
subtitle: IDslApi by dslSomethingDelegateImpl
toc: true
show_sidebar: false
hero_image: ../../assets/Chassis.png
---
# Dsl Block Delegation

[link to DSL docs]({{ site.baseurl }}{% link dsl/dsl.md %})

Chassis DSL tries to use the DRY principle (Don't Repeat Yourself) also for implementation of sub-DSL Structures,
that may appear below multiple different DSL nodes.

Let's take a `showcase { ... }` substructure that may appear inside a `model` as well as under a `dto`, 

```kotlin
    model("someModel") {
        showcase {
            ...
        }
        dto {
            showcase {
                ...
            }
        }
    }
```
Chassis solves this, by giving (creating via dslCtx) a delegate Instance (DslDelegateImpl) `dslShowcaseDelegateImpl`
which implements its corresponding `IDslApiShowcase` and then delegates all sub-DSL Structure calls to it with `IDslApiShowcase by dslShowcaseDelegateImpl`.
<br/>If you don't know about kotlin interface delegation see the [Kotlin Docs](https://kotlinlang.org/docs/delegation.html){:target="_blank"}

```kotlin
context(DslCtxWrapper)
class DslModel(
    val simpleName: String,
    val modelRef: DslRef.model,
    val dslShowcaseDelegateImpl: DslShowcaseDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.showcase(simpleName, modelRef)),
) : ADslClass(),
    IDslApiModel,
    IDslApiShowcase by dslShowcaseDelegateImpl
{
    val log = LoggerFactory.getLogger(javaClass)
    override val selfDslRef = modelRef
```

The `DslShocaseDelegateImpl` then implements the delegated node itself (NOT what is INSIDE that node!!!):

```kotlin
/** outer scope */
context(DslCtxWrapper)
class DslShowcaseDelegateImpl(
    simpleNameOfDelegator: String,
    delegatorRef: IDslRef
) : ADslDelegateClass(simpleNameOfDelegator, delegatorRef), IDslImplShowcaseDelegate {
    override fun toString() = "${this::class.simpleName}(${theShowcaseBlocks.size})"
    val log = LoggerFactory.getLogger(javaClass)
    override val selfDslRef = DslRef.showcase(simpleNameOfDelegator, delegatorRef)

    /** different gathered dsl data holder for different simpleName's inside the BlockImpl's */
    override var theShowcaseBlocks: MutableMap<String, DslShowcaseBlockImpl> = mutableMapOf()

    /** DslBlock funcs always operate on IDslApi interfaces */
    override fun showcase(simpleName: String, block: IDslApiShowcaseBlock.() -> Unit) {
        val dslImpl = theShowcaseBlocks.getOrPut(simpleName) { DslShowcaseBlockImpl(simpleName, selfDslRef) }
        dslImpl.apply(block)
    }
}
```

the `DslShowcaseDelegateImpl` also holds one or more properties which the inner logic of the delegated node:

```kotlin
/** inner scope */
context(DslCtxWrapper)
class DslShowcaseBlockImpl(
    val simpleName: String,
    override val selfDslRef: IDslRef // that should be the Delegate of this and NOT the parentRef in the Dsl
) : ADslClass(), IDslImplShowcaseBlock
{
    override fun toString() = "${this::class.simpleName}(${dslShowcasePropsData})"
    val log = LoggerFactory.getLogger(javaClass)
    // back reference to own DelegateImpl (from context or as function parameter)
    val showcaseDelegate: DslShowcaseDelegateImpl = dslCtx.ctxObj(selfDslRef)
```

This way any DSL node that wants to have the `showcase` node, just needs to add it to its constructor (creating it via context in its initializer)
and delegate the `DslApiShowcase` to that constructor argument.
