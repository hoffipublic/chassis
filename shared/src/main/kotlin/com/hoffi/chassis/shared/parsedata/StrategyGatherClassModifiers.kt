package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.shared.GatherPropertys

object StrategyClassModifiers {
    enum class STRATEGY { UNION, SPECIAL_WINS, GENERAL_WINS }

    fun resolve(
        strategy: StrategyGatherProperties.STRATEGY,
        dslRef: DslRef.ISubElementLevel,
        sharedGatheredGatherPropertys: SharedGatheredClassModifiers
    ): Set<GatherPropertys> {
        return when (strategy) {
            StrategyGatherProperties.STRATEGY.UNION -> StrategyGatherProperties.union(
                dslRef,
                sharedGatheredGatherPropertys
            )
            StrategyGatherProperties.STRATEGY.SPECIAL_WINS -> StrategyGatherProperties.specialWins(
                dslRef,
                sharedGatheredGatherPropertys
            )
        }
    }

}
