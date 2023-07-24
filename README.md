# Project Chassis (a Kotlin Code Generator)

from a custom Kotlin DSL using <https://square.github.io/kotlinpoet>, generate:

- Dto (Data-Transfer-Objects)
    - including static initializers
    - toString(), equals() and hashMap()
    - Companion instantiators and NULL object
    - no generic types yet
- Table Objects (currently for <https://github.com/JetBrains/Exposed>)
    - (no many2many mappings yet)
- Fillers
    - Dto <--> Dto
    - Table (SQL resultRow) <--> Dto
- RDBMS access via fillers (determining how deep to CRUD ( also from/to DB) via DSL "boundaries")

<br/>

<big><bold> This is personal "pet project" and on top still very alpha and work in progress!!!<br/>
(although it is already a complete rewrite of the initial version) </bold></big>

## TODO <!--- // TODO -->

current:

- (additional|remove)ToString members on `DslModelgroup`
- Primary Keys and FK Constraints on Database Tables (if not UuidTable)
- Cascading delete

coming up:

- DB access: SQL Functions incl. FKs
  - select DTOs from Table's with copy Boundries (eager/lazy loading via different generated functions)
  - and adhering to CopyBoundrys
  - further CRUD DB operations with "own function names" for each CopyBoundry
- Exposed Extensions
  - upsert ?
  - insert/delete if not exists [stackoverflow: how-can-i-do-insert-if-not-exists-in-mysql](https://stackoverflow.com/questions/1361340/how-can-i-do-insert-if-not-exists-in-mysql)


- static companion functions (NULL)


- **many to many relations**
- Properties with generic types
- collections with generic generic types
- properties with generic types that are models
- collections with generic types that are models
- DB mappings for above properties


## Future TODOs

- primary key of more than one column
- more generics on models and functions

<!--page layout css styles for markup headers numbering -->
<style type="text/css"> /* automatic heading numbering */ h1 { counter-reset: h2counter; font-size: 24pt; } h2 { counter-reset: h3counter; font-size: 22pt; margin-top: 2em; } h3 { counter-reset: h4counter; font-size: 16pt; } h4 { counter-reset: h5counter; font-size: 14pt; } h5 { counter-reset: h6counter; } h6 { } h2:before { counter-increment: h2counter; content: counter(h2counter) ".  "; } h3:before { counter-increment: h3counter; content: counter(h2counter) "." counter(h3counter) ".  "; } h4:before { counter-increment: h4counter; content: counter(h2counter) "." counter(h3counter) "." counter(h4counter) ".  "; } h5:before { counter-increment: h5counter; content: counter(h2counter) "." counter(h3counter) "." counter(h4counter) "." counter(h5counter) ".  "; } h6:before { counter-increment: h6counter; content: counter(h2counter) "." counter(h3counter) "." counter(h4counter) "." counter(h5counter) "." counter(h6counter) ".  "; } </style>

## debug watch expressions

- dslCtx.allCtxObjs.filter { it.key.simpleName == "Base" }.map { it.value }
- dslCtx.genCtx.genModels.filter { it.key.simpleName == "Base" }.map { it.value }
- DslRefString.REF("disc:commonBaseModelsDisc;modelgroup:Simple;model:Entity;dto")
- 
