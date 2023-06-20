package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.dsl.internal.DslBlockOn
import com.squareup.kotlinpoet.KModifier

// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===
@ChassisDslMarker
interface IDslApiClassModifiers {
    fun classModifiers(vararg modifiers: KModifier)
    operator fun KModifier.unaryPlus()

}

// === Impl Interfaces (extend IDslApi's plus methods and props that should not be visible from the DSL ===
interface IDslImplClassModifiers : IDslApiClassModifiers {
    val classModifiers: MutableSet<KModifier>
}


class DslClassModifiersImpl() : IDslImplClassModifiers {
    override fun toString() = classModifiers.joinToString()
    override val classModifiers = mutableSetOf<KModifier>()
    @DslBlockOn(DslModel::class, DslDto::class, DslTable::class)
    override fun classModifiers(vararg modifiers: KModifier) { classModifiers.addAll(modifiers) }
    @DslBlockOn(DslModel::class, DslDto::class, DslTable::class)
    override operator fun KModifier.unaryPlus() { classModifiers.add(this) }
}
