package com.hoffi.chassis.dsl.whereto

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.*
import com.hoffi.chassis.dsl.modelgroup.DslDto
import com.hoffi.chassis.dsl.modelgroup.DslModel
import com.hoffi.chassis.dsl.modelgroup.DslModelgroup
import com.hoffi.chassis.dsl.modelgroup.DslTable
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.helpers.pathSepRE
import okio.Path
import okio.Path.Companion.toPath
import org.slf4j.LoggerFactory

// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===

interface IDslApiNameAndWheretoProps {
    var baseDir: String
        get() = baseDirPath.toString()
        set(value) { baseDirPath = value.toPath()}
    var baseDirPath: Path
    fun baseDir(concat: String)
    fun baseDir(concat: Path)

    var classPrefix: String
    fun classPrefix(concat: String)
    fun classPrefixBefore(concat: String)
    fun classPrefixAfter(concat: String)
    var classPostfix: String
    fun classPostfix(concat: String)
    fun classPostfixBefore(concat: String)
    fun classPostfixAfter(concat: String)

    var packageName: String
    fun packageName(concat: String)
    fun packagePrefixBefore(concat: String)
    fun packagePrefixAfter(concat: String)
    fun packagePostfixBefore(concat: String)
    fun packagePostfixAfter(concat: String)
}
interface IDslApiNameAndWheretoOnly {
    //context(DslCtxWrapper)
    fun nameAndWhereto(simpleName: String = C.DEFAULT, block: IDslApiNameAndWheretoProps.() -> Unit)
}
interface IDslApiNameAndWheretoWithSubElements {
    //context(DslCtxWrapper)
    fun nameAndWhereto(simpleName: String = C.DEFAULT, block: IDslApiNameAndWheretoOnSubElements.() -> Unit)
}
interface IDslApiNameAndWheretoOnSubElements : IDslApiNameAndWheretoProps {
    //context(DslCtxWrapper)
    @DslBlockOn(DslDto::class)
    fun dtoNameAndWhereto(  simpleName: String = C.DEFAULT, block: IDslApiNameAndWheretoProps.() -> Unit)
    //context(DslCtxWrapper)
    @DslBlockOn(DslTable::class)
    fun tableNameAndWhereto(simpleName: String = C.DEFAULT, block: IDslApiNameAndWheretoProps.() -> Unit)
}

// === Impl Interfaces (extend IDslApi's plus methods and props that should not be visible from the DSL ===

interface IImplNameAndWheretoOnlyDelegate : IDslApiNameAndWheretoOnly {
    @DslInstance val nameAndWheretos: MutableMap<String, DslNameAndWheretoPropsImpl>
}
interface IDslImplNameAndWheretoWithSubElementsDelegate : IDslApiNameAndWheretoWithSubElements {
    @DslInstance val nameAndWheretos: MutableMap<String, DslNameAndWheretoOnSubElementsDelegateImpl>
    @DslInstance val dtoNameAndWheretos: MutableMap<String, DslNameAndWheretoPropsImpl>
    @DslInstance val tableNameAndWheretos: MutableMap<String, DslNameAndWheretoPropsImpl>
}
interface IDslImplNameAndWheretoOnSubElements : IDslApiNameAndWheretoOnSubElements  {
}

// === classes that actually implement IDslImpl interfaces

context(DslCtxWrapper)
@ChassisDslMarker(DslRun::class, DslModelgroup::class, DslModel::class, DslDto::class, DslTable::class) //, DslFiller::class)
open class DslNameAndWheretoPropsImpl(
    val simpleName: String = C.DEFAULT,
    val parentRef: IDslRef // that should be the NameAndWheretoDelegate
) : ADslClass(),
    IDslApiNameAndWheretoProps
{
    override val selfDslRef = DslRef.nameAndWhereto(simpleName, parentRef)

    override fun toString() = "$selfDslRef"
    override var baseDir: String
        get() = baseDirPath.toString()
        set(value) { baseDirPath = value.toPath()}
    override var baseDirPath: Path = java.io.File("./generated").absolutePath.toPath(normalize = true) // current working directory + "generated"
    internal var baseDirAddendum: Path = "".toPath(normalize = false)
    override fun baseDir(concat: String) { baseDirAddendum/=concat }
    override fun baseDir(concat: Path)   { baseDirPath/=concat }

    override var classPrefix: String = ""
    internal var classPrefixAddendum: String = ""
    override fun classPrefix(concat: String) { classPrefixBefore(concat) }
    override fun classPrefixBefore(concat: String) { classPrefixAddendum = "$concat$classPrefixAddendum" }
    override fun classPrefixAfter(concat: String)  { classPrefixAddendum = "$classPrefixAddendum$concat" }
    override var classPostfix: String = ""
    internal var classPostfixAddendum: String = ""
    override fun classPostfix(concat: String) { classPostfixAfter(concat) }
    override fun classPostfixBefore(concat: String) { classPostfixAddendum = "$concat$classPostfixAddendum" }
    override fun classPostfixAfter(concat: String)  { classPostfixAddendum = "$classPostfixAddendum$concat" }

    override var packageName: String = "generated"
    internal var packageNameAddendum: String = ""
    override fun packageName(concat: String) { packageNameAddendum = "$packageNameAddendum.${concat.replace(pathSepRE, ".")}" }
    internal var packagePrefix: String = ""
    override fun packagePrefixBefore(concat: String) { packagePrefix = "$concat$packagePrefix" }
    override fun packagePrefixAfter(concat: String)  { packagePrefix = "$packagePrefix$concat" }
    internal var packagePostfix: String = ""
    override fun packagePostfixBefore(concat: String) { packagePostfix= "$concat$packagePostfix" }
    override fun packagePostfixAfter(concat: String)  { packagePostfix= "$packagePostfix$concat" }
}

