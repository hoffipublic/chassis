package com.hoffi.chassis.shared.dsl

@JvmInline
value class DslDiscriminator(val dslDiscriminator: String)

interface IDslClass {
    val parent: IDslClass
    val selfDslRef: DslRef
    val parentDslRef: DslRef
    val groupDslRef: DslRef.DslGroupRefEither
}
