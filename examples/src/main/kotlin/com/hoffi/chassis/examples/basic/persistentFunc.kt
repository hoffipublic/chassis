package com.hoffi.chassis.examples.basic

import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.*
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.shared.TYP
import com.hoffi.chassis.shared.mutable
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.KModifier

const val PERSISTENTGROUP = "Persistentgroup"
const val PERSISTENT__INTFC                 = "IPersistent"
const val PERSISTENT__BASE                  = "Base"
const val PERSISTENT__PERSISTENT            = "Persistent"
const val PERSISTENT__TRANSIENT_STATE       = "TransientState"
const val PERSISTENT__PERSISTENT_OPTIMISTIC = "PersistentOptimistic"

context(DslCtxWrapper)
fun baseModelsPersistent() {
    dslCtx.topLevelDslFunctionName = object{}.javaClass.enclosingMethod.name
    modelgroup(PERSISTENTGROUP) {
        nameAndWhereto {
            packageName("persistent")
            dtoNameAndWhereto {
                classPostfix("PersistentDto")
                packageName("dto")
            }
            tableNameAndWhereto {
                classPostfix("PersistentTable")
                packageName("table")
            }
        }
        model(PERSISTENT__INTFC) {
            kind = CLASS
            dto {
                kind = INTERFACE
            }
            dto("other") {
                kind = OBJECT
            }
            table {
            }
        }
        model(PERSISTENT__BASE) {
            extends { +PERSISTENT__INTFC }
            classModifiers(KModifier.ABSTRACT)
            dto {
                + KModifier.ABSTRACT
            }
        }
        model(PERSISTENT__PERSISTENT) {
            extends { +PERSISTENT__BASE } // TODO this@model
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
                classMods { }
                nameAndWhereto {
                    classPostfix("Base")
                }
            }
        }
        model(PERSISTENT__TRANSIENT_STATE) {
            extends { +PERSISTENT__PERSISTENT }
            classModifiers(KModifier.ABSTRACT)
            property("created", TYP.BOOL, mutable, Tag.TRANSIENT)
            property("modified", TYP.BOOL, mutable, Tag.TRANSIENT)
            property("deleted", TYP.BOOL, mutable, Tag.TRANSIENT)

            dto {
            }
        }
        model(PERSISTENT__PERSISTENT_OPTIMISTIC) {
            extends { +PERSISTENT__TRANSIENT_STATE }
            classModifiers(KModifier.ABSTRACT)
            property("optimisticLockId", TYP.LONG, mutable)
//            initBusinessValues {
//                "optimisticLockId" with Initializer.of("%L", DEFAULT_OPTIMISTIC_LOCK_ID)
//            }
            dto {
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
