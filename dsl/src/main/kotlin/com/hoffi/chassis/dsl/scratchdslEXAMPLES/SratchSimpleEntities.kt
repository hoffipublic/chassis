package com.hoffi.chassis.dsl.scratchdslEXAMPLES

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.INTERFACE
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.OBJECT
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.shared.COLLECTIONTYP
import com.hoffi.chassis.shared.TYP
import com.hoffi.chassis.shared.dsl.DslRef.model.MODELELEMENT.DTO
import com.hoffi.chassis.shared.dsl.DslRef.model.MODELELEMENT.MODEL
import com.hoffi.chassis.shared.mutable
import com.hoffi.chassis.shared.shared.GatherPropertiesEnum
import com.hoffi.chassis.shared.shared.Initializer
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.asTypeName

const val SIMPLE = "Simple"
const val SIMPLE__ROOT      = ""
const val SIMPLE__ENTITY    = "Entity"
const val SIMPLE__SUBENTITY = "Subentity"

context(DslCtxWrapper)
fun simpleEntities() {
    dslCtx.topLevelDslFunctionName = object{}.javaClass.enclosingMethod.name
//    apigroup(SIMPLE) {
//
//    }
    modelgroup(SIMPLE) {
        nameAndWhereto {
            classPrefix("Simple")
        }

        // ================================================================================================================================

        model(SIMPLE__ROOT) {
            kind = INTERFACE
            dto {}
            table {}
        }

        // ================================================================================================================================

        model(SIMPLE__ENTITY) {
            extends {
                + SIMPLE__ROOT
                // NEXT WILL BREAK for Table
                //+ ( (MODEL inModelgroup COMMON withModelName COMMON__PERSISTENT) ) // withName COMMON__PERSISTENT) //
                //+ com.hoffi.chassis.shared.Dummy::class // special models overwrite non-Interface super classes
            }

            property("name", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("value", TYP.STRING, mutable, length = 4096, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("prio", TYP.INT, mutable, Tag.TO_STRING_MEMBER)
            property("aInstant", TYP.INSTANT, mutable)
            property("aLocalDateTime", TYP.LOCALDATETIME, mutable)
            property("someObject", com.hoffi.chassis.shared.Dummy::class, mutable, Initializer.of("%T.%L", com.hoffi.chassis.shared.Dummy::class.asTypeName(), "NULL"), length = C.DEFAULT_INT, Tag.TRANSIENT)
            //property("someModelObject", SIMPLE__SUBENTITY, GENS.DTO, mutable, Tag.NULLABLE)
            property("subentitys", "modelgroup:$SIMPLE;model:$SIMPLE__SUBENTITY", DTO, COLLECTIONTYP.SET, Tag.CONSTRUCTOR, Tag.NULLABLE)
            property("listOfStrings", TYP.STRING, COLLECTIONTYP.LIST, Tag.CONSTRUCTOR, Tag.TRANSIENT)

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
                    replaceSuperclass = true
                    + SIMPLE__ROOT
                    //+ ( (MODEL inModelgroup COMMON withModelName COMMON__PERSISTENT_OPTIMISTIC) ) // withName COMMON__PERSISTENT) //
                }
//                annotateProperty("someObject", AnnotationSpec.builder(Contextual::class))
                property("dtoSpecificProp", TYP.STRING, mutable = mutable, Tag.CONSTRUCTOR)
                propertiesOf( (DTO inModelgroup COMMON withModelName COMMON__TRANSIENT_STATE), GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
//                initBusinessValues {
//                    "someObject"      with Initializer.of("%T(%L)", Dummy::class.asTypeName(), 43)
//                    "dtoSpecificProp" with Initializer.of("%S", "businessInitialized")
//                    "someModelObject" with BUSINESSINIT.INIT
//                }
//                addToStringMembers("dtoSpecificProp")
            }
            table {
                kind = OBJECT
                extends {
                    replaceSuperclass = true
                }

                propertiesOf(DTO, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
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
        model(SIMPLE__SUBENTITY) {
            property("name", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("value", TYP.STRING, mutable, length = 4096, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("prio", TYP.INT, mutable, Tag.TO_STRING_MEMBER)
            property("aInstant", TYP.INSTANT, mutable)
            property("aLocalDateTime", TYP.LOCALDATETIME, mutable)
            ////addToStringMembers(toStringMembersList = COMMON_MODEL___toStringMembers)

            dto {
                extends {
                    + ( (MODEL inModelgroup COMMON withModelName COMMON__PERSISTENT) ) // withName COMMON__PERSISTENT) //
                }
                property("subEntityDtoSpecificProp", TYP.STRING, mutable = mutable, Tag.CONSTRUCTOR)

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
                propertiesOf(DTO, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
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
