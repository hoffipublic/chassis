---
layout: page
title: DSL Referencing
subtitle: DslRef IDslRef
toc: true
show_sidebar: false
hero_image: ../../assets/Chassis.png
---
# Dsl Referencing with DslRef and IDslRef

The ability to reference any DSL node from any other DSL node is one of the key features of Chassis DSL.

To achieve it *any* `DslImpl` (no matter if a Delegate or a plain DslImpl) has to have a `simpleName: String` and a `selfDslRef: IDslRef`.

At its core a DslRef is a class in `sealed class DslRef(...) : ADslRef(...) {`<br/>
and<br/>
```kotlin
abstract class ADslRef(
    override val simpleName: String,
    override val parentDslRef: IDslRef,
    override val refList: MutableList<DslRef.DslRefAtom> = mutableListOf()
) : IDslRef {
```

```kotlin
data class DslRefAtom(val dslRefName: String, val simpleName: String = C.DEFAULT)
```

As a DslRef ist a listOf DslRefAtom's you easily see that the list represents uniquely the complete hierarchy in the Chassis Dsl.

The `toString()` representation of a `DslRef` clarifies the undlerlying principle ('|' separates DslRefAtom's and ':' separates the `nodeName` and its `simpleName`)<br/>
(you can ignore the leading DslDiscriminator ... the use of that one has been refactored out somewhere in the past)

```
"disc:commonBasePersistentDisc|modelgroup:Persistentgroup|model:entity|dto:default|showcase:default"
```

| Level | DslRefAtom nodeName | DslRefAtom simpleName    |
|:-----:|---------------------|--------------------------|
|   -   | DslDiscriminator    | commonBasePersistentDisc |  
|   1   | modelgroup          | Persistengroup           |
|   2   | model               | entity                   |
|   3   | dto                 | default                  |
|   4   | showcase            | default                  |

to be a bit shorter, if the DslRefAtom simplename is `default` then the `:simpleName` parts of each is ommited and shortens to:

```
"disc:commonBasePersistentDisc|modelgroup:Persistentgroup|model:entity|dto|showcase"
```

As any DslImpl has to have as first two constructor Arguments `simpleName: String, parentDslRef: IDslRef` (as these have to be passed to abstract super `class ADslClass`)<br/>
any `class DslImpl` can have a property `val selfDslRef` as

```kotlin
class DslImplXxx(...) : ADslClass(simpleName: String, parentDslRef: IDslRef) {
    override val selfDslRef = DslRef.nodeName(simpleName, parentDslRef)
}
```


the `companion object { ... }` of `class DslRef` also has some handy and neede convenience functions to extract lower level DslRefs from deeper nested DslRefs.

`object DslRefString {` (defined in the same file as `DslRef`) gives you some neat functions to convert the `DslRef.toString()` representation into an actual
`DslRef` of the right sealed DslRef class with the correct `List<DslRef.DslRefAtom>`.


[link to DSL docs]({{ site.baseurl }}{% link dsl/dsl.md %})

