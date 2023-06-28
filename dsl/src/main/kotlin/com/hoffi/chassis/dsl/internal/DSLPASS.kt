package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import org.slf4j.LoggerFactory
import kotlin.time.DurationUnit
import kotlin.time.toDuration

sealed class DSLPASS(val index: Int, protected val nextDSLPASS: DSLPASS?, protected val dslCtx: DslCtx) {
    override fun toString() = this::class.simpleName!!
    private val log = LoggerFactory.getLogger(javaClass)
    var execStart: Long = 0L
    var execEnd: Long = -1L
    var execTimes: MutableMap<String, Pair<Long, Long>> = mutableMapOf()
    fun start(): DSLPASS {
        execStart = System.currentTimeMillis()
        log.info("||--> started {}", this::class.simpleName)
        return this
    }
    fun nextPass(): DSLPASS? {
        return nextDSLPASS
    }
    fun finish() {
        execEnd = System.currentTimeMillis()
        if (dslCtx.topLevelDslFunctionName == C.NULLSTRING) throw DslException("Forgot to set dslCtx.topLevelDslFunctionName (currently in PASS ${this::class.simpleName})") // TODO can this happen at all???
        if (execTimes.containsKey(dslCtx.topLevelDslFunctionName)) throw DslException("${this::class.simpleName} execTimes already contains a startTime/endTime for ${dslCtx.topLevelDslFunctionName}. You probably forgot to set topLevelDslFunctionName as first line of the next(!) topLevel DSL function called in closure of DslRun.start { } -> Set it with something like: dslCtx.topLevelDslFunctionName = object{}.javaClass.enclosingMethod.name")
        execTimes[dslCtx.topLevelDslFunctionName] = Pair(execStart, execEnd)
        log.info("||<-- finished {} of {}() (which was last in its dslRun.start{ } closure) in {}", this::class.simpleName, dslCtx.topLevelDslFunctionName, (execEnd - execStart).toDuration(DurationUnit.MILLISECONDS))
    }

    companion object {
        val NULL: DSLPASS = PASS_ERROR(DslCtx._create(DslRun(C.NULLSTRING)))
    }
    internal fun doIf(pass: DSLPASS, vararg passes: DSLPASS, block: () -> Unit) {
        if (dslCtx.currentPASS == pass || dslCtx.currentPASS in passes) block()
    }

    //    internal fun <T: DSLPASS> after(dslCtx: DslCtx, pass: KClass<T>, block: () -> Unit) {
//        if (dslCtx.pass.index > pass.index) {}
//    }
    class PASS_0_CONFIGURE  (nextPASS: DSLPASS, dslCtx: DslCtx) : DSLPASS(1, nextPASS, dslCtx) /* DslRun configure */
    class PASS_1_BASEMODELS (nextPASS: DSLPASS, dslCtx: DslCtx) : DSLPASS(1, nextPASS, dslCtx) /* normal models like DTOs */
    class PASS_2_TABLEMODELS(nextPASS: DSLPASS, dslCtx: DslCtx) : DSLPASS(2, nextPASS, dslCtx) // TABLE models (probably gathering propertys and stuff from above models)
    class PASS_3_ALLMODELS  (nextPASS: DSLPASS, dslCtx: DslCtx) : DSLPASS(3, nextPASS, dslCtx) // allModels { } at the end of DSLPASS.TWO_TABLEMODELS
    class PASS_4_REFERENCING(nextPASS: DSLPASS, dslCtx: DslCtx) : DSLPASS(4, nextPASS, dslCtx) // things that potentially reference other Models, like extends { } clauses
    class PASS_FINISH       (nextPASS: DSLPASS, dslCtx: DslCtx) : DSLPASS(5, nextPASS, dslCtx) // create Gen Models and cleanup for specific models
    class PASS_GENMODELSCREATED  (dslCtx: DslCtx) : DSLPASS(6, nextDSLPASS = null, dslCtx) // things after finish, e.g. gather propertiesOf
    class PASS_ERROR        (                   dslCtx: DslCtx) : DSLPASS(99, nextDSLPASS = null, dslCtx)

    //region equals and hashCode ...
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DSLPASS) return false

        if (index != other.index) return false
        if (nextDSLPASS != other.nextDSLPASS) return false
        if (dslCtx != other.dslCtx) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + (nextDSLPASS?.hashCode() ?: 0)
        result = 31 * result + dslCtx.hashCode()
        return result
    }
    //endregion
}
