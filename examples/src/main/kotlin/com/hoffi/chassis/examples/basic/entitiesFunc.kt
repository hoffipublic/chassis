package com.hoffi.chassis.examples.basic

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.ReplaceAppendOrModify.APPEND
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.chassismodel.typ.mutable
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.dsl.modelgroup.IDslApiConstructorVisibility.VISIBILITY.PROTECTED
import com.hoffi.chassis.dsl.modelgroup.IDslApiConstructorVisibility.VISIBILITY.PUBLIC
import com.hoffi.chassis.dsl.scratchdslEXAMPLES.COMMON__PERSISTENT_OPTIMISTIC
import com.hoffi.chassis.shared.shared.FillerData.COPYTYPE.IGNORE
import com.hoffi.chassis.shared.shared.GatherPropertiesEnum
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM.*
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

        constructorVisibility = PROTECTED

        // ================================================================================================================================

        model(ENTITY__BASE) {
            kind = DslClassObjectOrInterface.INTERFACE
            dto {}
            table {}
        }

        // ================================================================================================================================

        model(ENTITY__ENTITY) {
            extends {
                + (MODEL inModelgroup PERSISTENTGROUP withModelName COMMON__PERSISTENT_OPTIMISTIC)
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
            property("someModelObject", DTO of ENTITY__SUBENTITY, mutable)
            property("subentitys", "modelgroup:$ENTITYGROUP|model:$ENTITY__SUBENTITY", DTO, COLLECTIONTYP.SET, Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER, Tag.NULLABLE)
            property("listOfStrings", TYP.STRING, COLLECTIONTYP.LIST, Tag.COLLECTION_IMMUTABLE, Tag.CONSTRUCTOR)

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
                property("dtoSpecificProp", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER)
                //propertiesOf( (MODEL inModelgroup PERSISTENTGROUP withModelName PERSISTENT__PERSISTENT) )
                initializer("dtoSpecificProp", APPEND, "/* some dto specific comment */")
                initializer("prio", APPEND, "/* some dto prio comment */")

//                addToStringMembers("dtoSpecificProp")
            }
            table {
                kind = DslClassObjectOrInterface.OBJECT
                extends {
                    replaceSuperclass = true
                }

                propertiesOf(DTO, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
                initializer("name", APPEND, ".uniqueIndex()")
                initializer("prio", APPEND, "/* some table prio comment */")
                //alterPropertyForDB("name", "uniqueIndex()")
            }
            filler {
                +DTO // DTO filled by a DTO
                (DTO mutual TABLE) //.copyBoundries(IGNORE, DTO, TABLE)
                (DTO mutual TABLE) //.copyBoundry(IGNORE, DTO, "entityBackreference").copyBoundry(IGNORE, TABLE, "entityBackreference")
                DTO from TABLE
                TABLE from DTO
                //having CopyBoundry first, as we otherwise cannot distinguish from obove's fillers'
                fillerName("withoutModels", TABLE from DTO, DTO from TABLE) {
                    copyBoundry(IGNORE, "subentitys", "someModelObject")
                }
                //(TABLE from DTO).copyBoundry(named = "withoutModels", IGNORE, "subentitys", "someModelObject")
                //(TABLE from DTO).copyBoundry(named = "withoutModels", IGNORE, DTO of ENTITY__SUBENTITY)
                DTO from (DTO of ENTITY__SUBENTITY)
                //(DTO inModelgroup PERSISTENTGROUP withModelName PERSISTENT__PERSISTENT) from DTO
            }

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
                constructorVisibility = PUBLIC
                extends {
                    + ( (MODEL inModelgroup PERSISTENTGROUP withModelName PERSISTENT__PERSISTENT) ) // withName COMMON__PERSISTENT) //
                }
                property("subEntityDtoSpecificProp", TYP.STRING, mutable = mutable, Tag.CONSTRUCTOR)
                property("entityBackreference", DTO of ENTITY__ENTITY, mutable, Tag.TRANSIENT)

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
