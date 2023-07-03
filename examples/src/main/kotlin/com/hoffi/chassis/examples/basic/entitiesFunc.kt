package com.hoffi.chassis.examples.basic

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.dsl.scratchdslEXAMPLES.COMMON__PERSISTENT_OPTIMISTIC
import com.hoffi.chassis.shared.COLLECTIONTYP
import com.hoffi.chassis.shared.TYP
import com.hoffi.chassis.shared.mutable
import com.hoffi.chassis.shared.shared.GatherPropertiesEnum
import com.hoffi.chassis.shared.shared.Initializer
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM.MODEL
import com.hoffi.generated.universe.Dummy

const val ENTITYGROUP = "Entitygroup"
const val ENTITY__BASE      = ""
const val ENTITY__ENTITY    = "Entity"
const val ENTITY__SUBENTITY = "Subentity"

context(DslCtxWrapper)
fun entities() {
    dslCtx.topLevelDslFunctionName = object{}.javaClass.enclosingMethod.name
//    apigroup(SIMPLE) {
//
//    }
    modelgroup(ENTITYGROUP) {
        nameAndWhereto {
            classPrefix("Simple")
            classPostfix("Dto")
            packageName("entity")
        }

        // ================================================================================================================================

        model(ENTITY__BASE) {
            kind = DslClassObjectOrInterface.INTERFACE
            dto {}
            table {}
        }

        // ================================================================================================================================

        model(ENTITY__ENTITY) {
            extends {
                //+ (MODEL inModelgroup PERSISTENTGROUP withModelName COMMON__PERSISTENT_OPTIMISTIC)
                //- ENTITY__BASE
                // NEXT WILL BREAK for Table
                //+ ( (MODEL inModelgroup COMMON withModelName COMMON__PERSISTENT) ) // withName COMMON__PERSISTENT) //
                //+ com.hoffi.chassis.shared.Dummy::class // special models overwrite non-Interface super classes
            }

            property("name", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("value", TYP.STRING, mutable, length = 4096, Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("prio", TYP.INT, mutable, Tag.TO_STRING_MEMBER)
            property("aInstant", TYP.INSTANT, mutable)
            property("aLocalDateTime", TYP.LOCALDATETIME, mutable)
            //property("someObject", Dummy::class, mutable, Tag.NO_DEFAULT_INITIALIZER, Tag.TRANSIENT)
            property("someObject", Dummy::class, mutable, Initializer.of("%T.%L", Dummy::class, "NULL"), length = C.DEFAULT_INT, Tag.TRANSIENT)
            //property("someModelObject", SIMPLE__SUBENTITY, GENS.DTO, mutable, Tag.NULLABLE)
            property("subentitys", "modelgroup:$ENTITYGROUP;model:$ENTITY__SUBENTITY", MODELREFENUM.DTO, COLLECTIONTYP.SET, Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER, Tag.NULLABLE)
            property("listOfStrings", TYP.STRING, COLLECTIONTYP.LIST, Tag.COLLECTION_IMMUTABLE, Tag.CONSTRUCTOR, Tag.TRANSIENT)

//            initBusinessValues {
//                init("someObject", Initializer.of("%T(%L)", Dummy::class.asTypeName(), 42))
//            }
//
//            manyToMany(SIMPLE__SUBENTITY) {
//                // not implemented yet, and not sure if an own clause or via property(...)
//            }
//
//            //addToStringMembers(toStringMembersList = COMMON_MODEL___toStringMembers)

            dto {
                extends {
                    + (MODEL inModelgroup PERSISTENTGROUP withModelName COMMON__PERSISTENT_OPTIMISTIC)
                    //replaceSuperclass = true
                    - ENTITY__BASE
                    //+ ( (MODEL inModelgroup COMMON withModelName COMMON__PERSISTENT_OPTIMISTIC) ) // withName COMMON__PERSISTENT) //
                }
//                annotateProperty("someObject", AnnotationSpec.builder(Contextual::class))
                property("dtoSpecificProp", TYP.STRING, mutable, Tag.CONSTRUCTOR)
                //propertiesOf( (MODELREFENUM.DTO inModelgroup PERSISTENTGROUP withModelName PERSISTENT__TRANSIENT_STATE), GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
//                initBusinessValues {
//                    "someObject"      with Initializer.of("%T(%L)", Dummy::class.asTypeName(), 43)
//                    "dtoSpecificProp" with Initializer.of("%S", "businessInitialized")
//                    "someModelObject" with BUSINESSINIT.INIT
//                }
//                addToStringMembers("dtoSpecificProp")
            }
            table {
                kind = DslClassObjectOrInterface.OBJECT
                extends {
                    replaceSuperclass = true
                }

                propertiesOf(MODELREFENUM.DTO, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
                //alterPropertyForDB("name", "uniqueIndex()")
            }
//            filler {
//                +GENS.DTO
//                fill(GENS.DTO, CopyBoundry of COPYTYPE.IGNORE forPropRef SIMPLE__SUBENTITY propRef "someModelObject")
//                fill(GENS.DTO, CopyBoundry of COPYTYPE.IGNORE forPropRef "$SIMPLE__SUBENTITY:someModelObject")
//                fill(GENS.DTO mutual GENS.TABLE, CopyBoundry of COPYTYPE.INSTANCE forPropRef SIMPLE__SUBENTITY propRef "someModelObject")
//                fill(GENS.DTO from GENS.TABLE)
//                fill(GENS.TABLE from GENS.DTO)
        }
//
//        // ================================================================================================================================
//
        model(ENTITY__SUBENTITY) {
            property("name", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("value", TYP.STRING, mutable, length = 4096, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("prio", TYP.INT, mutable, Tag.TO_STRING_MEMBER)
            property("aInstant", TYP.INSTANT, mutable)
            property("aLocalDateTime", TYP.LOCALDATETIME, mutable)
            ////addToStringMembers(toStringMembersList = COMMON_MODEL___toStringMembers)

            dto {
                extends {
                    + ( (MODEL inModelgroup PERSISTENTGROUP withModelName PERSISTENT__PERSISTENT) ) // withName COMMON__PERSISTENT) //
                }
                property("subEntityDtoSpecificProp", TYP.STRING, mutable = mutable, Tag.CONSTRUCTOR)
                //property("entityBackreference", MODELREFENUM.DTO of ENTITY__ENTITY, mutable, Tag.TRANSIENT)

                //initBusinessValues {
                //    "dtoSpecificProp" with Initializer.of("%S", "subentity businessInitialized")
                //}
//                function("someInit") {
//                    addModifiers(KModifier.OVERRIDE, KModifier.PROTECTED)
//                    returns(dslCtx.typeWrapper(modelGenRef).typeName)
//                    addCode("""
//                        |super.init()
//                        |return this
//                        |""".trimMargin(),
//                    )
//                }
//                addToStringMembers("dtoSpecificProp")
            }
            table {
                propertiesOf(MODELREFENUM.DTO, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
            }
//            filler {
//                +GENS.DTO
//                fill(GENS.DTO mutual GENS.TABLE)
//                fill(GENS.DTO from GENS.DTO)
//                //from(GENS.TABLE to GENS.DTO)
//            }
        }

//        // ================================================================================================================================
//
//        allModels {
//            modelElement {
//            }
//            dtoType {
//                addPostfix("DtoType")
//                constructorVisibility = false
//                val COMMON_MODEL___toStringMembers = listOf(
//                    COMMON__PERSISTENT           .modelGenPropRef("uuid"),
//                    COMMON__PERSISTENT           .modelGenPropRef("updatedAt"),
//                    COMMON__TRANSIENT_STATE      .modelGenPropRef("created"),
//                    COMMON__TRANSIENT_STATE      .modelGenPropRef("modified"),
//                    COMMON__TRANSIENT_STATE      .modelGenPropRef("deleted"),
//                    COMMON__PERSISTENT_OPTIMISTIC.modelGenPropRef("optimisticLockId"),
//                )
//                addToStringMembers(COMMON_MODEL___toStringMembers)
//            }
//            tableType {
//                addPostfix("TableType")
//                gatherPropertysOfSuperclasses(GENS.DTO)
//            }
//            filler {
//
//            }
//        }
    }
}
