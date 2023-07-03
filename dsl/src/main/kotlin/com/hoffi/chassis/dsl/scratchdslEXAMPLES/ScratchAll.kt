package com.hoffi.chassis.dsl.scratchdslEXAMPLES

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.*
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.shared.dsl.DslRefString
import com.hoffi.chassis.shared.shared.GatherPropertiesEnum
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM.MODEL
import com.squareup.kotlinpoet.KModifier

const val SCRATCH = "CommonModel"
const val SCRATCH__INTFC = "ScratchIntfc"
const val SCRATCH__ROOT  = "ScratchBase"

context(DslCtxWrapper)
fun commonScratch() {
    dslCtx.topLevelDslFunctionName = object{}.javaClass.enclosingMethod.name

    modelgroup(SCRATCH) {
        // property() in group itself?
        nameAndWhereto("TEST1") {
            baseDir("MODELGROUP baseDir")
            packageName("commonMG")
            dtoNameAndWhereto("TESTNEST") {
                baseDir("MODELGROUP dtoNameAndWhereto baseDir")
                packageName("dtoTypeMG")
                classPrefix("CommonMGDto")
            }
        }
        model(SCRATCH__INTFC) {
            // baseDir("should fail!")
            nameAndWhereto("TEST2") {
                baseDir("MODEL baseDir")
                if (dslCtx.dslRun.runIdentifierEgEnvAndTime != "devRun") {
                    packageName(dslCtx.dslRun.wheretoImpl.nameAndWheretos[C.DEFAULT]!!.baseDirAddendum.toString())
                }
                dtoNameAndWhereto("TESTNEST") {
                    baseDir("MODEL dtoNameAndWhereto baseDir")
                }
                //fillerWhereto {  }

                val countInCommonBaseModels: Int = dslCtx.countModelsOfModelgroup(this@modelgroup.modelgroupRef)
                classPostfixAbsolute("overwrite")
                classPostfix(countInCommonBaseModels.toString())
            }
            kind = INTERFACE
            //extends {
            //    + SCRATCH__ROOT
            //}
            propertiesOf(DslRefString.modelElementRef("disc:$dslDiscriminator};modelgroup:$SCRATCH;model:$SCRATCH__INTFC", dslDiscriminator), GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
            showcase {
                dslProp = 42
                this -= "unset Data"
                + "add Data"
                - "remove Data"
                ! "not data"
                this % "something"
            }
            dto {
                nameAndWhereto("TEST3") {
                    baseDirAbsolute("DTO baseDir")
                    classPostfix("Dto")
                    packageName("dtoTypeDto")
                    classPrefix("Common")
                }
            }
            dto("other") {
                kind = CLASS
                nameAndWhereto("TEST otherDTO") {
                    baseDirAbsolute("DTO OTHER baseDir")
                }
                showcase {
                    dslProp = 66
                }
            }
            table {
                kind = OBJECT
            }
        }
        model(SCRATCH__ROOT) {
            classModifiers(KModifier.ABSTRACT)
            //extends {
            //    + SCRATCH__INTFC // throw, because don't know which concrete subelement
            //}
            extends("specialCase") {
                if (dslCtx.dslRun.runIdentifierEgEnvAndTime != "devRun") {
                    +((MODEL inModelgroup COMMON withModelName COMMON__PERSISTENT))
                }
            }
            dto {
                +KModifier.ABSTRACT
            }
        }
        // ================================================================================================================================

        allModels {
//            nameAndWhereto {
//                dtoNameAndWhereto {
//                    classPostfix = "DtoTypeAllModels"
//                    packageName("dtoTypeAllModels")
//                }
//                tableNameAndWhereto {
//                    classPostfix = "TableTypeAllModels"
//                    packageName("tableTypeAllModels")
//                }
                //fillerNameAndWhereto {
                //    classPostfix = "Filler"
                //    packageName("filler")
                //}
//            }
        }

        //filler {
        //
        //}
    }
}
