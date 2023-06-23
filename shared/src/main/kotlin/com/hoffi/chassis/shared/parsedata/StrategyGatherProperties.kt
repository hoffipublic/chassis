package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.shared.dsl.DslRef

object StrategyGatherProperties {
    enum class STRATEGY { SPECIAL_WINS }

    fun resolve(strategy: STRATEGY, dslRef: DslRef.ISubElementLevel) {
        return when (strategy) {
            STRATEGY.SPECIAL_WINS -> specialWins(dslRef)
        }
    }

    private fun specialWins(dslRef: DslRef.ISubElementLevel) {
    }
}
