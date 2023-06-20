package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslCtxException
import com.hoffi.chassis.dsl.whereto.DslNameAndWheretoWithSubelementsDelegateImpl
import com.hoffi.chassis.dsl.whereto.IDslApiNameAndWheretoWithSubelements
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.dsl.DslRef

/** a DslRun starts parsing multiple DSLs into the same DslCtx */
class DslRun(var runIdentifierEgEnvAndTime: String) : IDslClass {
    override fun toString() = "DslRun(runIdentifierEgEnvAndTime='$runIdentifierEgEnvAndTime')"
    var running = false
    val dslRun = this
    internal val dslCtx: DslCtx = DslCtx._create(this)
        .also {  globalDslCtx = it }
    val dslCtxWrapper by lazy { DslCtxWrapper(dslCtx, DslDiscriminator("run: $runIdentifierEgEnvAndTime")) }

    val runRef = DslRef.DslRun(runIdentifierEgEnvAndTime)
    override val selfDslRef: DslRef = runRef

    fun start(dslRunDiscriminator: String, dslRunBlock: DslCtxWrapper.() -> Unit): DslRun {
        dslCtxWrapper.dslDiscriminator = DslDiscriminator(dslRunDiscriminator)
        return start(dslRunBlock)
    }
    fun start(dslRunBlock: DslCtxWrapper.() -> Unit): DslRun {
        if (running) throw DslCtxException("DslRun is already running! $this ")
        dslCtx.start()
        running = true
        while (true) {
            dslCtxWrapper.apply(dslRunBlock)

            val nextPass: DSLPASS? = dslCtx.currentPASS.nextPass()
            if (nextPass != null) {
                if (nextPass !is DSLPASS.PASS_FINISH) {
                    dslCtx.currentPASS = nextPass
                } else {
                    if (dslCtx.errors.isEmpty()) {
                        dslCtx.currentPASS = nextPass
                    } else {
                        dslCtx.currentPASS = dslCtx.PASS_ERROR
                    }
                }
            } else {
                dslCtx.currentPASS.finish() // execEnd Time of last PASS (predecessors have been finished by call to nextPass()
                break
            }
        }
        return this
    }

    @DslInstance
    internal val wheretoImpl: DslNameAndWheretoWithSubelementsDelegateImpl = with(dslCtxWrapper) {
        dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto("<DslRun>", runRef))
    }

    @DslBlockOn(DslNameAndWheretoWithSubelementsDelegateImpl::class)
    fun configure(whereToBlock: IDslApiNameAndWheretoWithSubelements.() -> Unit) {
        dslCtx.currentPASS = dslCtx.PASS_0_CONFIGURE // special PASS_0 !
        wheretoImpl.apply(whereToBlock)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DslRun) return false
        if (!super.equals(other)) return false
        return runIdentifierEgEnvAndTime == other.runIdentifierEgEnvAndTime
    }
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + runIdentifierEgEnvAndTime.hashCode()
        return result
    }
    companion object {
        val NULL = DslRun(C.NULLSTRING)
    }
}
