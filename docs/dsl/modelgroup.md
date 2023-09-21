---
layout: page
title: node modelgroup { ... }
subtitle: Things that you define for all models<br/>and model elements (DTO, DCO, TABLEFOR)
menubar: data_menu_chassis
toc: true
show_sidebar: false
hero_image: ../assets/Chassis.png
---
# modelgroup: Things that you define for all models<br/>and model elements (DTO, DCO, TABLEFOR)

Tip: with alt/opt-RETURN you can import inner class/enum names statically. This can make your Chassis DSL much shorter and more readable!

For a more complete `nameAndWhereto { ... }` see [DSL block delegation showcase]({{ site.baseurl }}{% link dsl/modelNameAndWhereto.md %})

## modelgroup { ... }

```kotlin
    modelgroup("groupName") {            /** @see com.hoffi.chassis.dsl.modelgroup.DslModelgroup */
        nameAndWhereto {                 /** @see com.hoffi.chassis.dsl.whereto.IDslApiNameAndWheretoOnSubElements */
            classPrefix("Simple")
            packageName("entity")
            dtoNameAndWhereto {
                classPostfix("Dto")
                packageName("dto")
            }
            tableNameAndWhereto {
                classPostfix("Table")
                packageName("table")
            }
        }
        /** @see com.hoffi.chassis.dsl.modelgroupIDslApiKindClassObjectOrInterface */
        constructorVisibility = IDslApiConstructorVisibility.VISIBILITY.PROTECTED
        /** @see com.hoffi.chassis.dsl.modelgroup.IDslApiGatherPropertiesModelAndModelSubelementsCommon */
        propertiesOfSuperclasses /* do not extend superclasses/superinterfaces, but gather their properties */
        propertiesOf(MODELREFENUM.MODEL "modelgroupName" withModelName "modelName", GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
```

Things set on a `modelgroup { ... }` (eventually depending on the strategy) are considered to be defined for all containing models and submodels
and therefore can be omitted in them respectively.

### model

On defining property's also see [model properties]({{ site.baseurl }}{% link dsl/modelProps.md %}))

```kotlin
    modelgroup {
        model("modelName") {
            nameAndWhereto { /* see above */ }
            extends {
                replaceSuperclass = false
                replaceSuperInterfaces = false
                + (MODELREFENUM.DTO inModelgroup "modelgroupName" withModelName "persistentDTOname")
                - "modelName"   // unary minus ==> interface ; as no modelgroup is given, it is assumed that "this" modelgroup contains a model with this name
                - "otherModel"
                // minusAssign("ref:name|other:string") // not implemented yet
                // minusAssign()                        // not implemented yet
                // not()                                // not implemented yet
                // rem("...")                           // not implemented yet
            }
            /** @see com.hoffi.chassis.dsl.modelgroup.IDslApiGatherPropertiesModelAndModelSubelementsCommon */
            propertiesOfSuperclasses /* do not extend superclasses/superinterfaces, but gather their properties */
            propertiesOf(MODELREFENUM.MODEL "modelgroupName" withModelName "modelName", GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
            property("thePropName1", TYP.STRING, ..., Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER, Tag.PRIMARY)
            property("thePropName2", Dummy::class, ..., Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("thePropName3", ClassName("packageName", "ClassName"))
            property("someModelObject", DTO of "otherModelgroup", mutable)
        }
    }
```

A `model` is the named "outer" definition of a thing that can have multiple "incarnations".

E.g. a model "Entity" can be

- a kotlin class in form of a DTO (Data Transfer Object)
- a kotlin class in form of a DCO (Data Compute Object)
- the DB representation of a DTO (TableFor(DTO)) 
- the DB representation of a DCO (TableFor(DC0))
- ... (if more model subelements are defined)

Things set on a `model { ... }` (eventually depending on the strategy) are considered to be defined for all containing models and submodels
and therefore can be omitted in them respectively.<br/>
This is particularly useful for defining properties that all subelements have.


#### dto

model subelements are things that "really" get code generated for.

```kotlin
    modelgroup {
        model("name") {
            dto {
                /** everything that a model has */
                // plus e.g. props that only a dto has
                property("dtoSpecificProp", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER)

                //propertiesOf( (MODEL inModelgroup PERSISTENTGROUP withModelName PERSISTENT__PERSISTENT) )
                
                See(IDslApiInitializer::class)
                // REPLACE, APPEND, MODIFY the initializer of a defined property
                initializer("dtoSpecificProp", APPEND, "/* some dto specific comment */")
                initializer("prio", APPEND, "/* some dto prio comment */")
                // add/remove a property from `toString()` method
                addToStringMembers("dtoSpecificProp", "createdAt")
                removeToStringMembers("prio")
            }
        }
}
```

#### dco

```kotlin
    modelgroup {
        model("name") {
            dco {
                /* same as dto { ... } */
            }
        }
    }
```

#### tableFor

