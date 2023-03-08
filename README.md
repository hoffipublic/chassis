# chassis root README

## Project Layout (work in progress)

- `root` project chassis
  - console client to conquer all subprojects and their actions
  - gather artifacts??
  - call sub-artifacts (executing them)
- `dsl` subproject
  - chassis dsl language definition for generating stuff
  - main
  - some variant/plugin
  - other variant/plugin
- `codegen` sub-project
  - generation of code from result data of parsed dsl specifications (subproject `dsl`)
  - main
  - some variantA
  - some variantB
- `shared` data, code, etc. that is shared between `dsl` and `codegen`
  - but ***NOT*** used by `univers` andy `generated` or `targetProjects`
- `universe` subproject
  - static things that are needed to make generated code work
- plugins???
- `generated` subproject
  - subproject(s) where code is generated to, to see if everything works
  - kotlin (don't need this as this is all about kotlin)
- targetProjects
  - projects that actually do something and depend on generated stuff

## Description and Explanations

### dsl

The subproject `dsl` defines a **D**omain **S**pecific **L**anguage to let the user specify anything he/she needs to be able to generate something from it.

the DSL allows to:
- specify ModelObjects with properties/fields for each, with
  - predefined basic types, int, string, dates, uuid, ...
  - by referencing other model(group) types as type
  - arbitrary (existing) types
  - certain collection types
  - generic types (not yet implemented)
- filler methods
  - copy properties (with type conversion) recursively between different models (as long as the property/field's names are equal (e.g. DTO <--> Table) or (ApiDTO <--> BusinessModel <--> DB)
- table
  - gather all superclass properties of a model
  - alter properties/field's type for being mappable to a DB
- generate [JetBrains Exposed](https://github.com/JetBrains/Exposed) DB "mapping" functions
  - does NOT use ORM whatsoever, but generates recursive methods that using fillers to copy properties/fields of the same name (with type conversion) between sumodels (e.g. DTO and TABLE models) 

At the top level there are top-level blocks to be able to define:

- `<modelgroup> containing <models> with <modelsubtypes>`:
  - anything to specify classes with properties/fields
  - as you *can* also specify methods/functions for models, this is *not* the core use-case for models
  - currently the following <modelsubtypes> are implemented
  - `dto` **D**ata **T**ransfer **O**bjects (e.g. for serialization and de-serialization)
  - `table` mapping of property fields to database tables
    - also generic DB access methods are generated currently for [JetBrains Exposed](https://github.com/JetBrains/Exposed))
    - if models reference other (collections) of models these are also "load/save"able
    - ... but this is still matter of desing and TODO
  - `filler`
    - this is a special modelsubtype for copying data recursively between different models
      - e.g. DTO to DTO, or DTO (recursively) to its table

The subproject `dsl` ***DOES NOT*** generate code.<br/>
but instead gather and transform all dsl information data into `shared` data structures ...

... that will be process with ...

### codegen

Subproject `codegen` takes the intermediate information generated from subproject `dsl` and (mainly) uses [KotlinPoet](https://square.github.io/kotlinpoet/) to generate code from it.

### shared

code and classes shared between `dsl` and `codegen`

- the extracted "bite-sized" information from the `dsl`
  - `codegen` should ***NOT*** have to do ANY logic (like determine types, or (field/variable/type) names, nor their (sub)package or name (pre-/postfix))
  - TODO also the [JetBrains Exposed](https://github.com/JetBrains/Exposed) type mapping also has been done (??? has it???)
- sealed classes to describe variants
- information classes for information interchange between `dsl` and `codegen`
- helpers and wrappers

## Further Ideas

### Transformers

- from one name to another
  - and vice versa?
- data format transformation
- data type transformation

### Strategies

#### copy strategies

- how deep do you want to copy?
- what to do if field missing?
- what to do if validation fails?

#### filler Strategies

- validate fromValue, validate toValue<br/>
  define what to do if certain validation errors, null, etc.

#### table Strategies

-tbd-

## tbd

<!-- Content End -->

<!--inline css styles for markup headers numbering -->
<!-- markdownlint-disable-next-line MD033 -->
<style type="text/css"> h1 { counter-reset: h2counter; font-size: 24pt; } h2 { counter-reset: h3counter; font-size: 22pt; margin-top: 2em; } h3 { counter-reset: h4counter; font-size: 16pt; margin-top: 1em; } h4 { counter-reset: h5counter; font-size: 14pt; } h5 { counter-reset: h6counter; } h6 { } h2:before { counter-increment: h2counter; content: counter(h2counter) ".\00a0\00a0\00a0"; } h3:before { counter-increment: h3counter; content: counter(h2counter) "." counter(h3counter) ".\00a0\00a0"; } h4:before { counter-increment: h4counter; content: counter(h2counter) "." counter(h3counter) "." counter(h4counter) ". "; } h5:before { counter-increment: h5counter; content: counter(h2counter) "." counter(h3counter) "." counter(h4counter) "." counter(h5counter) ". "; } h6:before { counter-increment: h6counter; content: counter(h2counter) "." counter(h3counter) "." counter(h4counter) "." counter(h5counter) "." counter(h6counter) ". "; } </style>
