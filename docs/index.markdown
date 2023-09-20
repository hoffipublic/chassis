---
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults
layout: page
title: Chassis Home
subtitle: The Kotlin Code-Generator
description: Chassis Kotlin Code-Generator for DTO classes, objects, interfaces, api and RDBMS insert update read delete CRUD
hero_image: assets/Chassis.png
menubar: data_menu_chassis
show_sidebar: false
---

# Project Chassis Kotlin Code-Generator

|-------------------------------------------|----------------------------------------------------------------------------------------------------:|-------------------------------------------------------------------------------------------:|
| latest version: {{ site.chassisversion }} | [![Kotlin](https://img.shields.io/badge/kotlin-1.9.10-blue.svg?logo=kotlin)](http://kotlinlang.org) | ![GitHub Repo stars](https://img.shields.io/github/stars/hoffipublic/chassis?style=social) |

Over and over and over again...

***TL;DR***

- have a look at the DSL [chassis DSL examples](https://github.com/hoffipublic/chassis/tree/master/examples/src/main/kotlin/com/hoffi/chassis/examples/basic){:target="_blank"}
- find generated (non persistence related) DTO kotlin code in [github generatedchassis DTO](https://github.com/hoffipublic/generatedchassis/tree/master/examples/src/main/kotlin/com/hoffi/generated/examples){:target="_blank"}
- find generated (persistence related) exposed tables and CRUD operations in [github generated TABLE](https://github.com/hoffipublic/generatedchassis/tree/master/examples/src/main/kotlin/com/hoffi/generated/examples/table){:target="_blank"}

current example DSL (4 DTOs, 1 DCO with Fillers and DB CRUDs plus 4 abstract DTOs Base-Classes):

```
|           |  lines |  #files |
|:---------:|-------:|--------:|
|    DSL    |    319 |       3 |
| generated |   2190 |      43 |
|     %     | ~700 % | ~1400 % |
```

<hr/>
<hr/>
***The Story***

Whilst my extensive career in SW consulting and development I realized ... 80% of the work is marshalling and unmarshalling data structures and copying properties between hierarchies of related but independent (sub)datastructures consistently.

From the Internet (json, gRPC, XML, you name it) to your deserialized object, split up to x business objects, recursively copied to your persistent objects to be written to a database,
and then back again towards the internet. Every little adding of a model or even a property to some object causes a gazillion of files to be changed consistently.

Consistently also means: in your backend, in your middle-services, in your frontend(s) in your API specs ... (this also means in several *independent* repositories ... (btw: openAPI sucks)

All so often I saw those changes being the cause for broken tests, CICD pipelines (or even production takeouts).

Also - over time - any project evolves several variants for each (un)marshalling and CRUD operation.
In one case you may want to load an object "flat", in another cases with "a little bit" of contained objects,
sometimes all the object tree (loading half of the database redundantly) and sometimes some (sub)Objects need some nasty inner transformations
(because business (sub)domains often differ in just "nasty tiny details").

***Code Generation***

While code generation with text templates ever was a pain in the arse (and ever will be), since the last century frameworks evolved that are really "usable" for the purpose of code-generation.

[KotlinPoet](https://square.github.io/kotlinpoet/){:target="_blank"} is one of these really nice frameworks.

And as Kotlin also has the nice feature of [trailing lambda parameters](https://kotlinlang.org/docs/lambdas.html#passing-trailing-lambdas){:target="_blank"} (the function-implementation
can be placed outside of the function call parentheses), we can use **pure kotlin code as DSL** (no json, no yaml, no toml, just code).<br/>
(good thing: security is no issue in code generation ... as we're in our code-project anyways)<br/>
(BTW: Chassis DSL can reference any DSL Subelement from any other DSL Subelement,<br/>
&nbsp;no more xml/yml/json referencing hell...<br/>
&nbsp;and as the DSL is pure Kotlin code, we get syntax highlighting, code formatting and coloring<br/>
&nbsp;*for free* in the IDE of your choice)

***Chassis CodeGenerator to the rescue***

So the DSL "definition" of an entity dto and an entity dco that share some properties and have the same super-classes can easily look like:

```kotlin
    modelgroup(ENTITYGROUP) {
        constructorVisibility = PROTECTED
        model(ENTITY__BASEINTERFACE) {
            kind = INTERFACE
            dto {}
        }
        model(ENTITY__ENTITY) {
            extends {
                + (MODEL inModelgroup PERSISTENTGROUP withModelName COMMON__PERSISTENT_OPTIMISTIC)
                - ENTITY__BASEINTERFACE
            }
            property("name", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("value", TYP.STRING, mutable, length = 4096, Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("aLocalDateTime", TYP.LOCALDATETIME, mutable)
            property("someObject", Dummy::class, mutable, Initializer.of("%T.%L", Dummy::class, "NULL"), length = C.DEFAULT_INT, Tag.TRANSIENT)
            property("someModelObject", DTO of ENTITY__SOMEMODEL, mutable)
            property("subentitys", "modelgroup:$ENTITYGROUP|model:$ENTITY__SUBENTITY", DTO, COLLECTIONTYP.SET, Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER, Tag.NULLABLE)
            property("listOfStrings", TYP.STRING, COLLECTIONTYP.LIST, Tag.COLLECTION_IMMUTABLE, Tag.CONSTRUCTOR)

            dto {
                property("dtoSpecificProp", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER)
                initializer("prio", APPEND, "/* some dto prio comment */")
                addToStringMembers("dtoSpecificProp", "createdAt")
            }
            tableFor(DTO) {
                propertiesOf(DTO, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
                initializer("name", APPEND, ".uniqueIndex()")
                initializer("prio", APPEND, "/* some table prio comment */")

                crud {
                    STANDARD FOR DTO
                }
            }
        }
        filler {
            +DTO // DTO filled by a DTO
            DTO mutual TABLE
            DTO from (DTO of ENTITY__SUBENTITY)
        }
    }
```

On generating Code from above's Chassis DSL

- you get static "Fillers" to recursively copy any object to any(!) other object (as long as the prop names and their types are compatible)
- database CRUD (insert, select, update, delete) static methods to operate towards and from the RDBMS (currently only via [JetBrains exposed](https://github.com/JetBrains/Exposed){:target="_blank"})
  - read all contained objects in one SQL (via JOIN)
  - read via separate selects (for contained models)
- via DSL specification of CopyBoundrys you can generate variants for each of these operations
  - omiting subtrees or types or prop-names, or replacing them or transforming them ...

Your **business code** can (recursively &#x1F609;) concentrate on what there is to solve for business:

```kotlin
    transaction {
        CrudSimpleEntityTableCREATE.batchInsertDb(simpleEntityDtoList)
    }
    ...
    var selectedEntityDtos = transaction {
        CrudSimpleEntityTableREAD.readByJoin {
            (SimpleEntityTable.prio lessEq 3)
        }
    }
```

<hr/>

## This is what you're (currently) able to generate (all kotlin)

- **Dto** classes/interfaces/objects (Data-Transfer-Objects)
    - properties with "known" types
        - mutable and immutable (currently `Integer`, `Long`, `String`, `Boolean`, `java.util.uuid`, `kotlinx.datetime.Instant`, `kotlinx.datetime.LocalDateTime` (see [TYP.kt](https://github.com/hoffipublic/chassis/blob/db67b34c9ae78f8b975111bd378708b2c7b9c1e9/chassismodel/src/main/kotlin/com/hoffi/chassis/chassismodel/typ/TYP.kt#L40))
        - mutable and immutable collection types (currently `List`, `Set`, `Collection`, `Iterable`)
        - nullable, default initializers or initializers specified in DSL
    - properties by referencing their KClass<...>, mutable and immutable (also for collection generic-type)
        - nullable or initializers specified in DSL
    - properties by referencing other `model` types defined somewhere else in (other) chassis DSLs
    - by Tagging properties in the Dsl, they can "contribute" to generated things e.g.
      - `toString()`
      - `equals()`
      - `hashCode()`
    - primary (public/protected) `constructor` (via Tag on props)
    - `companion object`
      - `create()`
      - `createWithUuid()`
      - `val NULL = ...` (therefore, no need to have nullable model variables anymore!)
    - ***no*** generic types yet (TBD)
    - extending super classes and interfaces
        - also by referencing other DSL model instances as super type or interfaces
        - ability to specify "common" super classes/interfaces for all models in a modelgroup or all model instances in a model
    - gather (add) all properties (or/and of that ones super classes) by just saying e.g.:
      - `propertiesOf(DTO inModelgroup "PERSISTENTGROUP" withModelName "SomeModelName")`
      - use this to define an abstract model (code generation might be suppressed for that one)
        and reference its props from models in your DSL which should also have exactly THAT set of props (DRY).
- ***Fillers***
    - generate static copy methods to fill any DSL model from any other DSL model<br/>
      (as long as the prop names and their types are compatible)
    - specifying CopyBoundrys to specify in DSL how "deep" to copy model objects (or e.g. instead do not recurse but give fresh initialized model instances to a prop)
        - Dto <--> Dto
        - Dto <--> Dco
        - Table (SQL resultRow) <--> Dto
- ***Table*** RDBMS Objects (currently for <https://github.com/JetBrains/Exposed>)
    - by just specifying `tableFor(DTO)` in the DSL model
        - ***no*** many2many mappings yet
        - one2one to another DSL Model
        - one2many, many2one to another DSL Model
        - if the model has a property named `uuid` with TYP `java.util.Uuid`
            - generate PRIMARY KEY to Table and enable FOREIGN KEY integrity on generated jetbrain exposed Tables
            - DSL enable to set DB related property stuff, like `PK`, `nullable`, `index`, etc.
- ***CRUD DB access*** methods (CREATE/insert, READ/select, UPDATE/update, DELETE/delete)
    - either via DB `joins` of all (recursive) contained DSL model objects<br/>(inside the DSL model object you want to "CRUD")<br/>model instance has to have a `PK named 'uuid' for join's to work`
    - or via distinct `select`s to the db for each (recursive) contained DSL model object
    - specifying CopyBoundrys to specify in DSL how deep to "CRUD" from DB
        - this will generate separate `filler` and `CRUD` Methods for each "set/prefix/businessCase" that needs its own CopyBoundrys

**using:**

- ability to use/references other defined models and modelinstances to construct your code class
    - reference another DSL model/instance as super-class, super interface, prop type, prop collection type
    - specify abstract base classes and use their properties and super-class/interfaces to "include" directly (without extending class inheritance)<br/>
      (`propertiesOf(DTO inModelgroup "PERSISTENTGROUP" withModelName "SomeModelName")`)
- flexible ***naming strategies*** for
    - naming props, classes/interfaces/objects, methods, table_names, column_names,<br/>(CamelCase, snake_case, kebab-case, capitalized, decapitalized, prefixed, postfixed, replace pre/postfix, add to current pre/postfix, etc.)
- flexible destination spec in DSL
    - absolute package, addendum pre/postfix to package, same for classe names and same for destination path of generated classes
        - either in DSL run or
        - modelgroup (for all models and model instances (dto/table/...))
        - model (for all model instances of that model (dto/table))
        - model instance
        - implement your own strategy like `SPECIAL_WINS_ON_ABSOLUTE_STUFF_BUT_CONCAT_ADDENDUMS`
- take preferred defaults without having to specify too much in the DSL
    - just switch the strategies and get "your corporate design governance" compliant code
      <br/>

<big><bold> This is a personal ***pet project***<br/>and on top still <big>***very alpha and very work in progress!!!***</big></bold></big>
<div style="text-align: center;">*(although it is already a complete rewrite of the initial version)</div>

## Known Limitations

- no Model classes with generic types, no List/Set/Collections which hold a generic type (sure works for `List<MyModel>`)
- no many2many RDBMS table mapping yet
- cannot have more than one FK Relation to *the same* Model (e.g. one2Many to ModelX and on2One also to ModelX)
- primary key is `val uuid : java.util.Uuid` (nothing else implemented yet ... no, no Integer PKs, no autoincrements)
- DTOs get their PK UUID on instantiation in code ... so they have their "identity" given at time of "birth" (that is object instantiation)<br/>
  (otherwise each model instance would have NO IDENTITY until they are written to the DB for the first time)
- many loose ends which are not implemented yet
- many corner cases that will explode as by now I was concentrating on the "how-to"s and "architecture" instead of feature-completeness and robustness

## TODO

current ToDos:

- implementing COLLECTION, ITERABLE prop types
- &nbsp;
- (additional/remove)ToString members on `DslModelgroup`
- Primary Keys and FK Constraints on Database Tables (if not UuidTable)
- Cascading delete

coming up:

- write the docs on arch, principles, modules and code generation to enable people to participate in this project

- DB access: SQL UPDATE and DELETE Functions (honouring contained model instances and CopyBoundrys)
    - custom jetbrain exposed lambda parameters to "tweak stuff" on DB CRUD operations from Table's with CopyBoundries (eager/lazy loading via different generated functions)
    - further CRUD DB operations with "own function names" for each CopyBoundry
- Exposed Extensions
    - upsert ?
    - insert/delete if not exists [stackoverflow: how-can-i-do-insert-if-not-exists-in-mysql](https://stackoverflow.com/questions/1361340/how-can-i-do-insert-if-not-exists-in-mysql){:target="_blank"}

- **many to many relations**
- Properties with generic types
- collections with generic generic types
- properties with generic types that are models
- collections with generic types that are models
- DB mappings for above properties


## Future TODOs

- primary key of more than one column
- more generics on models and functions
- generating other things than kotlin code (e.g. openAPI spec)
- generating API code (REST, gRPC, ProtoBuffers, ...)
- generating PlantUML
- ...
