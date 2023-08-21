package com.hoffi.chassis.examples.basic

import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.chassismodel.typ.mutable
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.INTERFACE
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.OBJECT
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.KModifier

const val PERSISTENTGROUP = "Persistentgroup"
const val PERSISTENT__INTFCPERSISTENT       = "fc"
const val PERSISTENT__Abstract              = "Abstract"
const val PERSISTENT__BASE                  = "Base"
const val PERSISTENT__TRANSIENT_STATE       = "TransientState"
const val PERSISTENT__PERSISTENT_OPTIMISTIC = "PersistentOptimistic"

context(DslCtxWrapper)
fun baseModelsPersistent() {
    dslCtx.topLevelDslFunctionName = object{}.javaClass.enclosingMethod.name
    modelgroup(PERSISTENTGROUP) {
        nameAndWhereto {
            classPrefix("Persistent")
            packageName("persistent")
            dtoNameAndWhereto {
                classPostfix("Dto")
                packageName("dto")
            }
            tableNameAndWhereto {
                classPostfix("Table")
                packageName("table")
            }
        }
        model(PERSISTENT__INTFCPERSISTENT) {
            dto {
                kind = INTERFACE
            }
            dto("other") {
                kind = OBJECT
            }
        }
        model(PERSISTENT__Abstract) {
            classModifiers(KModifier.ABSTRACT)
            dto {
                + KModifier.ABSTRACT
                extends { -PERSISTENT__INTFCPERSISTENT }
            }
        }
        model(PERSISTENT__BASE) {
            classModifiers(KModifier.ABSTRACT)
            property("uuid", TYP.UUID, mutable, Tag.PRIMARY, Tag.TO_STRING_MEMBER)
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
                extends { +PERSISTENT__Abstract } // TODO this@model
                classMods { }
                nameAndWhereto {
                    classPostfix("Base")
                }
            }
        }
        model(PERSISTENT__TRANSIENT_STATE) {
            classModifiers(KModifier.ABSTRACT)
            property("created", TYP.BOOL, mutable, Tag.TRANSIENT)
            property("modified", TYP.BOOL, mutable, Tag.TRANSIENT)
            property("deleted", TYP.BOOL, mutable, Tag.TRANSIENT)

            dto {
                extends { +PERSISTENT__BASE }
            }
        }
        model(PERSISTENT__PERSISTENT_OPTIMISTIC) {
            classModifiers(KModifier.ABSTRACT)
            property("optimisticLockId", TYP.LONG, mutable)
//            initBusinessValues {
//                "optimisticLockId" with Initializer.of("%L", DEFAULT_OPTIMISTIC_LOCK_ID)
//            }
            dto {
                extends { +PERSISTENT__TRANSIENT_STATE }
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
