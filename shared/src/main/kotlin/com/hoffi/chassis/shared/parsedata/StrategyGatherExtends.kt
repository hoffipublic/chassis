package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType.Companion.NOTHING
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.shared.Extends

class CollectedExtends(val dslRefOfBasemodelWithoutAllFromSubelements: DslRef.IElementLevel, val dslRunIdentifier: String) {
    override fun toString() = "${this::class.simpleName}($dslRefOfBasemodelWithoutAllFromSubelements, $dslRunIdentifier)"
    val allFromGroup: MutableMap<String, Extends> = mutableMapOf()
    val allFromElement: MutableMap<String, Extends> = mutableMapOf()
    val allFromSubelements: MutableMap<DslRef.ISubElementLevel, MutableMap<String, Extends>> = mutableMapOf()
    fun copyForNewSubelement() = CollectedExtends(dslRefOfBasemodelWithoutAllFromSubelements, dslRunIdentifier).also {
        it.allFromGroup.putAll(allFromGroup.map { el -> el.key to el.value.copyDeepForDsl() })
        it.allFromElement.putAll(allFromElement.map { el -> el.key to el.value.copyDeepForDsl() })
        it.allFromSubelements.putAll(allFromSubelements.map { outer -> outer.key to outer.value.map { inner -> inner.key to inner.value.copyDeepForDsl() }.toMap().toMutableMap() }) // should be empty here
    }
}

object StrategyGatherExtends {
    enum class STRATEGY { DEFAULT, UNION, SPECIAL_WINS, GENERAL_WINS, SPECIAL_CLASS_WINS_UNION_OF_INTERFACES }

    fun resolve(
        strategy: StrategyGatherExtends.STRATEGY,
        ownModelSubelementRef: DslRef.ISubElementLevel,
        collectedExtends: CollectedExtends
    ): MutableMap<String, Extends> {
        val result: MutableMap<String, Extends> = when (strategy) {
            STRATEGY.DEFAULT, STRATEGY.SPECIAL_CLASS_WINS_UNION_OF_INTERFACES -> union(ownModelSubelementRef, collectedExtends, specialWinsForClass = true)
            STRATEGY.UNION -> union(ownModelSubelementRef, collectedExtends, specialWinsForClass = false)
            STRATEGY.SPECIAL_WINS -> specialWins(ownModelSubelementRef, collectedExtends)
            STRATEGY.GENERAL_WINS -> generalWins(ownModelSubelementRef, collectedExtends)
        }
        for (extend in result.values) {
            EitherTypOrModelOrPoetType.expandReffedEitherToSubelementIfModel(extend.typeClassOrDslRef, ownModelSubelementRef)
            for (superInterface in extend.superInterfaces) {
                EitherTypOrModelOrPoetType.expandReffedEitherToSubelementIfModel(superInterface, ownModelSubelementRef)
            }
        }
        return result
    }

    private fun union(ownModelSubelementRef: DslRef.ISubElementLevel, collectedExtends: CollectedExtends, specialWinsForClass: Boolean): MutableMap<String, Extends> {
        val result = mutableMapOf<String, Extends>()
        with(collectedExtends) {
            result.putAll(allFromGroup.map { it.key to it.value.copy() }) // we might alter the Extends content of group/element in EACH subelement
            for (extends in allFromElement.values) {
                putUnionInResultExtend(result, extends.copy(), ownModelSubelementRef, specialWinsForClass)
            }
            for (extends in allFromSubelements[ownModelSubelementRef]?.values ?: emptySet()) {
                putUnionInResultExtend(result, extends.copy(), ownModelSubelementRef, specialWinsForClass)
            }
        }
        return result
    }

    private fun specialWins(ownModelSubelementRef: DslRef.ISubElementLevel, collectedExtends: CollectedExtends): MutableMap<String, Extends> {
        val result: MutableMap<String, Extends> = mutableMapOf()
        with(collectedExtends) {
            result.putAll(allFromSubelements[ownModelSubelementRef] ?: emptyMap())
            for (extends in allFromElement.values) {
                result.putIfAbsent(extends.simpleName, extends.copy())
            }
            for (extends in allFromGroup.values) {
                result.putIfAbsent(extends.simpleName, extends.copy())
            }
        }
        return result
    }

    private fun generalWins(ownModelSubelementRef: DslRef.ISubElementLevel, collectedExtends: CollectedExtends): MutableMap<String, Extends> {
        val result: MutableMap<String, Extends> = mutableMapOf()
        with(collectedExtends) {
            result.putAll(allFromGroup)
            for (extends in allFromElement.values) {
                result.putIfAbsent(extends.simpleName, extends.copy())
            }
            for (extends in allFromSubelements[ownModelSubelementRef]?.values ?: emptyList())
                result.putIfAbsent(extends.simpleName, extends.copy())
        }
        return result
    }

    private fun putUnionInResultExtend(
        result: MutableMap<String, Extends>,
        extends: Extends,
        ownModelSubelementRef: DslRef.ISubElementLevel,
        specialWinsForClass: Boolean
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
                if (!specialWinsForClass) {
                    throw DslException("${this::class.simpleName} for $ownModelSubelementRef already extends $resultExtends in group")
                }
            } else {
                //if (extends.superclassHasBeenSet) {
                    resultExtends.typeClassOrDslRef = extends.typeClassOrDslRef
                    resultExtends.superclassHasBeenSet = true
                //}
            }
            if (extends.replaceSuperInterfaces) {
                resultExtends.replaceSuperInterfaces = true
                resultExtends.superInterfaces.clear()
            }
            resultExtends.superInterfaces.addAll(extends.superInterfaces)
        }
    }
}
