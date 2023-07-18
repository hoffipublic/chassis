package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.reffing.MODELKIND
import com.squareup.kotlinpoet.ClassName

object IntersectPropertys {
    data class CommonPropData(
        val targetGenModel: GenModel,
        val targetFillerPoetType: ClassName,
        val sourceGenModel: GenModel,
        val sourceFillerPoetType: ClassName,
        val intersectPropSet: Set<Property>,
        val sourcePropByName: Map<String, Property>,
        val fromPropButNotToPropSet: Set<Property>,
        val targetPropButNotFromPropSet: Set<Property>,
        val superModelsTargetIntersectPropSet: Set<Property>,
        val additionalDslRefsInSourceSuperclasses: Set<IDslRef>,
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
        modelkind: MODELKIND,
        targetGenModel: GenModel,
        sourceGenModel: GenModel,
        sourceVarNamePostfix: String = "",
        targetVarNamePostfix: String = ""
    ): CommonPropData {

        val intersectPropSet = targetGenModel.allProps.values.intersect(sourceGenModel.allProps.values)
        /** as translated props have different EitherTypes */
        val intersectPropSetSource = sourceGenModel.allProps.values.intersect(targetGenModel.allProps.values)
        /** as translated props have different EitherTypes */
        val sourcePropByName: Map<String, Property> = intersectPropSetSource.map { it.name to it }.toMap()
        val sourcePropButNotInTargetPropSet = sourceGenModel.allProps.values.toMutableSet().also { it.removeAll(intersectPropSet) }
        val targetPropButNotInSourcePropSet = targetGenModel.allProps.values.toMutableSet().also { it.removeAll(intersectPropSet) }
        val superModelsTargetIntersectPropSet = targetGenModel.superclassProps.values.intersect(targetPropButNotInSourcePropSet + sourceGenModel.superclassProps.values)
        val additionalDslRefsInSourceSuperclasses: Set<DslRef.IModelSubelement> = superModelsTargetIntersectPropSet.map { it.containedInSubelementRef }.toSet()

        val i = 0 //TODO breakpoint; having the DB Translated props also, so: intersected props may have the same name, but different type (e.g. );

        val targetFillerPoetType = targetGenModel.fillerPoetType
        val sourceFillerPoetType = sourceGenModel.fillerPoetType

        return CommonPropData(
            targetGenModel,
            targetFillerPoetType,
            sourceGenModel,
            sourceFillerPoetType,
            intersectPropSet,
            sourcePropByName,
            sourcePropButNotInTargetPropSet,
            targetPropButNotInSourcePropSet,
            superModelsTargetIntersectPropSet,
            additionalDslRefsInSourceSuperclasses,
            sourceVarNamePostfix,
            targetVarNamePostfix
        )
    }
}
