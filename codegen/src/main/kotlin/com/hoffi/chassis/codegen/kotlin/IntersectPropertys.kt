package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.shared.codegen.GenCtx
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.squareup.kotlinpoet.ClassName

object IntersectPropertys {
    data class CommonPropData(
        val genCtx: GenCtx,
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
        var targetVarName = "target$targetVarNamePostfix"
        var sourceVarName = "source$sourceVarNamePostfix"
        val targetPoetType: ClassName
            get() = targetGenModel.poetType
        val sourcePoetType: ClassName
            get() = sourceGenModel.poetType
        val dtoGenModelTarget: GenModel = genCtx.genModel(DslRef.dto(targetGenModel.modelSubElRef.simpleName, targetGenModel.modelSubElRef.parentDslRef))
        val tableGenModelTarget: GenModel = genCtx.genModel(DslRef.table(targetGenModel.modelSubElRef.simpleName, targetGenModel.modelSubElRef.parentDslRef))
        val dtoGenModelSource: GenModel = genCtx.genModel(DslRef.dto(sourceGenModel.modelSubElRef.simpleName, sourceGenModel.modelSubElRef.parentDslRef))
        val tableGenModelSource: GenModel = genCtx.genModel(DslRef.table(sourceGenModel.modelSubElRef.simpleName, sourceGenModel.modelSubElRef.parentDslRef))
    }

    fun intersectPropsOf(
        genCtx: GenCtx,
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
            genCtx,
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
