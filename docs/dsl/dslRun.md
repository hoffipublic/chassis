---
layout: page
title: Chassis DSL and codegen RUN
subtitle: actually generating something from your Chassis DSL
menubar: data_menu_chassis
toc: false
show_sidebar: false
hero_image: ../assets/Chassis.png
---
# Actually generating something from your Chassis DSL

This is how you "execute" code generation from your Chassis DSL function(s):

```kotlin
object MainExamples {
    @JvmStatic
    fun main(args: Array<String>) {
        val examplesRunName = "ExamplesRun"
        val examplesDslRun = DslRun(examplesRunName)

        examplesDslRun.configure {
            nameAndWhereto {
                baseDirAbsolute("../generated/examples/src/main/kotlin")
                basePackageAbsolute("com.hoffi.generated.examples")
                //dtoNameAndWhereto { ... }
            }
        }

        // actually parsing the Chassis DSL here:
        examplesDslRun.start("someDisc") {
            baseModelsPersistent() // contains Chassis DSL top-level func
            entitiesFunc()         // contains Chassis DSL top-level func
            dcosFunc()             // contains Chassis DSL top-level func
        }
        val examplesCodegenRun = GenRun(examplesDslRun.dslCtx.genCtx, examplesRunName)

        examplesCodegenRun.start {
            println("examples/${examplesCodegenRun.runIdentifier}".boxed(postfix = "\n"))
            KotlinCodeGen(examplesCodegenRun).codeGen(MODELREFENUM.MODEL) // MODEL = ALL gen Subelements (DTO, DCO, TABLEFOR)
        }
    }
}
```

<hr/>

[back to root](..)
