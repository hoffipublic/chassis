# Project Chassis (a Kotlin Code Generator)

from a custom Kotlin DSL using <https://square.github.io/kotlinpoet>, you're able to generate (Kotlin) source code.

to see examples of the DSL see [examples/src/main/kotlin/com/hoffi/chassis/examples/basic/](https://github.com/hoffipublic/chassis/tree/master/examples/src/main/kotlin/com/hoffi/chassis/examples/basic/)

to see the generated code of these DSL's see [https://github.com/hoffipublic/generatedchassis/tree/master/examples/src/main/kotlin/com/hoffi/generated/](https://github.com/hoffipublic/generatedchassis/tree/master/examples/src/main/kotlin/com/hoffi/generated/)

## This is what you're (currently) able to generate

- **Dto** classes/interfaces/objects (Data-Transfer-Objects)
  - properties with "known" types
    - mutable and immutable (currently `Integer`,`Long`,`String`,`Boolean`,`java.util.uuid`,`kotlinx.datetime.Instant`,`kotlinx.datetime.LocalDateTime` (see `chassismodel/src/main/kotlin/com/hoffi/chassis/chassismodel/typ/TYP.kt`))
    - mutable and immutable collection types (currently `List`, `Set`, `Collection`, `Iterable`) 
    - nullable, default initializers or initializers specified in DSL 
  - properties by referencing their KClass<...>, mutable and immutable (also for collection generic-type)
    - nullable or initializers specified in DSL
  - properties by referencing other `model` types defined somewhere else in (other) chassis DSLs
  - by Tagging properties in the Dsl, they can "contribute" to generated things e.g. `toString()`, `equals()`, `hashCode()`
  - primary `constructor` (via Tag on props)
  - `companion` `create()` and `createWithUuid()`
  - `companion` `val NULL` for each defined DSL model instance
  - ***no*** generic types yet (TBD)
  - extending super classes and interfaces
    - also by referencing other DSL model instances as super type or interfaces
    - ability to specify "common" super classes/interfaces for all models in a modelgroup or all model instances in a model
  - gather (add) all properties (or/and of that ones super classes) by just saying e.g.: `propertiesOf(DTO inModelgroup "PERSISTENTGROUP" withModelName "SomeModelName")`
- ***Table*** RDBMS Objects (currently for <https://github.com/JetBrains/Exposed>)
  - by just specifying `tableFor(DTO)` in the DSL model
    - ***no*** many2many mappings yet
    - one2one to another DSL Model
    - one2many, many2one to another DSL Model
    - if the model has a property named `uuid` with TYP `java.util.Uuid`
      - generate PRIMARY KEY to Table and enable FOREIGN KEY integrity on generated jetbrain exposed Tables
      - DSL enable to set DB related property stuff, like `PK`, `nullable`, `index`, etc.
- ***Fillers***
  - generate static copy methods to fill any DSL model from any other DSL model<br/>
    (as long as the prop names and their types are compatible)
  - specifying CopyBoundrys to specify in DSL how "deep" to copy model objects (or e.g. instead do not recurse but give fresh initialized model instances to a prop)
    - Dto <--> Dto
    - Table (SQL resultRow) <--> Dto
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

<big><bold> This is a personal ***"pet project"*** and on top still <big>***very alpha and very work in progress!!!***</big></bold></big>
<div style="text-align: center;">(although it is already a complete rewrite of the initial version)</div>

## Known Limitations

- no Model classes with generic types, no List/Set/Collections which hold a generic type (sure works for `List<MyModel>`)
- no many2many RDBMS table mapping yet
- cannot have more than one FK Relation to *the same* Model (e.g. one2Many to ModelX and on2One also to ModelX)
- primary key is `val uuid : java.util.Uuid` (nothing else implemented yet ... no, no Integer PKs, no autoincrements)
- DTOs get their PK UUID on instantiation in code ... so they have their "identity" given at time of "birth" (that is object instantiation)<br/>
  (otherwise each model instance would have NO IDENTITY until they are written to the DB for the first time)
- many loose ends which are not implemented yet
- many corner cases that will explode as by now I was concentrating on the "how-to"s and "architecture" instead of feature-completeness and robustness

## TODO <!--- // TODO -->

current ToDos:

- implementing COLLECTION, ITERABLE prop types
- 
- (additional|remove)ToString members on `DslModelgroup`
- Primary Keys and FK Constraints on Database Tables (if not UuidTable)
- Cascading delete

coming up:

- write the docs on arch, principles, modules and code generation to enable people to participate in this project

- DB access: SQL UPDATE and DELETE Functions (honouring contained model instances and CopyBoundrys)
  - custom jetbrain exposed lambda parameters to "tweak stuff" on DB CRUD operations from Table's with CopyBoundries (eager/lazy loading via different generated functions)
  - further CRUD DB operations with "own function names" for each CopyBoundry
- Exposed Extensions
  - upsert ?
  - insert/delete if not exists [stackoverflow: how-can-i-do-insert-if-not-exists-in-mysql](https://stackoverflow.com/questions/1361340/how-can-i-do-insert-if-not-exists-in-mysql)

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

<!--page layout css styles for markup headers numbering -->
<style> /* automatic heading numbering */ h1 { counter-reset: h2counter; font-size: 24pt; } h2 { counter-reset: h3counter; font-size: 22pt; margin-top: 2em; } h3 { counter-reset: h4counter; font-size: 16pt; } h4 { counter-reset: h5counter; font-size: 14pt; } h5 { counter-reset: h6counter; } h6 { } h2:before { counter-increment: h2counter; content: counter(h2counter) ".  "; } h3:before { counter-increment: h3counter; content: counter(h2counter) "." counter(h3counter) ".  "; } h4:before { counter-increment: h4counter; content: counter(h2counter) "." counter(h3counter) "." counter(h4counter) ".  "; } h5:before { counter-increment: h5counter; content: counter(h2counter) "." counter(h3counter) "." counter(h4counter) "." counter(h5counter) ".  "; } h6:before { counter-increment: h6counter; content: counter(h2counter) "." counter(h3counter) "." counter(h4counter) "." counter(h5counter) "." counter(h6counter) ".  "; } </style>

## Blog posts to write

- DSL referencing elements
- DTO code generation (predefined types, predefined inheritance, predefined "helper" and companion methods for each model instance)
- CRUD DB method generation (by join, by select)
  - copy Boundrys in genrated code instead of lazy loading with a ***bloated and heavy OR Mapping Framework***
- 

## debug watch expressions

- dslCtx.allCtxObjs.filter { it.key.simpleName == "Base" }.map { it.value }
- dslCtx.genCtx.genModels.filter { it.key.simpleName == "Base" }.map { it.value }
- DslRefString.REF("disc:commonBaseModelsDisc;modelgroup:Simple;model:Entity;dto")
- 

# implementing you're own (non table) proper Models (subelements of model { })

1) create by e.g. copying a simple existing DslModel Subelement (e.g. `class DslDco`)
2) create DslRef subelement for it in `class DslRef` (and add it to the needed places for reffing in `DslRef.kt`)
3) create a GenModel for it in `sealed class GenModel`
4) create the `IDslApiXxx` and `IDslImplXxx` interfaces for your new proper model subelement (e.g. study `DslShowcase.kt`)
5) add a `fun xxxNameAndWhereto(...)` to `interface IDslApiNameAndWheretoOnSubElements`</br>
   (if you want to be able to specify where all you xxx generated proper models of a modelgroup should go (package, path, prefix, postfix, ...))
6) add your Xxx to `enum class MODELREFENUM`
7) add your xxx to `interface IDslApiModel`
8) add implementation for it to `class DslModel`
   and also add a `val dslXxxs: MutableMap<String, DslXxx> = mutableMapOf()` 
9) at this point a lot of `when (...) {` should not compile anymore ... add meaningfull stuff to all of them for your Xxx

a minimal `interface IDslApiXxx` might look like:

```kotlin
interface IDslApiDco : IDslApi
    :   IDslApiModelAndModelSubelementsCommon,
        IDslApiSubelementsOnlyCommon
```
For table's and CRUDS it is more difficult as these are *very heavily* dependant on you persistence Framework,</br>
but your xxx should work just fine with the existing JetBrains "exposed" table's.
