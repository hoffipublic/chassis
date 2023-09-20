---
layout: page
title: Dsl Conventions
subtitle: DslApi..., DslImpl, Dsl...DelegateImpl, Dsl...BlockImpl
menubar: data_menu_chassis
toc: true
show_sidebar: false
hero_image: ../../assets/Chassis.png
---
# Chassis Dsl Conventions

[link to DSL docs]({{ site.baseurl }}{% link dsl/dsl.md %})

## implementing a DSL node

A Class implementing `someNode { ... }` always is prefixed with `DslImpl`

and it's first to constructor args are

- val simpleName: String
- val someNodeRef: IDslRef

It extens the abstract base class (of all DslImpl's) `ADslClass`.

And implements its `IDslApiXxx` interfaces (which are used in the trailing lambda functions) e.g.:<br/>
`override fun dslNodeName(simpleName: String, dslBlock: IDslApiXxx.() -> Unit) {`

By letting the trailing lambda operate on an `interface` `IDslApi` we ensure that nothing else than defined in the IDslApi
is callable in the Chassis DSL (e.g. `val log` might be visible in the Dsl if `fun dslNodeName(..., dslBlock: DslImplSomeNode.() -> Unit) {`)

```kotlin
context(DslCtxWrapper)
class DslImplSomeNode(
    val simpleName: String,
    val someNodeRef: IDslRef,
    ...
) : ADslClass(),
    IDslApiModel,
    IdslApiSomeOther,
{
    val log = LoggerFactory.getLogger(javaClass)
    override val selfDslRef = modelRef

    override fun dto(simpleName: String, dslBlock: IDslApiSomeNode.() -> Unit) {
        val dslSomeNodeImpl: DslImplSomeNode = dslCtx.ctxObjCreate... { DslImplSomeNode(simpleName, DslRef.someNode(simpleName, selfDslRef)) }
    }
```

Any `DslApi...` must inherit from the top-level `IDslApi` as this has the `@DslMarker`

```kotlin
/** DSL Contributing funcs and props (to get "DSL scoped"/@DslMarker marked) */
@ChassisDslMarker
interface IDslApi
```

```kotlin
/** all classes participating in the chassis DSL language must have this annotation for scope control</br>
 * see https://kotlinlang.org/docs/type-safe-builders.html#scope-control-dslmarker */
@DslMarker annotation class ChassisDslMarker(vararg val impls: KClass<out IDslParticipator>)
```

`DslImplXxx` hierarchies and their `IDslApiXxx` Interface hierarchies can be a bit "overwhelming",
I highly recommend using the Intellij `Type Hierarchy` action.
