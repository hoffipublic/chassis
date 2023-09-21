package com.hoffi.chassis.examples.basic

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.ReplaceAppendOrModify.APPEND
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.chassismodel.typ.mutable
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface.INTERFACE
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.dsl.modelgroup.*
import com.hoffi.chassis.dsl.modelgroup.IDslApiConstructorVisibility.VISIBILITY.PROTECTED
import com.hoffi.chassis.dsl.modelgroup.IDslApiConstructorVisibility.VISIBILITY.PUBLIC
import com.hoffi.chassis.dsl.modelgroup.crud.IDslApiOuterCrudBlock
import com.hoffi.chassis.dsl.modelgroup.crud.IDslApiPrefixedCrudScopeBlock
import com.hoffi.chassis.dsl.scratchdslEXAMPLES.COMMON__PERSISTENT_OPTIMISTIC
import com.hoffi.chassis.dsl.whereto.IDslApiNameAndWheretoOnSubElements
import com.hoffi.chassis.shared.helpers.See
import com.hoffi.chassis.shared.shared.COPYTYPE.IGNORE
import com.hoffi.chassis.shared.shared.COPYTYPE.NEW
import com.hoffi.chassis.shared.shared.CrudData.CRUD.Companion.CREATE
import com.hoffi.chassis.shared.shared.CrudData.CRUD.Companion.READ
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM.*
import com.hoffi.generated.universe.Dummy

const val ENTITYGROUP = "Entitygroup"
const val ENTITY__BASE      = ""
const val ENTITY__ENTITY    = "Entity"
const val ENTITY__SUBENTITY = "Subentity"
const val ENTITY__SOMEMODEL = "SomeModel"
const val ENTITY__SOMEOTHER = "SomeOther"


