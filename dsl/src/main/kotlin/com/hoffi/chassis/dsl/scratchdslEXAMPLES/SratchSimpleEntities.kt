package com.hoffi.chassis.dsl.scratchdslEXAMPLES

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.INTERFACE
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.OBJECT
import com.hoffi.chassis.dsl.internal.DslRun
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.dsl.DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslModelRef.MODELELEMENT.DTO
import com.hoffi.chassis.shared.dsl.DslRefString.modelElementRef
import com.hoffi.chassis.shared.values.gatherFromSuperclasses

const val SIMPLE = "Simple"
const val SIMPLE__ROOT      = "$SIMPLE;"
const val SIMPLE__ENTITY    = "$SIMPLE;Entity"
const val SIMPLE__SUBENTITY = "$SIMPLE;Subentity"

context(DslRun)
fun simpleEntities(dslDiscriminator: DslDiscriminator = DslDiscriminator(C.DEFAULTSTRING)) {
    with (dslDiscriminator) {
//    apigroup(SIMPLE) {
//
//    }
    modelgroup(SIMPLE) {
        nameAndWhereto {
            classPrefix = "Simple"
        }

        // ================================================================================================================================

        model(SIMPLE__ROOT) {
            kind = INTERFACE
//            dtoType {}
//            tableType {}
        }

        // ================================================================================================================================

        model(SIMPLE__ENTITY) {
            //extends {
            //    +SIMPLE__ROOT
            //    +com.hoffi.codegen.Dummy::class // special models overwrite non-Interface super classes
            //}
         // propertiesOf(DTO, gatherFromSuperclasses(false)) // SHOULD NOT EXIST ON model() { }
            propertiesOf("disc;modelgroup:someGroup;modelElement:someModel", gatherFromSuperclasses(false))
            propertiesOf("disc;modelgroup:someGroup;modelElement:someModel;DTO:someDto", gatherFromSuperclasses(false))
            propertiesOf("disc;modelgroup:someGroup;modelElement:someModel;DTO", gatherFromSuperclasses(false))
            propertiesOfSuperclasses()
         // propertiesOfSuperclassesOf(DTO) // SHOULD NOT EXIST ON model() { }

//            property("name", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
//            property("value", TYP.STRING, mutable, length = 4096, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
//            property("prio", TYP.INT, mutable, Tag.TO_STRING_MEMBER)
//            property("aInstant", TYP.INSTANT, mutable)
//            property("aLocalDateTime", TYP.LOCALDATETIME, mutable)
//            property("someObject", Dummy::class, mutable, Initializer.of("%T.%L", Dummy::class.asTypeName(), "NULL"), length = Defaults.DEFAULT_INT, Tag.TRANSIENT)
//            property("someModelObject", SIMPLE__SUBENTITY, GENS.DTO, mutable, Tag.NULLABLE)
//            property("subentitys", SIMPLE__SUBENTITY, GENS.DTO, COLLECTIONTYPE.SET, Tag.CONSTRUCTOR, Tag.NULLABLE)
//            property("listOfStrings", TYP.STRING, COLLECTIONTYPE.LIST, Tag.CONSTRUCTOR, Tag.TRANSIENT)
//
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
                propertiesOf(DTO, gatherFromSuperclasses(false))
                propertiesOf("disc;modelgroup:someGroup;modelElement:someModel", gatherFromSuperclasses(false))
                propertiesOf("disc;modelgroup:someGroup;modelElement:someModel;DTO:someDto", gatherFromSuperclasses(false))
                propertiesOf("disc;modelgroup:someGroup;modelElement:someModel;DTO", gatherFromSuperclasses(false))
                propertiesOfSuperclasses()
                propertiesOfSuperclassesOf(DTO)
                gatherPropertiesOf(modelElementRef(COMMON), true)
                //extends {
                //    replaceSuperclass = true
                //    from(COMMON__PERSISTENT_OPTIMISTIC)
                //}
//                annotateProperty("someObject", AnnotationSpec.builder(Contextual::class))
//                property("dtoSpecificProp", TYP.STRING, mutable = mutable, Tag.CONSTRUCTOR)
//                initBusinessValues {
//                    "someObject"      with Initializer.of("%T(%L)", Dummy::class.asTypeName(), 43)
//                    "dtoSpecificProp" with Initializer.of("%S", "businessInitialized")
//                    "someModelObject" with BUSINESSINIT.INIT
//                }
//                addToStringMembers("dtoSpecificProp")
            }
            table {
                kind = OBJECT
                propertiesOf(DTO, gatherFromSuperclasses(false))
                propertiesOfSuperclasses()
                propertiesOfSuperclassesOf(DTO)
                gatherPropertiesOf(modelElementRef(COMMON), true)
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
//        }
//
//        // ================================================================================================================================
//
//        modelElement(SIMPLE__SUBENTITY) {
//            property("name", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
//            property("value", TYP.STRING, mutable, length = 4096, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
//            property("prio", TYP.INT, mutable, Tag.TO_STRING_MEMBER)
//            property("aInstant", TYP.INSTANT, mutable)
//            property("aLocalDateTime", TYP.LOCALDATETIME, mutable)
//            //addToStringMembers(toStringMembersList = COMMON_MODEL___toStringMembers)
//
//            dtoType {
//                extends { from(COMMON__PERSISTENT_OPTIMISTIC) }
//                property("dtoSpecificProp", TYP.STRING, mutable = mutable, Tag.CONSTRUCTOR)
//                //initBusinessValues {
//                //    "dtoSpecificProp" with Initializer.of("%S", "subentity businessInitialized")
//                //}
////                function("someInit") {
////                    addModifiers(KModifier.OVERRIDE, KModifier.PROTECTED)
////                    returns(dslCtx.typeWrapper(modelGenRef).typeName)
////                    addCode("""
////                        |super.init()
////                        |return this
////                        |""".trimMargin(),
////                    )
////                }
//                addToStringMembers("dtoSpecificProp")
//            }
//            tableType {
//                gatherPropertysOfSuperclasses(GENS.DTO)
//            }
//            filler {
//                +GENS.DTO
//                fill(GENS.DTO mutual GENS.TABLE)
//                fill(GENS.DTO from GENS.DTO)
//                //from(GENS.TABLE to GENS.DTO)
//            }
//        }
//
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
}
