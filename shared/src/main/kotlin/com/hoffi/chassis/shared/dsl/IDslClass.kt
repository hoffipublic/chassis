package com.hoffi.chassis.shared.dsl

import com.hoffi.chassis.chassismodel.C

@JvmInline
value class DslDiscriminator(val dslDiscriminator: String) { companion object { val NULL = DslDiscriminator(C.NULLSTRING) }
    override fun toString() = dslDiscriminator }
class DslDiscriminatorWrapper(val dslDiscriminator: DslDiscriminator) {
    override fun toString() = "$dslDiscriminator" }

interface IDslParticipator
interface IDslClass : IDslParticipator {
    val parent: IDslClass
    val selfDslRef: IDslRef
    val parentDslRef: IDslRef
    val groupDslRef: DslRef.IGroupLevel
    companion object {
        object NULL : IDslClass {
            override val parent = NULL
            override val selfDslRef = DslRef.NULL
            override val parentDslRef = DslRef.NULL
            override val groupDslRef = DslRef.IGroupLevel.NULL
        }
    }
}
interface IDelegatee : IDslParticipator
