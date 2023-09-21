---
layout: page
title: Dsl Reffing
subtitle: how to reference thing from other nodes
menubar: data_menu_chassis
toc: false
show_sidebar: false
hero_image: ../assets/Chassis.png
---
# DslRef and Referencing Chassis DSL Elements

There are several ways to reference another node in Chassis DSL..

## Reffing: using a string representation of the DslRef

`object DslRefString` (inside `DslRef.kt`) offers some conversion of absolute full DslRef Strings (with or without DslDiscriminator)
to valid typed DslRef's which you then could stuff into the DSL funcs as you like.

(but DslRefString is very basic right now, needs overhaul to use generic inline funcs to do so in the future)

**reffing inside the same modelgroup node**

tbd


## Reffing: using infix methods of `IDslApiModelReffing`

DslImpls that implement `IDslApiModelReffing` have some infix convenience function for (more or less) typesafe model reffing:

```kotlin
interface IDslApiModelReffing {
    infix fun MODELREFENUM.of(thisModelgroupSubElementRef: IDslRef): DslRef.IModelOrModelSubelement
    infix fun MODELREFENUM.of(thisModelgroupsModelSimpleName: String): DslRef.IModelOrModelSubelement // + for super class (referencing this@modelgroup's name of ModelSubElement MODELELEMENT.(DTO|TABLE)
    infix fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault // + for super class
    infix fun OtherModelgroupSubelementWithSimpleNameDefault.withModelName(modelName: String): IDslRef
}
```

There is also a `class DslImplModelReffing(val dslClass: ADslClass) : IDslApiModelReffing`
that implements these so that the DslImpl doesn't have to implement the interface by itself over and over again.

Inside Chassis DSL nodes that implement `IDslApiModelReffing` you can get a DslRef by doing:

```kotlin
    val aModelDslRef = (MODEL inModelgroup "someOtherModelgroupName" withModelName "someModelName")
    val aDtoDslRef   = (DTO   inModelgroup "someOtherModelgroupName" withModelName "someModelName")
    val aTableDslRef = (TABLE inModelgroup "someOtherModelgroupName" withModelName "someModelName")

    // referencing with the same Chassis DSL modelgroup:
    val aModelDslRef = (MODEL of "sameModelgroupModelName")
    val aDtoDslRef   = (DTO   of "sameModelgroupModelName")
    val aTableDslRef = (TABLE of "sameModelgroupModelName")
```

as non-parenthesis'ed infix functions are evaluated from left to right,
you have to take care to set parenthesises the right way.

E.g. for a `prefixed` `crud` `READ` with `deepRestrictions`:

```kotlin
    tableFor(DTO) {
        crud {
            prefixed("woModels") {
                // left to right, so you have to use parenthesis'es around the DslRef
                CRUDALL FOR (DTO inModelgroup ENTITYGROUP withModelName ENTITY__SOMEMODEL) deepRestrictions {
                    // left to right, so you have to use parenthesis'es around the DslRef
                    NEW model (DTO of ENTITY__SUBENTITY)
                }
            }
        }
    }
```
