package com.hoffi.chassis.examples

import com.hoffi.chassis.chassismodel.helpers.boxed
import com.hoffi.chassis.codegen.kotlin.KotlinCodeGen
import com.hoffi.chassis.dsl.internal.DslRun
import com.hoffi.chassis.examples.basic.baseModelsPersistent
import com.hoffi.chassis.examples.basic.dcosFunc
import com.hoffi.chassis.examples.basic.entitiesFunc
import com.hoffi.chassis.shared.codegen.GenRun
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM

object MainExamples {
    @JvmStatic
    fun main(args: Array<String>) {
        val examplesRunName = "ExamplesRun"
        val examplesDslRun = DslRun(examplesRunName)

        examplesDslRun.configure {
            nameAndWhereto {
                baseDirAbsolute("../generated/examples/src/main/kotlin")
                basePackageAbsolute("com.hoffi.generated.examples")
                //dtoNameAndWhereto {
                //    classPostfix("RunDto")
                //    packageName("dto")
                //}
                //dtoNameAndWhereto("SPECIAL") {
                //    classPostfix("ExamplesDtoDevRun")
                //    packageName("examplesDevRun")
                //}
                //tableNameAndWhereto {
                //    classPostfix("RunTable")
                //    packageName("table")
                //}
                ////            fillerNameAndWhereto {
                ////                classPostfix = "Filler"
                ////                packageName("filler")
                ////            }
            }
        }

        // actually parsing the Chassis DSL here:
        examplesDslRun.start("someDisc") {
            with(withDslDiscriminator("commonBasePersistentDisc")) {
                baseModelsPersistent()
            }
            this.dslDiscriminator = DslDiscriminator("simpleEntities")
            entitiesFunc() // TODO still the project :dsl which will be executed
            dcosFunc()
        }

        // GenRun operates on the GenCtx, which was populated inside a DslRun's DslCtx
        val examplesCodegenRun = GenRun(examplesDslRun.dslCtx.genCtx, examplesRunName)

        examplesCodegenRun.start {
            println("examples/${examplesCodegenRun.runIdentifier}".boxed(postfix = "\n"))
            KotlinCodeGen(examplesCodegenRun).codeGen(MODELREFENUM.MODEL) // MODEL = all gen Subelements
        }
    }
}
