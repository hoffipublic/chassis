---
layout: page
title: super classes and interfaces
subtitle: class extends
menubar: data_menu_chassis
toc: false
show_sidebar: false
hero_image: ../assets/Chassis.png
---
# model and model elements super classes and interfaces

Either a `model("modelName") { ... }` as well as its submoldels can specify that they extend a class or interface.

an `extends { ... }` node can use the `interface IDslApiExtendsProp` funcs:

```kotlin
var replaceSuperclass: Boolean
var replaceSuperInterfaces: Boolean
operator fun KClass<*>.unaryPlus()  // + for super class
operator fun IDslRef.unaryPlus()    // + for super class
/** inherit from same SubElement Type (e.g. dto/tableFor/...) with simpleName C.DEFAULT, of an element (e.g. model) in the same group which has this name */
operator fun String.unaryPlus()    // + for super class (referencing this@modelgroup's name of ModelSubElement of this@modelsubelement(thisSimpleName)
operator fun KClass<*>.unaryMinus() // - for super interfaces
operator fun IDslRef.unaryMinus() // - for super interfaces
operator fun String.unaryMinus() // - for super interfaces
operator fun IDslApiExtendsProps.minusAssign(kClass: KClass<*>) // exclude from super class/interfaces
operator fun IDslApiExtendsProps.minusAssign(dslRef: DslRef) // exclude from super class/interfaces
operator fun String.not()
operator fun IDslApiExtendsProps.rem(docs: CodeBlock)
```
`replaceSuperclass` and `replaceSuperInterfaces` specify if any previous (in higher node levels) set superclasses/superinterfaces
should be replaced by the ones of the current `extends { ... }` node.

If replaceSuperclass is false (the default) and the extends block sets a super class,
but a superclass already has been set on parsing a "higher up" node in the model hierarchy (e.g. `model { extends { ... } }`)
a DslException will be thrown. (TODO: maybe better to let the Strategy decide??)

<hr/>

[back to root](..)
