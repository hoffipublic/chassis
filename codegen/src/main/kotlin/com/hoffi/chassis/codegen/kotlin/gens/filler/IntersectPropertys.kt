package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.codegen.kotlin.gens.AKotlinClass
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.whens.WhensDslRef

object IntersectPropertys {
    data class CommonPropData(
        val sourceGenModel: GenModel,
        val sourceKotlinClass: AKotlinClass,
        val intersectPropSet: Set<Property>,
        val fromPropButNotToPropSet: Set<Property>,
        val targetPropButNotFromPropSet: Set<Property>,
        val superModelsTargetIntersectPropSet: Set<Property>,
        val additionalDslRefsInSourceSuperclasses: Set<IDslRef>,
        val sourceVarNamePostfix: String,
        val targetVarNamePostfix: String,
    ) {
        val sourceVarName = WhensDslRef.whenModelSubelement(sourceGenModel.modelSubElRef,
            isDtoRef = { "source$sourceVarNamePostfix" },
            isTableRef = { "resultRow$sourceVarNamePostfix" },
        )
        val targetVarName = WhensDslRef.whenModelSubelement(sourceGenModel.modelSubElRef,
            isDtoRef = { "target$sourceVarNamePostfix" },
            isTableRef = { "resultRow$sourceVarNamePostfix" },
        )
    }

    fun intersectPropsOf(targetGenModel: GenModel, sourceGenModel: GenModel, sourceKotlinClass: AKotlinClass, sourceVarNamePostfix: String = "", targetVarNamePostfix: String = ""): CommonPropData {

        val intersectPropSet = targetGenModel.allProps.values.intersect(sourceGenModel.allProps.values)
        val sourcePropButNotInTargetPropSet = sourceGenModel.allProps.values.toMutableSet().also { it.removeAll(intersectPropSet) }
        val targetPropButNotInSourcePropSet = targetGenModel.allProps.values.toMutableSet().also { it.removeAll(intersectPropSet) }
        val superModelsTargetIntersectPropSet = targetGenModel.superclassProps.values.intersect(targetPropButNotInSourcePropSet + sourceGenModel.superclassProps.values)
        val additionalDslRefsInSourceSuperclasses: Set<DslRef.IModelSubelement> = superModelsTargetIntersectPropSet.map { it.containedInSubelementRef }.toSet()

        return CommonPropData(
            sourceGenModel,
            sourceKotlinClass,
            intersectPropSet,
            sourcePropButNotInTargetPropSet,
            targetPropButNotInSourcePropSet,
            superModelsTargetIntersectPropSet,
            additionalDslRefsInSourceSuperclasses,
            sourceVarNamePostfix,
            targetVarNamePostfix
        )
    }
}
