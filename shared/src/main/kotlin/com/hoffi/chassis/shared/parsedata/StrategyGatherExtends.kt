package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType.Companion.NOTHING
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.shared.Extends

class SharedGatheredExtends(val dslRef: DslRef.IElementLevel, val dslRunIdentifier: String) {
    override fun toString() = "${this::class.simpleName}($dslRef, $dslRunIdentifier)"
    val allFromGroup: MutableMap<String, Extends> = mutableMapOf()
    val allFromElement: MutableMap<String, Extends> = mutableMapOf()
    val allFromSubelements: MutableMap<DslRef.ISubElementLevel, MutableMap<String, Extends>> = mutableMapOf()
}

object StrategyGatherExtends {
    enum class STRATEGY { UNION, SPECIAL_WINS, GENERAL_WINS }

    fun resolve(
        strategy: StrategyGatherExtends.STRATEGY,
        dslRef: DslRef.ISubElementLevel,
        sharedGatheredExtends: SharedGatheredExtends
    ): MutableMap<String, Extends> {
        return when (strategy) {
            STRATEGY.UNION -> union(dslRef, sharedGatheredExtends)
            STRATEGY.SPECIAL_WINS -> specialWins(dslRef, sharedGatheredExtends)
            STRATEGY.GENERAL_WINS -> generalWins(dslRef, sharedGatheredExtends)
        }
    }

    private fun union(dslRef: DslRef.ISubElementLevel, sharedGatheredExtends: SharedGatheredExtends): MutableMap<String, Extends> {
        val result = mutableMapOf<String, Extends>()
        with(sharedGatheredExtends) {
            result.putAll(allFromGroup.map { it.key to it.value.copy() }) // we might alter the Extends content of group/element in EACH subelement
            for (extends in allFromElement.values) {
                putInResultExtend(result, extends.copy(), dslRef)
            }
            for (extends in allFromSubelements[dslRef]?.values ?: emptySet()) {
                putInResultExtend(result, extends.copy(), dslRef)
            }
        }
        return result
    }

    private fun specialWins(dslRef: DslRef.ISubElementLevel, sharedGatheredExtends: SharedGatheredExtends): MutableMap<String, Extends> {
        val result = mutableMapOf<String, Extends>()
        with(sharedGatheredExtends) {
            result.putAll(allFromSubelements[dslRef] ?: emptyMap())
            for (extends in allFromElement.values) {
                result.putIfAbsent(extends.simpleName, extends.copy())
            }
            for (extends in allFromGroup.values) {
                result.putIfAbsent(extends.simpleName, extends.copy())
            }
        }
        return result
    }

    private fun generalWins(dslRef: DslRef.ISubElementLevel, sharedGatheredExtends: SharedGatheredExtends): MutableMap<String, Extends> {
        val result = mutableMapOf<String, Extends>()
        with(sharedGatheredExtends) {
            result.putAll(allFromGroup)
            for (extends in allFromElement.values) {
                result.putIfAbsent(extends.simpleName, extends.copy())
            }
            for (extends in allFromSubelements[dslRef]?.values ?: emptyList())
                result.putIfAbsent(extends.simpleName, extends.copy())
        }
        return result
    }

    private fun SharedGatheredExtends.putInResultExtend(
        result: MutableMap<String, Extends>,
        extends: Extends,
        dslRef: DslRef.ISubElementLevel
    ) {
        val resultExtends: Extends? = result[extends.simpleName]
        if (resultExtends == null) {
            result[extends.simpleName] = extends
        } else {
            if (extends.replaceSuperclass) {
                resultExtends.replaceSuperclass = true
                resultExtends.typeClassOrDslRef = extends.typeClassOrDslRef
                resultExtends.superclassHasBeenSet = true
            } else if (resultExtends.typeClassOrDslRef != NOTHING && extends.superclassHasBeenSet) {
                throw DslException("${this::class.simpleName} for $dslRef already extends $resultExtends in group")
            } else {
                if (extends.superclassHasBeenSet) {
                    resultExtends.typeClassOrDslRef = extends.typeClassOrDslRef
                    resultExtends.superclassHasBeenSet = true
                }
            }
            if (extends.replaceSuperInterfaces) {
                resultExtends.replaceSuperInterfaces = true
                resultExtends.superInterfaces.clear()
            }
            resultExtends.superInterfaces.addAll(extends.superInterfaces)
        }
    }
}
