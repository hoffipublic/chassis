---
layout: page
title: GenModel, ModelClassDataFromDsl,<br/>ModelClassName and<br/>EitherTypOrModelOrPoetTyp
subtitle: basic things to work with for code generation
menubar: data_menu_chassis
toc: true
show_sidebar: false
hero_image: ../assets/Chassis.png
---
# bite-sized immutable Chassis DSL parsing result

Any "thing" you might want to generate code for eventual has a gazillion of variants, characteristics and subfeatures.

This usually leads to having a gazillion of when, if, then decisions in code generation code.

The following "two" things are, how Chassis tries to tackle this challenge.

## sealed class GenModel

`sealed class GenModel` is the *(immutable!)* bite-sized gathered and populated information about a Chassis DSL `model { ... }`

The "challenge" here is, that models can be all so different (Typ, model, class, poetType, collection, mutable, nullable, interface, ...)

For each model subelement (DTO, DCO, TABLE, ...) there is a sealed implementation of `GenModel`.

sealed class GenModel extends `ModelClassDataFromDsl`. Chassis tries to achieve a "uniform" programming experience this way.

As you can see you'll almost never will work with the `GenModel` itself, but just "passing it around" as GenModel ...
but 99,9% of the time just using its `ModelClassDataFromDsl`

You can consider `ModelClassDataFromDsl` as being the always alike **"flesh"** of any GenModel.

Any naming, path package (that is `nameAndWhereto { ... }` related information + naming Strategies stuff)<br/>
is delegated to a GenModel's `ModelClassName`

```kotlin
sealed class GenModel(modelSubElRef: DslRef.IModelSubelement, modelClassName: ModelClassName)
    : ModelClassDataFromDsl(modelSubElRef, modelClassName) {
    class DtoModelFromDsl(dtoRef: DslRef.dto, modelClassName: ModelClassName) : GenModel(dtoRef, modelClassName) { init { modelClassName.modelClassDataFromDsl = this } }
    class TableModelFromDsl(tableRef: DslRef.table, modelClassName: ModelClassName) : GenModel(tableRef, modelClassName) { init { modelClassName.modelClassDataFromDsl = this } }
    class DcoModelFromDsl(dcoRef: DslRef.dco, modelClassName: ModelClassName) : GenModel(dcoRef, modelClassName) { init { modelClassName.modelClassDataFromDsl = this } }
}

/** all props and sub-props are set on chassis DSL PASS_FINISH */
abstract class ModelClassDataFromDsl(
    var modelSubElRef: DslRef.IModelSubelement,
    val modelClassName: ModelClassName
) : Comparable<ModelClassDataFromDsl>,
    IModelClassName by modelClassName
{
    ...
}
```

## sealed class EitherTypOrModelOrPoetType

`sealed class EitherTypOrModelOrPoetType` represents the possible **Data Type** of a model's class or property.

Again the "challenge" here is, that a type can be many many things in any programming language.

Chassis tries to tackle this with uniform sealed classes also:

(note, that all these type representations also contain<br/>a "shortcut" to its `lateinit var modelClassName: ModelClassName` for convenience)

Both of Chassis' DSL variants of properties `KClass<*>` `ClassName` (KotlinPoet) are represented by `class EitherPoetType` 

```kotlin
sealed class EitherTypOrModelOrPoetType(override val initializer: Initializer) {
    lateinit var modelClassName: ModelClassName
    class EitherTyp(val typ: TYP, initializer: Initializer) : EitherTypOrModelOrPoetType(initializer)
    class EitherModel(val modelSubElementRefOriginal: DslRef.IModelOrModelSubelement, initializer: Initializer) : EitherTypOrModelOrPoetType(initializer)
    class EitherPoetType(val poetType: ClassName, override var isInterface: Boolean, initializer: Initializer) : EitherTypOrModelOrPoetType(initializer)
    class NOTHING : EitherTypOrModelOrPoetType(Initializer.EMPTY)
}
```
