package com.hoffi.chassis.dsl.scratchdslEXAMPLES

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.*
import com.hoffi.chassis.dsl.internal.DslRun
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.DslRefString
import com.hoffi.chassis.shared.dsl.GatherPropertiesEnum

const val COMMON = "CommonModel"
const val COMMON__INTFC                 = "$COMMON;Intfc"
const val COMMON__ROOT                  = "$COMMON;"
const val COMMON__PERSISTENT            = "$COMMON;Persistent"
const val COMMON__TRANSIENT_STATE       = "$COMMON;TransientState"
const val COMMON__PERSISTENT_OPTIMISTIC = "$COMMON;PersistentOptimistic"

context(DslRun)
fun commonBaseModels(dslDiscriminator: DslDiscriminator = DslDiscriminator(C.DEFAULTSTRING)) {
    with (dslDiscriminator) {
    modelgroup(COMMON) {
        // property() in group itself?
        nameAndWhereto {
            baseDir("common")
            packageName("common")
            dtoNameAndWhereto {
                baseDir("dtoType")
                packageName("dtoType")
                classPrefix("Common")
            }
        }
        model(COMMON__INTFC) {
            kind = INTERFACE
            propertiesOf(DslRefString.modelElementRef(COMMON), GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
//            subPackage("base")
            nameAndWhereto {
                packageName = dslRun.wheretoImpl.packageName
                dtoNameAndWhereto {  }
                //fillerWhereto {  }

                val countInCommonBaseModels: Int = dslCtx.countModelsOfModelgroup(groupDslRef as DslRef.DslGroupRefEither.DslModelgroupRef)
                classPostfix = "overwrite"
                classPostfix(countInCommonBaseModels.toString())
            }
            dto {
                nameAndWhereto {
                    classPostfix = "DtoType"
                    packageName("dtoType")
                    classPrefix = "Common"
                }
            }
            kind = CLASS
            dto("other") {
                nameAndWhereto {  }
                kind = INTERFACE
            }
            table {
                kind = OBJECT
            }
        }
//        modelElement(COMMON__ROOT) {
//            subPackage("base")
//            classModifiers(KModifier.ABSTRACT)
//            dtoType { }
//        }
//        modelElement(COMMON__PERSISTENT) {
//            extends { +COMMON__ROOT }
//            classModifiers(KModifier.ABSTRACT)
//            property("uuid", TYP.UUID, mutable, Tag.PRIMARY)
//            property("createdAt", TYP.LOCALDATETIME, mutable)
//            property("updatedAt", TYP.LOCALDATETIME, mutable)
//            property("createUser", TYP.STRING, mutable)
//            property("updateUser", TYP.STRING, mutable)
//            initBusinessValues {
//                "createdAt"  with Initializer.of("%L", DEFAULT_INITCODE_LOCALDATETIME)
//                "updatedAt"  with Initializer.of("%L", DEFAULT_INITCODE_LOCALDATETIME)
//                "createUser" with Initializer.of("%S", DEFAULT_USER)
//                "updateUser" with Initializer.of("%S", DEFAULT_USER)
//            }
//            dtoType {
//                postfix("SomeXXX")
//            }
//        }
//        modelElement(COMMON__TRANSIENT_STATE) {
//            classModifiers(KModifier.ABSTRACT)
//            extends { +COMMON__PERSISTENT }
//            property("created", TYP.BOOL, mutable, Tag.TRANSIENT)
//            property("modified", TYP.BOOL, mutable, Tag.TRANSIENT)
//            property("deleted", TYP.BOOL, mutable, Tag.TRANSIENT)
//
//            dtoType { }
//        }
//        modelElement(COMMON__PERSISTENT_OPTIMISTIC) {
//            subPackage("base")
//            extends { +COMMON__TRANSIENT_STATE }
//            classModifiers(KModifier.ABSTRACT)
//            property("optimisticLockId", TYP.LONG, mutable)
//            initBusinessValues {
//                "optimisticLockId" with Initializer.of("%L", DEFAULT_OPTIMISTIC_LOCK_ID)
//            }
//            dtoType { }
//        }
//        // ================================================================================================================================
//
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
}
