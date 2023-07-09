package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.nameandwhereto.IModelClassName
import com.hoffi.chassis.shared.parsedata.nameandwhereto.ModelClassName
import com.hoffi.chassis.shared.shared.Extends
import com.hoffi.chassis.shared.shared.GatherPropertys
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

sealed class GenModel(modelSubElRef: DslRef.IModelSubelement, modelClassName: ModelClassName)
    : ModelClassData(modelSubElRef, modelClassName) {
    class DtoModel(dtoRef: DslRef.dto, modelClassName: ModelClassName) : GenModel(dtoRef, modelClassName) { init { modelClassName.modelClassData = this } }
    class TableModel(tableRef: DslRef.table, modelClassName: ModelClassName) : GenModel(tableRef, modelClassName) { init { modelClassName.modelClassData = this } }
}

/** all props and sub-props are set on chassis DSL PASS_FINISH */
abstract class ModelClassData(
    var modelSubElRef: DslRef.IModelSubelement,
    val modelClassName: ModelClassName
) : Comparable<ModelClassData>,
    IModelClassName by modelClassName
{
    override fun toString() = "${this::class.simpleName} ${classModifiers.joinToString(" ")} $kind $modelClassName"
    var kind: TypeSpec.Kind = TypeSpec.Kind.CLASS
    val isInterface: Boolean
        get() = kind == TypeSpec.Kind.INTERFACE
    val classModifiers: MutableSet<KModifier> = mutableSetOf()
    val extends: MutableMap<String, Extends> = mutableMapOf()
    var constructorVisibility = true
    val directProps: MutableMap<String, Property> = mutableMapOf()
    val gatheredProps: MutableMap<String, Property> = mutableMapOf()
    val gatheredPropsDslModelRefs: MutableSet<GatherPropertys> = mutableSetOf()
    // convenience maps for codeGen
    val allProps: MutableMap<String, Property> by lazy { (gatheredProps + directProps).toMutableMap() }

    var isUuidPrimary = false

    // TODO delegate to concrete sealed class implementation
//    val modelFunSpecs = mutableMapOf<String, FunSpec.Builder>()
//    val incomingFKs = sortedSetOf<Models.DslFK>() // TODO are these really used? see AKotlinClass
//    val outgoingFKs = sortedSetOf<Models.DslFK>() // TODO are these really used? see AKotlinClass

    fun filterProps(propFilter: (Property) -> Boolean): MutableSet<Property> = allProps.values.filter(propFilter).toMutableSet()
    fun filterGatheredProps(propFilter: (Property) -> Boolean = { true }): MutableSet<Property> = gatheredProps.values.filter(propFilter).toMutableSet()
    //fun filterInclSuperclassPropsMap(propFilter: (Property) -> Boolean = { true }): MutableSet<Property> = propsInclSuperclassPropsMap.values.filter(propFilter).toMutableSet()
    fun getProp(name: String): Property = allProps[name] ?: throw GenException("$this does not contain a property named $name")

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