```kotlin
    modelgroup {
        model("name") {
            tableFor(MODELREFENUM.DTO) { // generate a table for the DTO of this model("name")
                /* also all a dto/dco { ... } has, but you mainly won't need those */
                initializer("name", APPEND, ".uniqueIndex()") // setting an index on the table column
                // PRIMARY KEY already had been set on property via Tag.PRIMARY
            }
        }
    }
```

##### crud

the Chassis DSL node `crud { ... }` specifies which persistence/DB operations should be generated for which model subelements.

```kotlin
    modelgroup {
        model("name") {
            tableFor(MODELREFENUM.DTO) {
                crud {
                    // generate a crud operations for the DTO of this model("name")
                    See(IDslApiOuterCrudBlock::class)
                    STANDARD FOR DTO // just a helper
                    +DTO // unary plus means "mutual with itself
                    CRUDALL FOR DTO // just a helper
                    //
                    READ.viaAllVariants FOR DTO // only select methods but all variants of them (byJoin/bySelect)
                    CREATE FOR DTO // only insert

                    prefixed("somePrefix") {
                        // create the same methods that would be generated directly in `crud { ... }` node
                        // but prefix them with the given prefix
                        // and apply the depp or shallow Restrictions to them
                        See(IDslApiPrefixedCrudScopeBlock::class)
                        (READ.viaAllVariants FOR DTO) deepRestrictions {
                            IGNORE propName "someModelObject"
                            IGNORE("someModelObject", "prefix1")
                        }
                        (CREATE FOR DTO) deepRestrictions {
                            IGNORE propName "subentitys"
                            IGNORE("subentitys", "someModelObject", "prefix2")
                        }
                    }

                }
            }
        }
    }
```

### filler

Fillers are static methods that take to submodel objects and fill the one from the other.

That means all the props that have the same name and same type will (recursively) copied over to the other.

As target as well as source you also can specify modelsubelements that are outside the current model node or even outside the current modelgroup.

The Logic for CopyBoundrys are the same as for `crud { ... }` above.

As `crud { ... }` operations use `filler { ... }` defined stuff under the hood,<br/>
if a crud needs a filler, it will synthetically create it (meaning you do NOT have to define it here redundantly)

```kotlin
    modelgroup {
        model("name") {}
            filler {
                See(IDslApiOuterFillerBlock::class)
                +DTO // DTO filled by a DTO
                DTO mutual TABLE
                DTO mutual TABLE
                DTO from TABLE
                TABLE from DTO
                DTO from (DTO of ENTITY__SUBENTITY) // TODO check if this corresponding virtual Filler is also created because of (next line)
                (DTO of ENTITY__SUBENTITY) from TABLE
                //(DTO inModelgroup PERSISTENTGROUP withModelName PERSISTENT__PERSISTENT) from DTO
                prefixed("withoutModels") {
                    See(IDslApiPrefixedCrudScopeBlock::class)
                    (DTO mutual TABLE) shallowRestrictions {
                        IGNORE propName "someModelObject∆" // TODO XXX ∆ check if prop exists!!!
                        IGNORE("dtoSpecificProp", "someObject", "aLocalDateTime")
                        IGNORE("someModelObject") // vararg
                        copyBoundry(IGNORE, "someModelObject") // vararg extended form
                    }
                    (DTO mutual TABLE) deepRestrictions {
                        IGNORE propName "subentitys"
                        IGNORE("subentitys") // vararg
                        copyBoundry(IGNORE, "subentitys") // vararg extended form
                        IGNORE model (DTO of ENTITY__SUBENTITY) onlyIf COLLECTIONTYP.COLLECTION
                    }
                    FOR((TABLE from DTO), (DTO from TABLE)) deepRestrictions {
                        IGNORE propName "subentitys"
                        IGNORE("subentitys") // vararg
                    }
                    +DTO deepRestrictions {
                    //FOR DTO {
                        copyBoundry(IGNORE, "subentitys", "someModelObject")
                    }
                    FOR(+DTO, +DTO) deepRestrictions {
                        copyBoundry(IGNORE, "subentitys", "someModelObject")
                    }
                }
            }
        }
```

there are a number of generated functions for each filler:

e.g.: `object FillerEntityDto` for model("Entity") dto classPostfix "Dto" mutual filler from/to itself:

```kotlin
fun cloneDeep(EntityDto): EntityDto
fun cloneShallowlgnoreModels(EntityDto): EntityDto
fun cloneShallowTakeSameModels(EntityDto): EntityDto
fun cloneWithNewModels(EntityDto): EntityDto
fun copyDeepinto(EntityDto, EntityDto): EntityDto
fun copyShallowAndTakeSameModelslnto(EntityDto, EntityDto): EntityDto
fun copyShallowIgnoreModelsInto(EntityDto, EntityDto): EntityDto
fun copyShallowWithNewModelslnto(EntityDto, EntityDto): EntityDto
```

for all `prefixed("some") restrictions { ... }` above's set of filler functions will be generated.

<hr/>

[back to root](..)
