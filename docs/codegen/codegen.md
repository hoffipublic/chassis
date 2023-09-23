---
layout: page
title: Chassis codegen
subtitle: Generating Code
menubar: data_menu_chassis
toc: false
show_sidebar: false
hero_image: ../assets/Chassis.png
---
# using DSL outcome to generate Code

A GenRun operates on the GenCtx, which was populated inside a DslRun's DslCtx

```kotlin
    val examplesCodegenRun = GenRun(examplesDslRun.dslCtx.genCtx, examplesRunName)

    examplesCodegenRun.start {
        KotlinCodeGen(examplesCodegenRun).codeGen(MODELREFENUM.MODEL) // MODEL = ALL gen Subelements (DTO, DCO, TABLEFOR)
    }
```

The `GenCtx` contains the *immutable* result of a `DslRun` in a "bite-sized" representation most suitable for code-generation.

Whilst performing code-generation a `GenRun` reads information from the `GenCtx` and might store/add information in its `KotlinGenCtx`

`class GenCtxWrapper` wraps both to be neatly available to `context(GenCtxWrapper)` classes and methods:

```kotlin
class GenCtxWrapper(val genCtx: GenCtx) {
    val kotlinGenCtx = KotlinGenCtx._create()
}
```

## implemented code generations

- `abstract class KotlinGenClassNonPersristent`
  - base class for e.g. `DTO` and `DCO`, unrelated to persistent or other stuff (kind of `pojo`'s)
- `abstract class KotlinGenExposedTable`
  - persistent table mapping generation (for Jetbrains Exposed db-framework)
- `abstract class AKotlinFiller`
  - base class for fillers (AND for persistent CRUD operations of `KotlinCrudExposed`)
    - KotlinFillerDto (nonPersistent stuff)
    - KotlinFillerTable (persistent stuff) (for Jetbrains Exposed db-framework)
- `class KotlinCrudExposed`
  - CRUD (insert/select/update/delete) via Jetbrains Exposed db-framework


### TODO codegen docs

maybe also some refactorings necessary...

how to get names from the strategies

sealed classes of codegen

etc. etc...

<hr/>

[back to root](..)