context(DslCtxWrapper)
@ChassisDslMarker
class DslNameAndWheretoOnlyDelegateImpl constructor(
    simpleNameOfParentDslBlock: String,
    parentRef: IDslRef
) : ADslDelegateClass(simpleNameOfParentDslBlock, parentRef), IImplNameAndWheretoOnlyDelegate {
    override fun toString() = "${super@DslNameAndWheretoOnlyDelegateImpl.toString()}->[${nameAndWheretos.keys.joinToString(DslRef.COUNTSEP, DslRef.COUNTSEP)}]"
    val log = LoggerFactory.getLogger(javaClass)

    override val selfDslRef = DslRef.nameAndWhereto(simpleNameOfParentDslBlock, parentRef)

    @DslInstance override val nameAndWheretos = mutableMapOf<String, DslNameAndWheretoPropsImpl>()

    //context(DslCtxWrapper)
    @DslBlockOn(DslModelgroup::class, DslModel::class, DslDto::class, DslTable::class)
    override fun nameAndWhereto(simpleName: String, block: IDslApiNameAndWheretoProps.() -> Unit) { //dslCtx.currentPASS)
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            else -> {
                val dslImpl = nameAndWheretos.getOrPut(simpleName) { DslNameAndWheretoPropsImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
        }
    }
}

context(DslCtxWrapper)
@ChassisDslMarker
class DslNameAndWheretoWithSubElementsDelegateImpl(
    simpleNameOfParentDslBlock: String,
    parentRef: IDslRef
) : ADslDelegateClass(simpleNameOfParentDslBlock, parentRef), IDslImplNameAndWheretoWithSubElementsDelegate, IDslParticipator {
    val log = LoggerFactory.getLogger(javaClass)
    override val selfDslRef = DslRef.nameAndWhereto(simpleNameOfParentDslBlock, parentRef)

    @DslInstance
    override val nameAndWheretos = mutableMapOf<String, DslNameAndWheretoOnSubElementsDelegateImpl>()
    override val dtoNameAndWheretos = mutableMapOf<String, DslNameAndWheretoPropsImpl>()
    override val tableNameAndWheretos = mutableMapOf<String, DslNameAndWheretoPropsImpl>()

    //context(DslCtxWrapper)
    @DslBlockOn(DslModelgroup::class, DslModel::class, DslRun::class)
    override fun nameAndWhereto(simpleName: String, block: IDslApiNameAndWheretoOnSubElements.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            else -> {
                val dslImpl = nameAndWheretos.getOrPut(simpleName) { DslNameAndWheretoOnSubElementsDelegateImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
        }
    }
}

context(DslCtxWrapper)
@ChassisDslMarker
class DslNameAndWheretoOnSubElementsDelegateImpl(
    simpleNameOfParentDslBlock: String,
    parentRef: IDslRef
) : DslNameAndWheretoPropsImpl(simpleNameOfParentDslBlock, parentRef), IDslImplNameAndWheretoOnSubElements {
    val log = LoggerFactory.getLogger(javaClass)
    val parentDslClass = dslCtx.ctxObj<DslNameAndWheretoWithSubElementsDelegateImpl>(parentRef)

    //context(DslCtxWrapper)
    override fun dtoNameAndWhereto(simpleName: String, block: IDslApiNameAndWheretoProps.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            else -> {
                val dslImpl = parentDslClass.dtoNameAndWheretos.getOrPut(simpleName) { DslNameAndWheretoPropsImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
        }
    }
    //context(DslCtxWrapper)
    override fun tableNameAndWhereto(simpleName: String, block: IDslApiNameAndWheretoProps.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        val simpleNameNameAndWhereto = parentDslClass.tableNameAndWheretos.getOrPut(simpleName) { DslNameAndWheretoPropsImpl(simpleName, selfDslRef) }
        simpleNameNameAndWhereto.apply(block)
    }
}
