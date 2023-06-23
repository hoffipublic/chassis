package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.shared.dsl.DslRef
import com.squareup.kotlinpoet.KModifier

class SharedGatheredClassModifiers(val dslRef: DslRef.IElementLevel, val dslRunIdentifier: String) {
    override fun toString() = "${this::class.simpleName}($dslRef, $dslRunIdentifier)"
    val allFromGroup: MutableSet<KModifier> = mutableSetOf()
    val allFromElement: MutableSet<KModifier> = mutableSetOf()
    val allFromSubelement: MutableMap<String, MutableSet<KModifier>> = mutableMapOf()
}

object StrategyGatherClassModifiers {
    enum class STRATEGY { UNION, SPECIAL_WINS, GENERAL_WINS }

    fun resolve(
        strategy: StrategyGatherClassModifiers.STRATEGY,
        dslRef: DslRef.ISubElementLevel,
        sharedGatheredClassModifiers: SharedGatheredClassModifiers
    ): Set<KModifier> {
        return when (strategy) {
            STRATEGY.UNION -> union(dslRef, sharedGatheredClassModifiers)
            STRATEGY.SPECIAL_WINS -> specialWins(dslRef, sharedGatheredClassModifiers)
            STRATEGY.GENERAL_WINS -> generalWins(dslRef, sharedGatheredClassModifiers)
        }
    }

    private fun union(dslRef: DslRef.ISubElementLevel, sharedGatheredClassModifiers: SharedGatheredClassModifiers): Set<KModifier> {
        val set = mutableSetOf<KModifier>()
        with(sharedGatheredClassModifiers) {
            set.addAll(allFromGroup)
            set.addAll(allFromElement)
            set.addAll(allFromSubelement[dslRef.simpleName] ?: emptySet())
        }
        return set
    }

    private fun specialWins(dslRef: DslRef.ISubElementLevel, sharedGatheredClassModifiers: SharedGatheredClassModifiers): Set<KModifier> {
        val set = mutableSetOf<KModifier>()
        with(sharedGatheredClassModifiers) {
            if (allFromSubelement[dslRef.simpleName]?.isNotEmpty() ?: false) {
                set.addAll(allFromSubelement[dslRef.simpleName]!!)
            } else if(allFromElement.isNotEmpty()) {
                set.addAll(allFromElement)
            } else {
                set.addAll(allFromGroup)
            }
        }
        return set
    }

    private fun generalWins(dslRef: DslRef.ISubElementLevel, sharedGatheredClassModifiers: SharedGatheredClassModifiers): Set<KModifier> {
        val set = mutableSetOf<KModifier>()
        with(sharedGatheredClassModifiers) {
            if (allFromGroup.isNotEmpty()) {
                set.addAll(allFromGroup)
            } else if (allFromElement.isNotEmpty()) {
                set.addAll(allFromElement)
            } else {
                set.addAll(allFromSubelement[dslRef.simpleName] ?: emptySet())
            }
        }
        return set
    }
}
