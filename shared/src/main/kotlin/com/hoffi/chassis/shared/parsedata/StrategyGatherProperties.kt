package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.shared.GatherPropertys

class SharedGatheredGatherPropertys(val dslRef: DslRef.IElementLevel, val dslRunIdentifier: String) {
    override fun toString() = "${this::class.simpleName}($dslRef, $dslRunIdentifier)"
    val allFromGroup: MutableSet<GatherPropertys> = mutableSetOf()
    val allFromElement: MutableSet<GatherPropertys> = mutableSetOf()
    val allFromSubelements: MutableMap<DslRef.ISubElementLevel, MutableMap<String, MutableSet<GatherPropertys>>> = mutableMapOf()
}

object StrategyGatherProperties {
    enum class STRATEGY { UNION, SPECIAL_WINS }

    fun resolve(
        strategy: STRATEGY,
        dslRef: DslRef.ISubElementLevel,
        sharedGatheredGatherPropertys: SharedGatheredGatherPropertys
    ): Set<GatherPropertys> {
        return when (strategy) {
            STRATEGY.UNION -> union(dslRef, sharedGatheredGatherPropertys)
            STRATEGY.SPECIAL_WINS -> specialWins(dslRef, sharedGatheredGatherPropertys)
        }
    }

    private fun union(dslRef: DslRef.ISubElementLevel, sharedGatheredGatherPropertys: SharedGatheredGatherPropertys): Set<GatherPropertys> {
        val set = mutableSetOf<GatherPropertys>()
        with(sharedGatheredGatherPropertys) {
            set.addAll(allFromGroup)
            set.addAll(allFromElement)
            set.addAll(allFromSubelements[dslRef]?.get(dslRef.simpleName) ?: emptySet())
        }
        return set
    }

    private fun specialWins(dslRef: DslRef.ISubElementLevel, sharedGatheredGatherPropertys: SharedGatheredGatherPropertys): Set<GatherPropertys> {
        val set = mutableSetOf<GatherPropertys>()
        with(sharedGatheredGatherPropertys) {
            if (allFromSubelements[dslRef]?.get(dslRef.simpleName)?.isNotEmpty() ?: false) {
                set.addAll(allFromSubelements[dslRef]?.get(dslRef.simpleName) ?: emptySet())
            } else if(allFromElement.isNotEmpty()) {
                set.addAll(allFromElement)
            } else {
                allFromGroup
            }
        }
        return set
    }
}
