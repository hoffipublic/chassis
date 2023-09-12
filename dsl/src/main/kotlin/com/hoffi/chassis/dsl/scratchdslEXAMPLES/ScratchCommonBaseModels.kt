package com.hoffi.chassis.dsl.scratchdslEXAMPLES

import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.chassismodel.typ.mutable
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.CLASS
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.INTERFACE
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM.DTO
import com.squareup.kotlinpoet.KModifier

const val COMMON = "CommonModel"
const val COMMON__INTFC                 = "Intfc"
const val COMMON__ROOT                  = "Base"
const val COMMON__PERSISTENT            = "Persistent"
const val COMMON__TRANSIENT_STATE       = "TransientState"
const val COMMON__PERSISTENT_OPTIMISTIC = "PersistentOptimistic"

context(DslCtxWrapper)
fun commonBaseModels() {
    dslCtx.topLevelDslFunctionName = object{}.javaClass.enclosingMethod.name
    modelgroup(COMMON) {
        // property() in group itself?
        nameAndWhereto("TEST") {
            packageName("base")
            dtoNameAndWhereto {
                classPostfix("Dto")
                packageName("dto")
            }
            tableNameAndWhereto {
                classPostfix("Table")
                packageName("table")
            }
        }
        model(COMMON__INTFC) {
            nameAndWhereto("TEST") {
                packageName("base")
            }
            kind = INTERFACE
            dto {
            }
            dto("other") {
                kind = CLASS
            }
            tableFor(DTO) {
            }
        }
        model(COMMON__ROOT) {
            classModifiers(KModifier.ABSTRACT)
            dto {
                +KModifier.ABSTRACT
            }
        }
        model(COMMON__PERSISTENT) {
            classModifiers(KModifier.ABSTRACT)
            property("uuid", TYP.UUID, mutable, Tag.PRIMARY)
            property("createdAt", TYP.LOCALDATETIME, mutable)
            property("updatedAt", TYP.LOCALDATETIME, mutable)
            property("createUser", TYP.STRING, mutable)
            property("updateUser", TYP.STRING, mutable)
//            initBusinessValues {
//                "createdAt"  with Initializer.of("%L", DEFAULT_INITCODE_LOCALDATETIME)
//                "updatedAt"  with Initializer.of("%L", DEFAULT_INITCODE_LOCALDATETIME)
//                "createUser" with Initializer.of("%S", DEFAULT_USER)
//                "updateUser" with Initializer.of("%S", DEFAULT_USER)
//            }
            dto {
                extends { +COMMON__ROOT } // TODO this@model
                classMods { }
                nameAndWhereto {
                    classPostfix("SomeXXX")
                }
            }
        }
        model(COMMON__TRANSIENT_STATE) {
            classModifiers(KModifier.ABSTRACT)
            property("created", TYP.BOOL, mutable, Tag.TRANSIENT)
            property("modified", TYP.BOOL, mutable, Tag.TRANSIENT)
            property("deleted", TYP.BOOL, mutable, Tag.TRANSIENT)

            dto {
                extends { +COMMON__PERSISTENT }
            }
        }
        model(COMMON__PERSISTENT_OPTIMISTIC) {
            nameAndWhereto {
                packageName("base")
            }
            classModifiers(KModifier.ABSTRACT)
            property("optimisticLockId", TYP.LONG, mutable)
//            initBusinessValues {
//                "optimisticLockId" with Initializer.of("%L", DEFAULT_OPTIMISTIC_LOCK_ID)
//            }
            dto {
                extends { +COMMON__TRANSIENT_STATE }
            }
        }
        // ================================================================================================================================

        allModels {
//            nameAndWhereto {
//                dtoNameAndWhereto {
//                    classPostfix = "Dto"
//                    packageName("dto")
//                }
//                tableNameAndWhereto {
//                    classPostfix = "Table"
//                    packageName("table")
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
