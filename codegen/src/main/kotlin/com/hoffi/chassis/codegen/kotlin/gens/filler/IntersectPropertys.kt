package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.squareup.kotlinpoet.ClassName

object IntersectPropertys {
    data class CommonPropData(
        val targetGenModel: GenModel,
        val targetFillerPoetType: ClassName,
        val sourceGenModel: GenModel,
        val sourceFillerPoetType: ClassName,
        val allIntersectPropSet: Set<Property>,
        val allIntersectPropSetSource: Set<Property>,
        val targetPropButNotInSourcePropSet: Set<Property>,
        val sourcePropButNotInTargetPropSet: Set<Property>,
        val sourceVarNamePostfix: String,
        val targetVarNamePostfix: String,
    ) {
        var sourceVarName = "source$sourceVarNamePostfix"
        var targetVarName = "target$targetVarNamePostfix"
        val targetPoetType: ClassName
            get() = targetGenModel.poetType as ClassName
        val sourcePoetType: ClassName
            get() = sourceGenModel.poetType as ClassName
    }

    fun intersectPropsOf(
        targetGenModel: GenModel,
        sourceGenModel: GenModel,
        sourceVarNamePostfix: String = "",
        targetVarNamePostfix: String = ""
    ): CommonPropData {
        val allIntersectPropSet = targetGenModel.propsInclSuperclassPropsMap.values.intersect(sourceGenModel.propsInclSuperclassPropsMap.values.toSet())
        /** as translated props have different EitherTypes */
        val allIntersectPropSetSource = sourceGenModel.propsInclSuperclassPropsMap.values.intersect((targetGenModel.propsInclSuperclassPropsMap.values).toSet())
        val targetPropButNotInSourcePropSet = targetGenModel.propsInclSuperclassPropsMap.values.toMutableSet().also { it.removeAll(allIntersectPropSet) }
        val sourcePropButNotInTargetPropSet = sourceGenModel.propsInclSuperclassPropsMap.values.toMutableSet().also { it.removeAll(allIntersectPropSetSource) }

        //val directIntersectPropSet = targetGenModel.allProps.values.intersect(sourceGenModel.allProps.values.toSet())
        ///** as translated props have different EitherTypes */
        //val directIntersectPropSetSource = sourceGenModel.allProps.values.intersect(targetGenModel.allProps.values.toSet())
        ///** as translated props have different EitherTypes */
        //val directSourcePropByName: Map<String, Property> = directIntersectPropSet.associateBy { it.name }
        //val directSourcePropButNotInTargetPropSet = sourceGenModel.allProps.values.toMutableSet().also { it.removeAll(directIntersectPropSet) }
        //val targetPropButNotInSourcePropSet = targetGenModel.allProps.values.toMutableSet().also { it.removeAll(directIntersectPropSet) }
        //val superModelsTargetIntersectPropSet = targetGenModel.superclassProps.values.intersect(targetPropButNotInSourcePropSet + sourceGenModel.superclassProps.values)
        ////val additionalDslRefsInSourceSuperclasses: Set<DslRef.IModelSubelement> = superModelsTargetIntersectPropSet.map { it.containedInSubelementRef }.toSet()

        val targetFillerPoetType = targetGenModel.fillerPoetType
        val sourceFillerPoetType = sourceGenModel.fillerPoetType

        return CommonPropData(
            targetGenModel,
            targetFillerPoetType,
            sourceGenModel,
            sourceFillerPoetType,
            allIntersectPropSet,
            allIntersectPropSetSource,
            targetPropButNotInSourcePropSet,
            sourcePropButNotInTargetPropSet,
            sourceVarNamePostfix,
            targetVarNamePostfix
        )
    }
}
