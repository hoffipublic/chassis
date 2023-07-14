package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.gens.AKotlinClass
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.squareup.kotlinpoet.ClassName

context(GenCtxWrapper)
class KotlinFillerTable(fillerData: FillerData): AKotlinFiller(fillerData, MODELREFENUM.TABLE) {

    override fun build(fillerData: FillerData) {
        if (alreadyCreated.contains(fillerData.sourceDslRef)) return
        else alreadyCreated.add(fillerData.sourceDslRef)
        val fromGenModel: GenModel = genCtx.genModel(fillerData.sourceDslRef)
        val fromKotlinClass: AKotlinClass = kotlinGenCtx.kotlinGenClass(fillerData.sourceDslRef)
        val fromVarNamePostfix = (fromGenModel.poetType as ClassName).simpleName
        log.info("${this::class.simpleName}.build() not yet implemented")
    }
}
