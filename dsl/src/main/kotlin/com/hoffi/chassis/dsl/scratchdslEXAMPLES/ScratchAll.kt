package com.hoffi.chassis.dsl.scratchdslEXAMPLES

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.*
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.shared.dsl.DslRefString
import com.hoffi.chassis.shared.shared.GatherPropertiesEnum
import com.squareup.kotlinpoet.KModifier

const val SCRATCH = "CommonModel"
const val SCRATCH__INTFC = "ScratchIntfc"
const val SCRATCH__ROOT  = "ScratchBase"

context(DslCtxWrapper)
fun commonScratch() {
    modelgroup(SCRATCH) {
        // property() in group itself?
        nameAndWhereto("TEST1") {
            baseDir = "MODELGROUP baseDir"
            packageName("common")
            nameAndWhereto {
            }
            dtoNameAndWhereto("TESTNEST") {
                baseDir = "MODELGROUP dtoNameAndWhereto baseDir"
                packageName("dtoType")
                classPrefix("Common")
            }
        }
        model(SCRATCH__INTFC) {
//            baseDir = "should fail!"
            nameAndWhereto("TEST2") {
                nameAndWhereto {
                }
                baseDir = "MODEL baseDir"
                if (dslCtx.dslRun.runIdentifierEgEnvAndTime != "devRun") {
                    packageName = dslCtx.dslRun.wheretoImpl.nameAndWheretos[C.DEFAULT]!!.baseDir
                }
                dtoNameAndWhereto("TESTNEST") {
                    baseDir = "MODEL dtoNameAndWhereto baseDir"
                }
                //fillerWhereto {  }

                val countInCommonBaseModels: Int = dslCtx.countModelsOfModelgroup(this@modelgroup.modelgroupRef)
                classPostfix = "overwrite"
                classPostfix(countInCommonBaseModels.toString())
            }
            kind = INTERFACE
//            extends {
//                + SCRATCH__ROOT
//            }
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
                    nameAndWhereto {
                        val x = this
                    }
                    baseDir = "DTO baseDir"
                    classPostfix = "DtoType"
                    packageName("dtoType")
                    classPrefix = "Common"
                    nameAndWhereto("TESTNEST") {
                        baseDir = "DTO NOTALLOWED NESTED baseDir"
                    }
                }
            }
            dto("other") {
                kind = CLASS
                nameAndWhereto("TEST otherDTO") {
                    baseDir = "DTO OTHER baseDir"
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
            dto {
                +KModifier.ABSTRACT
            }
        }
        // ================================================================================================================================

        allModels {
            nameAndWhereto {
                dtoNameAndWhereto {
                    classPostfix = "DtoType"
                    packageName("dtoType")
                }
                tableNameAndWhereto {
                    classPostfix = "TableType"
                    packageName("tableType")
                }
                //fillerNameAndWhereto {
                //    classPostfix = "Filler"
                //    packageName("filler")
                //}
            }
        }

        //filler {
        //
        //}
    }
}
