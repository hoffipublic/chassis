package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.chassismodel.dsl.DslInstance
import com.hoffi.chassis.dsl.whereto.INameAndWheretoPlusModelSubtypes
import com.hoffi.chassis.dsl.whereto.NameAndWheretoPlusModelSubtypesImpl
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslClass
import com.hoffi.chassis.shared.dsl.IDslRef

class DslRun(var runIdentifierEgEnvAndTime: String) : IDslClass {
    var running = false
    val dslRun = this
    internal val dslCtx: DslCtx = DslCtx(this)

    val runTopLevelFunction = TopLevelDslFunction()

    override val parent: IDslClass = runTopLevelFunction
    override val selfDslRef: DslRef = DslRef.DslRun(runIdentifierEgEnvAndTime)
    override val parentDslRef: IDslRef = parent.selfDslRef
    override val groupDslRef: DslRef.IGroupLevel = DslRef.IGroupLevel.NULL
    override fun toString() = selfDslRef.toString()

    fun start(dslRunBlock: DslRun.() -> Unit = {}): DslRun {
        this.apply(dslRunBlock)
        return this
    }
    companion object {
        val NULL = DslRun("NULL")
    }

    @DslInstance
    internal val wheretoImpl = NameAndWheretoPlusModelSubtypesImpl(DslRef.DslRun("<DslRun>"))
    @DslBlockOn(NameAndWheretoPlusModelSubtypesImpl::class)
    fun configure(whereToBlock: INameAndWheretoPlusModelSubtypes.() -> Unit) {
        wheretoImpl.apply(whereToBlock)
    }
}
