package com.hoffi.chassis.examples

import com.hoffi.chassis.chassismodel.helpers.boxed
import com.hoffi.chassis.codegen.kotlin.KotlinCodeGen
import com.hoffi.chassis.dsl.internal.DslRun
import com.hoffi.chassis.examples.basic.baseModelsPersistent
import com.hoffi.chassis.examples.basic.entities
import com.hoffi.chassis.shared.codegen.GenCtxWrapper
import com.hoffi.chassis.shared.codegen.GenRun
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.dsl.DslRef

fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    val examplesRunName = "ExamplesRun"
    val examplesDslRun = DslRun(examplesRunName)

    examplesDslRun.configure {
        nameAndWhereto {
            baseDirAbsolute("./generated/examples/src/main/kotlin")
            basePackageAbsolute("com.hoffi.generated.examples")
            dtoNameAndWhereto {
                classPostfix("RunDto")
                packageName("dto")
            }
            dtoNameAndWhereto("SPECIAL") {
                classPostfix("ExamplesDtoDevRun")
                packageName("examplesDevRun")
            }
            tableNameAndWhereto {
                classPostfix("RunTable")
                packageName("table")
            }
//            fillerNameAndWhereto {
//                classPostfix = "Filler"
//                packageName("filler")
//            }
        }
    }

// actually parsing the Chassis DSL here:
    examplesDslRun.start("someDisc") {
        with(withDslDiscriminator("commonBasePersistentDisc")) {
            baseModelsPersistent()
        }
        this.dslDiscriminator = DslDiscriminator("simpleEntities")
        entities() // TODO still the project :dsl which will be executed
    }

    val examplesCodegenRun = GenRun(examplesDslRun.dslCtx.genCtx, examplesRunName)

    with(GenCtxWrapper(examplesCodegenRun.genCtx)) {
        examplesCodegenRun.start {
            println("examples/${examplesCodegenRun.runIdentifier}".boxed(postfix = "\n"))
            KotlinCodeGen().codeGen(DslRef.model.MODELELEMENT.DTO)
        }
    }
}
