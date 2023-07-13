package com.hoffi.chassis.dsl.scratchdslEXAMPLES

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.DslShowcasePropsData
import com.hoffi.chassis.dsl.internal.DslRun
import com.hoffi.chassis.dsl.internal.IDslClass
import com.hoffi.chassis.dsl.modelgroup.DslModel
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.dsl.DslRefString
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
            baseDirAbsolute("./generated/src/main/kotlin")
            basePackage("com.hoffi.generated")
            dtoNameAndWhereto {
                classPostfix("DtoDevRun")
                packageName("dtoDevRun")
            }
            dtoNameAndWhereto(COMMON) {
                classPostfix("CommonDtoDevRun")
                packageName("commonDevRun")
            }
            tableNameAndWhereto {
                classPostfix("TableDevRun")
                packageName("tableDevRun")
            }
//            fillerNameAndWhereto {
//                classPostfix = "Filler"
//                packageName("filler")
//            }
        }
    }

    println("devRun ...")
    val devDslRun = DslRun("devRun")
    devDslRun.configure {
        nameAndWhereto {
            baseDirAbsolute("./devRunBaseDir/generated")
            packageNameAbsolute("com.hoffi.generated.devrun")
            dtoNameAndWhereto {
                classPostfix("DevRunDtoType")
                packageName("DevRundtoType")
            }
            dtoNameAndWhereto(COMMON) {
                classPostfix("DevRunCommonDto")
                packageName("devRunCommon")
            }
            tableNameAndWhereto {
                classPostfix("DevRunTableType")
                packageName("DevRunTableType")
            }
//            fillerNameAndWhereto {
//                classPostfix = "Filler"
//                packageName("filler")
//            }
        }
    }
    devDslRun.start("devDisc") {
        commonScratch()
    }
    println("devRun finished.\n")


    println(aDslRun.runIdentifierEgEnvAndTime)
    println("running: " + aDslRun.running)

    // actually parsing the Chassis DSL here:
    aDslRun.start("someDisc") {
        with(withDslDiscriminator("commonBaseModelsDisc")) {
            commonBaseModels()
        }
        this.dslDiscriminator = DslDiscriminator("simpleEntities")
        simpleEntities()
    }

//    // if not using the closure above you can also do it like this:
//    //simpleEntities(aRun)
//    val f = ::simpleEntities
//    f(aDslRun, DslDiscriminator("someDisc"))

    // we continue here later

    for (aModel in aDslRun.dslCtx.getAllModels()) {
        println(aModel.selfDslRef)
        for(dto in aModel.dslDtos.values) {
            println("  dto(\"${dto.selfDslRef.simpleName}\")")
        }
    }

//    println("================================================================================")
//    println("================================================================================")
//    println("================================================================================")
//
//    var i = 0
//    for (dslInstance in aDslRun.dslCtx.allCtxObjs) {
//        println(" ${String.format("%4d", ++i)}: ${dslInstance.key}\n    -> ${dslInstance.value}")
//    }


    println("================================================================================")
    println("================================================================================")
    println("================================================================================")

    val elementRef = DslRefString.REF("modelgroup:CommonModel;model:Intfc")
    //val model = aDslRun.dslCtx.getModel(modelRef)
    val element: IDslClass = aDslRun.dslCtx.allCtxObjs.entries.first { it.key == elementRef }.value
    @Suppress("UNUSED_VARIABLE") val breakpoint = 1
    val model = element as DslModel
    val dslShowcasePropsDataDefault = model.showcaseImpl.theShowcaseBlocks[C.DEFAULT]?.dslShowcasePropsData ?: DslShowcasePropsData()
    val derivedData = dslShowcasePropsDataDefault.dslDerivedData
    println("derivedData: '$derivedData'")

}
