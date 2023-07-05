package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.shared.GatherPropertys
import com.hoffi.chassis.shared.whens.WhensDslRef

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
        ownModelSubelementRef: DslRef.ISubElementLevel,
        sharedGatheredGatherPropertys: SharedGatheredGatherPropertys
    ): Set<GatherPropertys> {
        val result: Set<GatherPropertys> = when (strategy) {
            STRATEGY.UNION -> union(ownModelSubelementRef, sharedGatheredGatherPropertys)
            STRATEGY.SPECIAL_WINS -> specialWins(ownModelSubelementRef, sharedGatheredGatherPropertys)
        }
        for (gatherPropertys in result) {
            val modelOrModelSubelementRefOriginal = gatherPropertys.modelOrModelSubelementRefOriginal
            val expandedRef = WhensDslRef.expandRefToSubelement(modelOrModelSubelementRefOriginal, ownModelSubelementRef)
            if (expandedRef != modelOrModelSubelementRefOriginal) gatherPropertys.modelSubelementRefExpanded = expandedRef
        }
        return result
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