context(DslCtxWrapper)
fun entitiesFunc() {
    dslCtx.topLevelDslFunctionName = object{}.javaClass.enclosingMethod.name
//    apigroup(SIMPLE) {
//
//    }
    modelgroup(ENTITYGROUP) {               /** @see com.hoffi.chassis.dsl.modelgroup.DslModelgroup */
        See(DslModelgroup::class)
        nameAndWhereto { /** @see com.hoffi.chassis.dsl.whereto.IDslApiNameAndWheretoOnSubElements */
            See(IDslApiNameAndWheretoOnSubElements::class)
            classPrefix("Simple")
            packageName("entity")
            dtoNameAndWhereto {
                classPostfix("Dto")
                packageName("dto")
            }
            tableNameAndWhereto {
                classPostfix("Table")
                packageName("table")
            }
        }
        // TODO let modelgroup also have extends Delegate
        //extends {
        //
        //}

        constructorVisibility = PROTECTED

        // ================================================================================================================================

        model(ENTITY__BASE) {
            See(IDslApiModel::class)
            kind = INTERFACE
            dto {}
        }

        // ================================================================================================================================

        model(ENTITY__ENTITY) {
            See(IDslApiModel::class)
            extends {  /** @see com.hoffi.chassis.dsl.modelgroup.IDslApiExtendsBlock */
                See(IDslApiExtendsBlock::class)
                + (MODEL inModelgroup PERSISTENTGROUP withModelName COMMON__PERSISTENT_OPTIMISTIC)
                //- ENTITY__BASE
                // NEXT WILL BREAK for Table
                //+ ( (MODEL inModelgroup COMMON withModelName COMMON__PERSISTENT) ) // withName COMMON__PERSISTENT) //
                //+ com.hoffi.chassis.shared.Dummy::class // special models overwrite non-Interface super classes
            }
            See(IDslApiPropFuns::class)
            property("name", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("value", TYP.STRING, mutable, length = 4096, Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("prio", TYP.INT, mutable, Tag.TO_STRING_MEMBER)
            property("aInstant", TYP.INSTANT, mutable)
            property("aLocalDateTime", TYP.LOCALDATETIME, mutable)
            //property("someObject", Dummy::class, mutable, Tag.NO_DEFAULT_INITIALIZER, Tag.TRANSIENT)
            property("someObject", Dummy::class, mutable, Initializer.of("%T.%L", Dummy::class, "NULL"), length = C.DEFAULT_INT, Tag.TRANSIENT)
            property("someModelObject", DTO of ENTITY__SOMEMODEL, mutable)
            property("subentitys", "modelgroup:$ENTITYGROUP|model:$ENTITY__SUBENTITY", DTO, COLLECTIONTYP.SET, Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER, Tag.NULLABLE)
            property("listOfStrings", TYP.STRING, COLLECTIONTYP.LIST, Tag.COLLECTION_IMMUTABLE, Tag.CONSTRUCTOR)

            //propertiesOf("...")
//            manyToMany(SIMPLE__SUBENTITY) {
//                // not implemented yet, and not sure if an own clause or via property(...)
//            }

            addToStringMembers("aLocalDateTime", "updatedAt")

            dto {
                See(IDslApiDto::class, IDslApiSubelementsOnlyCommon::class)
                extends {
                    See(IDslApiExtendsBlock::class)
                    + (MODEL inModelgroup PERSISTENTGROUP withModelName COMMON__PERSISTENT_OPTIMISTIC)
                    //replaceSuperclass = true
                    - ENTITY__BASE
                    //+ ( (MODEL inModelgroup COMMON withModelName COMMON__PERSISTENT_OPTIMISTIC) ) // withName COMMON__PERSISTENT) //
                }
                See(IDslApiPropFuns::class)
//                annotateProperty("someObject", AnnotationSpec.builder(Contextual::class))
                property("dtoSpecificProp", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.DEFAULT_INITIALIZER)
                //propertiesOf( (MODEL inModelgroup PERSISTENTGROUP withModelName PERSISTENT__PERSISTENT) )
                See(IDslApiInitializer::class)
                initializer("dtoSpecificProp", APPEND, "/* some dto specific comment */")
                initializer("prio", APPEND, "/* some dto prio comment */")

                addToStringMembers("dtoSpecificProp", "createdAt")
                removeToStringMembers("prio")
            }

            tableFor(DTO) {
                See(IDslApiTable::class, IDslApiInitializer::class)
                initializer("name", APPEND, ".uniqueIndex()")
                initializer("prio", APPEND, "/* some table prio comment */")

                crud {
                    See(IDslApiOuterCrudBlock::class)
                    STANDARD FOR DTO
                    +DTO
                    CRUDALL FOR DTO
                    prefixed("somePrefix") {
                        See(IDslApiPrefixedCrudScopeBlock::class)
                        (READ.viaAllVariants FOR DTO) deepRestrictions  {
                            IGNORE propName "someModelObject"
                            IGNORE("someModelObject", "prefix1")
                        }
                        CREATE FOR DTO deepRestrictions  {
                            IGNORE propName "subentitys"
                            IGNORE("subentitys", "someModelObject", "prefix2")
                        }
                    }
                    //READSELECT FOR DTO
                    //READSELECT FOR (DTO of ENTITY__SUBENTITY)
                    prefixed("subentity") {
                        READ FOR (DTO of ENTITY__SUBENTITY) deepRestrictions {
                            IGNORE propName "subentitys"
                            IGNORE("subentitys", "someModelObject")
                            IGNORE model (DTO of ENTITY__SUBENTITY) onlyIf COLLECTIONTYP.COLLECTION
                        }
                    }
                    prefixed("withoutModels") {
                        +DTO deepRestrictions  {
                            IGNORE propName "subentitys"
                            IGNORE("subentitys", "someModelObject", "withoutModels1")
                        }
                    }
                    prefixed("woModels") {
                        CRUDALL FOR (DTO inModelgroup ENTITYGROUP withModelName ENTITY__SOMEMODEL) deepRestrictions  {
                            NEW model (DTO of ENTITY__SUBENTITY)
                        }
                    }
                }
            }

            filler {
                See(IDslApiOuterFillerBlock::class)
                +DTO // DTO filled by a DTO
                DTO mutual TABLE
                DTO mutual TABLE
                DTO from TABLE
                TABLE from DTO
                DTO from (DTO of ENTITY__SUBENTITY) // TODO check if this corresponding virtual Filler is also created because of (next line)
                (DTO of ENTITY__SUBENTITY) from TABLE
                //(DTO inModelgroup PERSISTENTGROUP withModelName PERSISTENT__PERSISTENT) from DTO
                prefixed("withoutModels") {
                    See(IDslApiPrefixedCrudScopeBlock::class)
                    (DTO mutual TABLE) shallowRestrictions {
                        IGNORE propName "someModelObject∆" // TODO XXX ∆ check if prop exists!!!
                        IGNORE("dtoSpecificProp", "someObject", "aLocalDateTime")
                        IGNORE("someModelObject") // vararg
                        copyBoundry(IGNORE, "someModelObject") // vararg extended form
                    }
                    (DTO mutual TABLE) deepRestrictions {
                        IGNORE propName "subentitys"
                        IGNORE("subentitys") // vararg
                        copyBoundry(IGNORE, "subentitys") // vararg extended form
                        IGNORE model (DTO of ENTITY__SUBENTITY) onlyIf COLLECTIONTYP.COLLECTION
                    }
                    FOR((TABLE from DTO), (DTO from TABLE)) deepRestrictions {
                        IGNORE propName "subentitys"
                        IGNORE("subentitys") // vararg
                    }
                    +DTO deepRestrictions {
                    //FOR DTO {
                        copyBoundry(IGNORE, "subentitys", "someModelObject")
                    }
                    FOR(+DTO, +DTO) deepRestrictions {
                        copyBoundry(IGNORE, "subentitys", "someModelObject")
                    }
                }
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
                    + ( (MODEL inModelgroup PERSISTENTGROUP withModelName PERSISTENT__BASE) ) // withName COMMON__PERSISTENT) //
                }
                property("subEntityDtoSpecificProp", TYP.STRING, mutable = mutable, Tag.CONSTRUCTOR)
                //property("entityBackreference", DTO of ENTITY__ENTITY, mutable, Tag.TRANSIENT)

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

            tableFor(DTO) {
            }
        }

        model(ENTITY__SOMEMODEL) {
            extends {
                +(MODEL inModelgroup PERSISTENTGROUP withModelName COMMON__PERSISTENT_OPTIMISTIC)
            }
            property("someName", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("someValue", TYP.STRING, mutable, length = 4096, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            dto {
            }
            tableFor(DTO) {
            }
            filler {
                +DTO
                DTO mutual TABLE
            }
        }
        model(ENTITY__SOMEOTHER) {
            extends {
                +(MODEL inModelgroup PERSISTENTGROUP withModelName COMMON__PERSISTENT_OPTIMISTIC)
            }
            property("someName", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("someValue", TYP.STRING, mutable, length = 4096, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            dto {
            }
            tableFor(DTO) {
            }
            filler {
                +DTO
                DTO mutual TABLE
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
