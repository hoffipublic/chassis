package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.shared.GatherPropertys
import com.hoffi.chassis.shared.whens.WhensDslRef

class CollectedGatherPropertys(val dslRefOfBasemodelWithoutAllFromSubelements: DslRef.IElementLevel, val dslRunIdentifier: String) {
    override fun toString() = "${this::class.simpleName}($dslRefOfBasemodelWithoutAllFromSubelements, $dslRunIdentifier)"
    val allFromGroup: MutableSet<GatherPropertys> = mutableSetOf()
    val allFromElement: MutableSet<GatherPropertys> = mutableSetOf()
    val allFromSubelements: MutableMap<DslRef.ISubElementLevel, MutableMap<String, MutableSet<GatherPropertys>>> = mutableMapOf()
    fun copyForNewSubelement() = CollectedGatherPropertys(dslRefOfBasemodelWithoutAllFromSubelements, dslRunIdentifier).also {
        it.allFromGroup.addAll(allFromGroup.map { el -> el.copyDeep() })
        it.allFromElement.addAll(allFromElement.map { el -> el.copyDeep() })
        it.allFromSubelements.putAll(allFromSubelements.map { outer -> outer.key to outer.value.map {
            inner -> inner.key to inner.value.map {
                el -> el.copyDeep() }.toMutableSet() }.toMap().toMutableMap() }) // should be empty here
    }
}

object StrategyGatherProperties {
    enum class STRATEGY { UNION, SPECIAL_WINS }

    fun resolve(
        strategy: STRATEGY,
        ownModelSubelementRef: DslRef.ISubElementLevel,
        collectedGatherPropertys: CollectedGatherPropertys
    ): Set<GatherPropertys> {
        val result: Set<GatherPropertys> = when (strategy) {
            STRATEGY.UNION -> union(ownModelSubelementRef, collectedGatherPropertys)
            STRATEGY.SPECIAL_WINS -> specialWins(ownModelSubelementRef, collectedGatherPropertys)
        }
        for (gatherPropertys in result) {
            val modelOrModelSubelementRefOriginal = gatherPropertys.modelOrModelSubelementRefOriginal
            val expandedRef = WhensDslRef.expandRefToSubelement(modelOrModelSubelementRefOriginal, ownModelSubelementRef)
            if (expandedRef != modelOrModelSubelementRefOriginal) gatherPropertys.modelSubelementRefExpanded = expandedRef
        }
        return result
    }

    private fun union(dslRef: DslRef.ISubElementLevel, collectedGatherPropertys: CollectedGatherPropertys): Set<GatherPropertys> {
        val set = mutableSetOf<GatherPropertys>()
        with(collectedGatherPropertys) {
            set.addAll(allFromGroup)
            set.addAll(allFromElement)
            set.addAll(allFromSubelements[dslRef]?.get(dslRef.simpleName) ?: emptySet())
        }
        return set
    }

    private fun specialWins(dslRef: DslRef.ISubElementLevel, collectedGatherPropertys: CollectedGatherPropertys): Set<GatherPropertys> {
        val set = mutableSetOf<GatherPropertys>()
        with(collectedGatherPropertys) {
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
