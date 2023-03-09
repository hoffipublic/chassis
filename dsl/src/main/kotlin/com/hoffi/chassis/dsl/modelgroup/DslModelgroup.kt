package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.ChassisDslMarker
import com.hoffi.chassis.chassismodel.dsl.DslInstance
import com.hoffi.chassis.dsl.internal.DslBlockOn
import com.hoffi.chassis.dsl.internal.DslRun
import com.hoffi.chassis.dsl.internal.TopLevelDslFunction
import com.hoffi.chassis.dsl.modelgroup.allmodels.AllModels
import com.hoffi.chassis.dsl.modelgroup.allmodels.IAllModels
import com.hoffi.chassis.dsl.whereto.INameAndWheretoPlusModelSubtypes
import com.hoffi.chassis.dsl.whereto.NameAndWheretoPlusModelSubtypesImpl
import com.hoffi.chassis.shared.dsl.DslDiscriminatorWrapper
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslClass
import com.hoffi.chassis.shared.dsl.IDslRef

@ChassisDslMarker
class DslModelgroup(
    val modelgroupRef: DslRef.modelgroup,
    override val parent: TopLevelDslFunction,
    val nameAndWheretoPlusModelSubtypesImpl: NameAndWheretoPlusModelSubtypesImpl = NameAndWheretoPlusModelSubtypesImpl(modelgroupRef),
    val gatherPropertiesImpl: DslGatherPropertiesImpl = DslGatherPropertiesImpl(modelgroupRef)
)
    : IDslClass,
    INameAndWheretoPlusModelSubtypes by nameAndWheretoPlusModelSubtypesImpl,
    IDslGatherPropertiesModelAndElementsCommon by gatherPropertiesImpl
{
    override val selfDslRef: DslRef = modelgroupRef
    override val parentDslRef: IDslRef = parent.selfDslRef
    override val groupDslRef: DslRef.IGroupLevel = modelgroupRef
    override fun toString() = selfDslRef.toString()

//    override fun toString(): String = "${DslModelgroup::class.simpleName}[${modelgroupName.string}]"
//    val string = modelgroupName.string
////    var alternateBasePackage: String? = null
////        set(value) { if (dslCtx.DSLPASS == DslCtx.DSLPASS.ONE_BASEMODELS) field = value?.replace("/", ".") }
////    var alternateSrcPath: String? = null
////        set(value) { if (dslCtx.DSLPASS == DslCtx.DSLPASS.ONE_BASEMODELS) field = value?.replace(".", "/") }
//
//    @NonDslBlock var subPackage = modelgroupName.string.replace("/", ".").modifyPackagename()
//        private set(value) { field = value.replace("/", ".").modifyPackagename() }
//    @NonDslBlock fun subPackage(aSubPackage: String) {
//        this.subPackage = aSubPackage
//    }

//    var classesPrefix = ""
//        set(value) { field = value } //.modifyClassname()  }
//    var classesPostfix = ""
//        set(value) { field = value } //.modifyClassname()  }
//    fun classesPrefix(prefix: String) {
//        this.classesPrefix = prefix
//    }
//    fun classesPostfix(postfix: String) {
//        this.classesPostfix = postfix
//    }

    @DslInstance
    internal val allModels = mutableSetOf<AllModels>()

    @DslBlockOn(AllModels::class)
    fun allModels(allModelsBlock: IAllModels.() -> Unit) {
        val allModels = AllModels(DslRef.allModels(C.DEFAULTSTRING, modelgroupRef), this)
        this.allModels.add(allModels)
        allModels.apply(allModelsBlock)
    }

    @DslInstance
    var dslModels = mutableSetOf<DslModel>()

    context(DslRun, DslDiscriminatorWrapper)
    @DslBlockOn(DslModel::class)
    fun model(simpleName: String, dslModelBlock: IDslModel.() -> Unit) {
        val modelRef = DslRef.model(simpleName, modelgroupRef)
        val dslModel = dslCtx.createModel(simpleName, modelRef, this)
        dslModels.add(dslModel)
        dslModel.apply(dslModelBlock)

//        val modelNameString = if(name.contains(':')) name.s2() else name
//        when (dslCtx.DSLPASS) {
//            DslCtx.DSLPASS.ONE_BASEMODELS -> {
//                val modelElement = dslCtx.createModel(modelNameString, this).apply(dslModelBlock)
////                for (g in GENS.values()) { when (g) {
////                    GENS.UNDEF -> { }
////                    GENS.COMMON -> { }
////                    GENS.DTO -> { modelElement.dslDtoObj.kind = modelElement.theKind(GENS.DTO) }
////                    GENS.TABLE -> { modelElement.dslTableObj.kind = modelElement.theKind(GENS.TABLE) }
////                    GENS.FILLER -> { modelElement.dslTableObj.kind = modelElement.theKind(GENS.FILLER) }
////                }}
//            }
//            DslCtx.DSLPASS.TWO_TABLEMODELS -> dslCtx[ModelRef.from(modelgroupName, modelNameString)].apply(dslModelBlock)
//            DslCtx.DSLPASS.THREE_ALLMODELS -> { }
//            DslCtx.DSLPASS.FOUR_REFERENCING -> dslCtx[ModelRef.from(modelgroupName, modelNameString)].apply(dslModelBlock)
//            DslCtx.DSLPASS.FINISH -> { }
////                val dslModel = dslCtx[ModelRef.from(modelgroupName, modelNameString)]
////                dslModel.finish(GENS.DTO)
////                dslModel.finish(GENS.TABLE)
////                dslModel.finish(GENS.FILLER)
////            }
//        }
//    }
    }

//        if (dslCtx.DSLPASS != DslCtx.DSLPASS.TWO_TABLEMODELS) return
////        allModelsObj.apply(allModelsBlock)
//        // remember allModels { } clause to be called in DslCtx.DSLPASS.THREE_ALLMODELS via modelgroup { }
//        this.allModelsBlock = allModelsBlock
//    }
//    val allModelsObj = AllModels(modelgroupName)
//    var allModelsBlock: (AllModels.() -> Unit)? = null // making sure allModels { } runs after all dslModel { } clauses
//        val allModelsModel = AllModelsModel(modelgroupName)
//        val allModelsDto = AllModelsDto(modelgroupName)
//        val allModelsTable = AllModelsTable(modelgroupName)
//        val allModelsFiller = AllModelsFiller(modelgroupName)
//        @NonDslBlock fun modelElement(function: AllModelsModel.() -> Unit) { allModelsModel.apply(function) }
//        @NonDslBlock fun dtoType(function: AllModelsDto.() -> Unit) { allModelsDto.apply(function) }
//        @NonDslBlock fun tableType(function: AllModelsTable.() -> Unit) { allModelsTable.apply(function) }
//        @NonDslBlock fun filler(function: AllModelsFiller.() -> Unit) { allModelsFiller.apply(function) }
//    }
//    @DslModel class AllModelsModel(val modelgroupName: ModelgroupName) {
//        fun addToStringMembers(toStringMembersList: List<ModelGenPropRef>) {
//            dslCtx.allModels(modelgroupName).forEach {
//                it.addToStringMembers(toStringMembersList)
//            }
//        }
//        fun gatherPropertysOfSuperclasses(gentype: GENS) {
//            dslCtx.allModels(modelgroupName).forEach { it[gentype].gatherPropertysOfSuperclasses = gentype }
//        }
//    }
//    abstract class AllSpecificModels(val modelgroupName: ModelgroupName, var gentype: GENS) {
//        var constructorVisibility: Boolean = true
//            set(value) {
//                dslCtx.allModels(modelgroupName).forEach {
//                    when (gentype) {
//                        GENS.DTO -> it.dslDtoObj.constructorVisibility = value
//                        GENS.TABLE -> it.dslTableObj.constructorVisibility = value
//                        else -> throw DslException("not implemented yet!")
//                    }
//                }
//                field = value
//            }
//
//        @NonDslBlock fun addPrefix(prefix: String) {
//            dslCtx.allModels(modelgroupName).forEach {
//                when (gentype) {
//                    GENS.DTO -> it.dslDtoObj.addPrefixFromAll(prefix) // otherwise the dslCtx.DSLPASS will refuse to add it
//                    GENS.TABLE -> it.dslTableObj.addPrefixFromAll(prefix) // otherwise the dslCtx.DSLPASS will refuse to add it
//                    else -> throw DslException("not implemented yet!")
//                }
//            }
//        }
//        @NonDslBlock fun addPostfix(postfix: String) {
//            dslCtx.allModels(modelgroupName).forEach {
//                when (gentype) {
//                    GENS.DTO -> it.dslDtoObj.addPostfixFromAll(postfix) // otherwise the dslCtx.DSLPASS will refuse to add it
//                    GENS.TABLE -> it.dslTableObj.addPostfixFromAll(postfix) // otherwise the dslCtx.DSLPASS will refuse to add it
//                    else -> throw DslException("not implemented yet!")
//                }
//            }
//        }
////        fun prependSubpackage(subpackage: String) {
////            dslCtx.allModels(modelgroupName).forEach {
////                when (gentype) {
////                    GENS.COMMON -> throw DslException("unreachable")
////                    GENS.DTO -> it.dslDtoObj.prependSubpackageFromAll(subpackage)
////                    GENS.TABLE -> it.dslTableObj.prependSubpackageFromAll(subpackage)
////                    GENS.FILLER -> it.dslFillerObj.prependSubpackageFromAll(subpackage)
////                    GENS.UNDEF -> throw DslException("unreachable")
////                }
////            }
////        }
//    }
//    @DslModel class AllModelsDto(modelgroupName: ModelgroupName): AllSpecificModels(modelgroupName, GENS.DTO) {
//        @NonDslBlock fun addToStringMembers(toStringMembersList: List<ModelGenPropRef>) {
//            dslCtx.allModels(modelgroupName).forEach {
//                it.dslDtoObj.addToStringMembers(toStringMembersList = toStringMembersList)
//            }
//        }
//        @NonDslBlock fun gatherPropertysOfSuperclasses(gentype: GENS) {
//            dslCtx.allModels(modelgroupName).forEach { it.dslDtoObj.gatherPropertysOfSuperclasses = gentype }
//        }
//    }
//    @DslModel class AllModelsTable(modelgroupName: ModelgroupName): AllSpecificModels(modelgroupName, GENS.TABLE) {
//        @NonDslBlock fun gatherPropertysOfSuperclasses(gentype: GENS) {
//            dslCtx.allModels(modelgroupName).forEach { it.dslTableObj.gatherPropertysOfSuperclasses = gentype }
//        }
//    }
//    @DslModel class AllModelsFiller(modelgroupName: ModelgroupName): AllSpecificModels(modelgroupName, GENS.FILLER) {
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other !is DslModelgroup) return false
//        if (modelgroupName != other.modelgroupName) return false
//        return true
//    }
//
//    override fun hashCode(): Int {
//        return modelgroupName.hashCode()
//    }
}
