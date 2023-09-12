package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.shared.codegen.GenCtx
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.squareup.kotlinpoet.ClassName

object IntersectPropertys {
    data class CommonPropData(
        val genCtx: GenCtx,
        val targetGenModelFromDsl: GenModel,
        val targetFillerPoetType: ClassName,
        val sourceGenModelFromDsl: GenModel,
        val sourceFillerPoetType: ClassName,
        val allIntersectPropSet: Set<Property>,
        val allIntersectPropSetSource: Set<Property>,
        val targetPropButNotInSourcePropSet: Set<Property>,
        val sourcePropButNotInTargetPropSet: Set<Property>,
        val sourceVarNamePostfix: String,
        val targetVarNamePostfix: String,
    ) {
        var targetVarName = "target$targetVarNamePostfix"
        var sourceVarName = "source$sourceVarNamePostfix"
        val targetPoetType: ClassName
            get() = targetGenModelFromDsl.poetType
        val sourcePoetType: ClassName
            get() = sourceGenModelFromDsl.poetType
    }

    fun intersectPropsOf(
        genCtx: GenCtx,
        targetGenModelFromDsl: GenModel,
        sourceGenModelFromDsl: GenModel,
        sourceVarNamePostfix: String = "",
        targetVarNamePostfix: String = ""
    ): CommonPropData {
        val allIntersectPropSet = targetGenModelFromDsl.propsInclSuperclassPropsMap.values.intersect(sourceGenModelFromDsl.propsInclSuperclassPropsMap.values.toSet())
        /** as translated props have different EitherTypes */
        val allIntersectPropSetSource = sourceGenModelFromDsl.propsInclSuperclassPropsMap.values.intersect((targetGenModelFromDsl.propsInclSuperclassPropsMap.values).toSet())
        val targetPropButNotInSourcePropSet = targetGenModelFromDsl.propsInclSuperclassPropsMap.values.toMutableSet().also { it.removeAll(allIntersectPropSet) }
        val sourcePropButNotInTargetPropSet = sourceGenModelFromDsl.propsInclSuperclassPropsMap.values.toMutableSet().also { it.removeAll(allIntersectPropSetSource) }

        //val directIntersectPropSet = targetGenModelFromDsl.allProps.values.intersect(sourceGenModelFromDsl.allProps.values.toSet())
        ///** as translated props have different EitherTypes */
        //val directIntersectPropSetSource = sourceGenModelFromDsl.allProps.values.intersect(targetGenModelFromDsl.allProps.values.toSet())
        ///** as translated props have different EitherTypes */
        //val directSourcePropByName: Map<String, Property> = directIntersectPropSet.associateBy { it.name }
        //val directSourcePropButNotInTargetPropSet = sourceGenModelFromDsl.allProps.values.toMutableSet().also { it.removeAll(directIntersectPropSet) }
        //val targetPropButNotInSourcePropSet = targetGenModelFromDsl.allProps.values.toMutableSet().also { it.removeAll(directIntersectPropSet) }
        //val superModelsTargetIntersectPropSet = targetGenModelFromDsl.superclassProps.values.intersect(targetPropButNotInSourcePropSet + sourceGenModelFromDsl.superclassProps.values)
        ////val additionalDslRefsInSourceSuperclasses: Set<DslRef.IModelSubelement> = superModelsTargetIntersectPropSet.map { it.containedInSubelementRef }.toSet()

        val targetFillerPoetType = targetGenModelFromDsl.fillerPoetType
        val sourceFillerPoetType = sourceGenModelFromDsl.fillerPoetType

        return CommonPropData(
            genCtx,
            targetGenModelFromDsl,
            targetFillerPoetType,
            sourceGenModelFromDsl,
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
