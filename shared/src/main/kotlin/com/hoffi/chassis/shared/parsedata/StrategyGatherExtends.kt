package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.shared.Extends

class SharedGatheredExtends(val dslRef: DslRef.IElementLevel, val dslRunIdentifier: String) {
    override fun toString() = "${this::class.simpleName}($dslRef, $dslRunIdentifier)"
    val allFromGroup: MutableMap<String, Extends> = mutableMapOf()
    val allFromElement: MutableMap<String, Extends> = mutableMapOf()
    val allFromSubelements: MutableMap<DslRef.ISubElementLevel, MutableMap<String, MutableMap<String, Extends>>> = mutableMapOf()
}

object StrategyGatherExtends {
    enum class STRATEGY { UNION, SPECIAL_WINS, GENERAL_WINS }

    fun resolve(
        strategy: StrategyGatherExtends.STRATEGY,
        dslRef: DslRef.ISubElementLevel,
        sharedGatheredExtends: SharedGatheredExtends
    ): Set<Extends> {
        return when (strategy) {
            STRATEGY.UNION -> union(dslRef, sharedGatheredExtends)
            STRATEGY.SPECIAL_WINS -> specialWins(dslRef, sharedGatheredExtends)
            STRATEGY.GENERAL_WINS -> generalWins(dslRef, sharedGatheredExtends)
        }
    }

    private fun union(dslRef: DslRef.ISubElementLevel, sharedGatheredExtends: SharedGatheredExtends): Set<Extends> {
        val set = mutableSetOf<Extends>()
        with(sharedGatheredExtends) {
            set.addAll(allFromGroup.values)
            set.addAll(allFromElement.values)
            set.addAll(allFromSubelements[dslRef]?.get(dslRef.simpleName)?.values ?: emptySet())
        }
        return set
    }

    private fun specialWins(dslRef: DslRef.ISubElementLevel, sharedGatheredExtends: SharedGatheredExtends): Set<Extends> {
        val set = mutableSetOf<Extends>()
        with(sharedGatheredExtends) {
            if (allFromSubelements[dslRef]?.get(dslRef.simpleName)?.isNotEmpty() ?: false) {
                set.addAll(allFromSubelements[dslRef]?.get(dslRef.simpleName)?.values ?: emptySet())
            } else if(allFromElement.isNotEmpty()) {
                set.addAll(allFromElement.values)
            } else {
                set.addAll(allFromGroup.values)
            }
        }
        return set
    }

    private fun generalWins(dslRef: DslRef.ISubElementLevel, sharedGatheredExtends: SharedGatheredExtends): Set<Extends> {
        val set = mutableSetOf<Extends>()
        with(sharedGatheredExtends) {
            if (allFromGroup.isNotEmpty()) {
                set.addAll(allFromGroup.values)
            } else if (allFromElement.isNotEmpty()) {
                set.addAll(allFromElement.values)
            } else {
                set.addAll(allFromSubelements[dslRef]?.get(dslRef.simpleName)?.values ?: emptySet())
            }
        }
        return set
    }
}
