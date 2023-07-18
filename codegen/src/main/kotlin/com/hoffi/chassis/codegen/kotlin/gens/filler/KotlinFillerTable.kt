package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.gens.AKotlinClass
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.squareup.kotlinpoet.FunSpec

context(GenCtxWrapper)
class KotlinFillerTable(fillerData: FillerData): AKotlinFiller(fillerData, MODELREFENUM.TABLE) {

    override fun build(fillerData: FillerData) {
        currentBuildFillerData = fillerData
        if (alreadyCreated.contains(fillerData.sourceDslRef)) return
        else alreadyCreated.add(fillerData.sourceDslRef)
        val sourceGenModel: GenModel = genCtx.genModel(fillerData.sourceDslRef)
        val sourceKotlinClass: AKotlinClass = kotlinGenCtx.kotlinGenClass(fillerData.sourceDslRef)
        val intersectPropsData = IntersectPropertys.intersectPropsOf(targetGenModel, sourceGenModel, sourceKotlinClass, "", "")

        if (currentBuildFillerData.targetDslRef !is DslRef.table) {
            createFromTable(intersectPropsData)
        }
    }

    private fun createFromTable(i: IntersectPropertys.CommonPropData) {
        val funSpec = FunSpec.builder(targetGenModel.asVarName)
            .addParameter(i.sourceVarName, i.sourceGenModel.poetType)
            .returns(targetGenModel.poetType)

        builder.addFunction(funSpec.build())
    }
}
