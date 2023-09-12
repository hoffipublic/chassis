package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.shared.dsl.DslRef
import com.squareup.kotlinpoet.KModifier

class CollectedClassModifiers(val dslRefOfBasemodelWithoutAllFromSubelements: DslRef.IElementLevel, val dslRunIdentifier: String) {
    override fun toString() = "${this::class.simpleName}($dslRefOfBasemodelWithoutAllFromSubelements, $dslRunIdentifier)"
    val allFromGroup: MutableSet<KModifier> = mutableSetOf()
    val allFromElement: MutableSet<KModifier> = mutableSetOf()
    val allFromSubelements: MutableMap<DslRef.ISubElementLevel, MutableMap<String, MutableSet<KModifier>>> = mutableMapOf()
    fun copyForNewSubelement() = CollectedClassModifiers(dslRefOfBasemodelWithoutAllFromSubelements, dslRunIdentifier).also {
        it.allFromGroup.addAll(allFromGroup)
        it.allFromElement.addAll(allFromElement)
        it.allFromSubelements.putAll(allFromSubelements.map { outer -> outer.key to outer.value.map { sn ->
            sn.key to sn.value.map { inner -> inner }.toMutableSet() }.toMap().toMutableMap() }) // should be empty here
    }
}

object StrategyGatherClassModifiers {
    enum class STRATEGY { UNION, SPECIAL_WINS, GENERAL_WINS }

    fun resolve(
        strategy: StrategyGatherClassModifiers.STRATEGY,
        dslRef: DslRef.ISubElementLevel,
        collectedClassModifiers: CollectedClassModifiers
    ): Set<KModifier> {
        return when (strategy) {
            STRATEGY.UNION -> union(dslRef, collectedClassModifiers)
            STRATEGY.SPECIAL_WINS -> specialWins(dslRef, collectedClassModifiers)
            STRATEGY.GENERAL_WINS -> generalWins(dslRef, collectedClassModifiers)
        }
    }

    private fun union(dslRef: DslRef.ISubElementLevel, collectedClassModifiers: CollectedClassModifiers): Set<KModifier> {
        val set = mutableSetOf<KModifier>()
        with(collectedClassModifiers) {
            set.addAll(allFromGroup)
            set.addAll(allFromElement)
            set.addAll(allFromSubelements[dslRef]?.get(dslRef.simpleName) ?: emptySet())
        }
        return set
    }

    private fun specialWins(dslRef: DslRef.ISubElementLevel, collectedClassModifiers: CollectedClassModifiers): Set<KModifier> {
        val set = mutableSetOf<KModifier>()
        with(collectedClassModifiers) {
            if (allFromSubelements[dslRef]?.get(dslRef.simpleName)?.isNotEmpty() ?: false) {
                set.addAll(allFromSubelements[dslRef]?.get(dslRef.simpleName) ?: emptySet())
            } else if(allFromElement.isNotEmpty()) {
                set.addAll(allFromElement)
            } else {
                set.addAll(allFromGroup)
            }
        }
        return set
    }

    private fun generalWins(dslRef: DslRef.ISubElementLevel, collectedClassModifiers: CollectedClassModifiers): Set<KModifier> {
        val set = mutableSetOf<KModifier>()
        with(collectedClassModifiers) {
            if (allFromGroup.isNotEmpty()) {
                set.addAll(allFromGroup)
            } else if (allFromElement.isNotEmpty()) {
                set.addAll(allFromElement)
            } else {
                set.addAll(allFromSubelements[dslRef]?.get(dslRef.simpleName) ?: emptySet())
            }
        }
        return set
    }
}
