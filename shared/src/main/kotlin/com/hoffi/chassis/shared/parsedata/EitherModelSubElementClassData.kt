package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.nameandwhereto.IModelClassName
import com.hoffi.chassis.shared.parsedata.nameandwhereto.ModelClassName
import com.hoffi.chassis.shared.shared.Extends
import com.hoffi.chassis.shared.shared.GatherPropertys
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

/** all props and sub-props are set on chassis DSL PASS_FINISH */
abstract class ModelClassData(
    var modelSubElRef: DslRef.IModelSubelement,
    val modelClassName: ModelClassName
) : Comparable<ModelClassData>,
    IModelClassName by modelClassName
{
    override fun toString() = "${this::class.simpleName} ${classModifiers.joinToString(" ")} $kind $modelClassName"
    var kind: TypeSpec.Kind = TypeSpec.Kind.CLASS
    val classModifiers = mutableSetOf<KModifier>()
    val extends = mutableSetOf<Extends>()
    var constructorVisibility = true
    val propertys = mutableMapOf<String, Property>()
    val gatheredPropertys = mutableMapOf<String, Property>()
    val gatheredFromDslRefs = mutableSetOf<GatherPropertys>()

    init {

    }

    // TODO delegate to concrete sealed class implementation
//    val className: ClassName
//        get() = typeWrapper.className
//    val varName: String
//        get() = typeWrapper.className.simpleName.modifyVarname()
//    val tableName: String
//        get() = typeWrapper.className.simpleName.removeSuffix("Table").modifyDbTableName()

//    val businessInitializers = mutableMapOf<String, DslClass.EitherInitializerOrBusinessInit>()
//    var didGatherPropsFromSuperclasses: GENS = GENS.COMMON // GENS.COMMON means did IGNORE gather Propertys of superclasses
//    val modelFunSpecs = mutableMapOf<String, FunSpec.Builder>()
//    var eitherExtendsModelOrClass: EitherExtendsModelOrClass = EitherTypeOrDslRef.ExtendsNothing.INSTANCE
//    val superInterfaces = mutableListOf<EitherExtendsModelOrClass>()

//    val incomingFKs = sortedSetOf<Models.DslFK>() // TODO are these really used? see AKotlinClass
//    val outgoingFKs = sortedSetOf<Models.DslFK>() // TODO are these really used? see AKotlinClass
    fun allPropertys(propFilter: (Property) -> Boolean = { true }): MutableSet<Property> = propertys.values.filter(propFilter).toMutableSet()
    fun allGatheredPropertys(propFilter: (Property) -> Boolean = { true }): MutableSet<Property> = gatheredPropertys.values.filter(propFilter).toMutableSet()
    fun getProp(name: String): Property = propertys[name]!!

//    data class SuperModelsAndClasses(val models: MutableList<EitherModelNew> = mutableListOf(), var nonModel: EitherExtendsModelOrClass = ExtendsNothing.INSTANCE)
//    fun superClasses(): SuperModelsAndClasses {
//        val superModelsAndClasses = SuperModelsAndClasses()
//        return recurseSuperClasses(eitherExtendsModelOrClass, superModelsAndClasses)
//    }
//    private fun recurseSuperClasses(eitherExtendsModelOrClass: EitherExtendsModelOrClass, superModelsAndClasses: SuperModelsAndClasses): SuperModelsAndClasses {
//        when (eitherExtendsModelOrClass) {
//            is ExtendsClass -> superModelsAndClasses.nonModel = eitherExtendsModelOrClass
//            is ExtendsModel -> {
//                val superModel = Models.get(eitherExtendsModelOrClass.modelGenRef)
//                superModelsAndClasses.models.add(superModel)
//                recurseSuperClasses(superModel.eitherExtendsModelOrClass, superModelsAndClasses)
//            }
//            is ExtendsNothing -> { }
//        }
//        return superModelsAndClasses
//    }

    override fun compareTo(other: ModelClassData): Int = modelSubElRef.refList.last().simpleName.compareTo(other.modelSubElRef.refList.last().simpleName)
    //region equals and hashCode ...
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModelClassData) return false
        if (modelSubElRef != other.modelSubElRef) return false
        return true
    }
    override fun hashCode(): Int {
        // careful(!!!), same as its DslRef (but should be ok, if you only use them as e.g. Map/Set alone(!)
        // and not e.g. with a common supertype or compare a Pair<DslRef, DslClass> with a Pair<DslClass, DslRef>
        return modelSubElRef.hashCode()
    }
    //endregion
}

sealed class EitherModel(modelSubElRef: DslRef.IModelSubelement, modelClassName: ModelClassName)
    : ModelClassData(modelSubElRef, modelClassName) {
    class DtoModel(dtoRef: DslRef.dto, modelClassName: ModelClassName) : EitherModel(dtoRef, modelClassName)
    class TableModel(tableRef: DslRef.table, modelClassName: ModelClassName) : EitherModel(tableRef, modelClassName)
}
