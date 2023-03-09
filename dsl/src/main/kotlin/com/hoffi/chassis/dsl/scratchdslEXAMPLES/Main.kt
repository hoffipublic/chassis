package com.hoffi.chassis.dsl.scratchdslEXAMPLES

import com.hoffi.chassis.dsl.internal.DslRun
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import kotlinx.datetime.Clock

fun main() {
    // we create a DslRun for parsing Chassis-DSL
    // Clock.now() is just a part of the string identifier and not used for elapsed time measurement
    val aDslRun = DslRun("testRun: ${Clock.System.now()}")

    // we configure some global information where to generate
    // this is handsome, so we can re-use the DSL definitions to generate the same classes to somewhere else
    // or maybe with different naming conventions there
    aDslRun.configure {
        nameAndWhereto {
            baseDir = "./generated/src/main/kotlin"
            packageName = "com.hoffi.generated"
            dtoNameAndWhereto {
                classPostfix = "DtoType"
                packageName("dtoType")
            }
            dtoNameAndWhereto(COMMON) {
                classPostfix = "CommonDto"
                packageName("common")
            }
            tableNameAndWhereto {
                classPostfix = "TableType"
                packageName("tableType")
            }
//            fillerNameAndWhereto {
//                classPostfix = "Filler"
//                packageName("filler")
//            }
        }
    }
//
    println(aDslRun.runIdentifierEgEnvAndTime)
    println("running: " + aDslRun.running)

    // actually parsing the Chassis DSL here:
    aDslRun.start {
        println("running: $running")
        commonBaseModels(DslDiscriminator("someDisc"))
        simpleEntities(DslDiscriminator("someDisc"))
    }

//    // if not using the closure above you can also do it like this:
//    //simpleEntities(aRun)
//    val f = ::simpleEntities
//    f(aDslRun, DslDiscriminator("someDisc"))

    // we continue here later
    //val modelRef = DslRefString.elementRef("someDisc;modelgroup:CommonModel;model:Intfc", DslDiscriminator("someDisc"))
    //val model = aDslRun.dslCtx.getModel(modelRef)
    //println(model.kind)
    for (aModel in aDslRun.dslCtx.getAllModels()) {
        println(aModel.modelOrModelSubElementRef)
        for(dto in aModel.dslDtos) {
            println("  ${dto.dtoRef}")
        }
    }
}
