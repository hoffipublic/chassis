package com.hoffi.chassis.dsl.whereto

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.*
import com.hoffi.chassis.dsl.modelgroup.DslDto
import com.hoffi.chassis.dsl.modelgroup.DslModel
import com.hoffi.chassis.dsl.modelgroup.DslModelgroup
import com.hoffi.chassis.dsl.modelgroup.DslTable
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.helpers.ifNotBlank
import com.hoffi.chassis.shared.helpers.pathSepRE
import com.hoffi.chassis.shared.helpers.pathSepREFirst
import com.hoffi.chassis.shared.parsedata.nameandwhereto.IDslApiSharedNameAndWheretoProps
import com.hoffi.chassis.shared.parsedata.nameandwhereto.NameAndWheretoDefaults
import com.hoffi.chassis.shared.strategies.IClassNameStrategy
import com.hoffi.chassis.shared.strategies.ITableNameStrategy
import okio.Path
import okio.Path.Companion.toPath
import org.slf4j.LoggerFactory

// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===

@ChassisDslMarker
interface IDslApiNameAndWheretoProps : IDslApiSharedNameAndWheretoProps {
    fun strategyClassName(strategyName: String) { strategyClassName = IClassNameStrategy.STRATEGY.valueOf(strategyName) }
    override var strategyClassName: IClassNameStrategy.STRATEGY
    fun strategyTableName(strategyName: String) { strategyTableName = ITableNameStrategy.STRATEGY.valueOf(strategyName) }
    override var strategyTableName: ITableNameStrategy.STRATEGY
    fun baseDirAbsolute(absolute: String)
    fun baseDirAbsolute(absolute: Path)
    fun baseDir(concat: String)
    fun baseDir(concat: Path)
    fun pathAbsolute(absolute: String)
    fun pathAbsolute(absolute: Path)
    fun path(concat: String)
    fun path(concat: Path)

    fun classPrefixAbsolute(absolute: String)
    fun classPrefix(concat: String)
    fun classPrefixBefore(concat: String)
    fun classPrefixAfter(concat: String)
    fun classPostfixAbsolute(absolute: String)
    fun classPostfix(concat: String)
    fun classPostfixBefore(concat: String)
    fun classPostfixAfter(concat: String)

    fun basePackageAbsolute(absolute: String)
    fun basePackage(concat: String)
    fun packageNameAbsolute(absolute: String)
    fun packageName(concat: String)
}
@ChassisDslMarker
interface IDslApiNameAndWheretoOnly {
    fun nameAndWhereto(simpleName: String = C.DEFAULT, block: IDslApiNameAndWheretoProps.() -> Unit)
}
@ChassisDslMarker
interface IDslApiNameAndWheretoWithSubelements {
    fun nameAndWhereto(simpleName: String = C.DEFAULT, block: IDslApiNameAndWheretoOnSubElements.() -> Unit)
}
interface IDslApiNameAndWheretoOnSubElements : IDslApiNameAndWheretoProps {
    @DslBlockOn(DslDto::class)
    fun dtoNameAndWhereto(  simpleName: String = C.DEFAULT, block: IDslApiNameAndWheretoProps.() -> Unit)
    @DslBlockOn(DslTable::class)
    fun tableNameAndWhereto(simpleName: String = C.DEFAULT, block: IDslApiNameAndWheretoProps.() -> Unit)
}

// === Impl Interfaces (extend IDslApi's plus methods and props that should not be visible from the DSL ===

interface IImplNameAndWheretoOnlyDelegate : IDslApiNameAndWheretoOnly {
    @DslInstance val nameAndWheretos: MutableMap<String, DslNameAndWheretoPropsImpl>
}
interface IDslImplNameAndWheretoWithSubelementsDelegate : IDslApiNameAndWheretoWithSubelements {
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
    override var strategyClassName = IClassNameStrategy.STRATEGY.DEFAULT
    override var strategyTableName = ITableNameStrategy.STRATEGY.DEFAULT
    var baseDirPathAbsolute: Path = NameAndWheretoDefaults.basePath
    override fun baseDirAbsolute(absolute: Path) { baseDirPathAbsolute = absolute }
    override fun baseDirAbsolute(absolute: String) { baseDirPathAbsolute = absolute.toPath() }
    var baseDirAddendum: Path = NameAndWheretoDefaults.path
    override fun baseDir(concat: String) { baseDirAddendum/=concat }
    override fun baseDir(concat: Path)   { baseDirAddendum/=concat }
    var pathAbsolute: Path = NameAndWheretoDefaults.path
    override fun pathAbsolute(absolute: String) { pathAbsolute = absolute.replace(pathSepREFirst, "").toPath() }
    override fun pathAbsolute(absolute: Path) { pathAbsolute = absolute.toString().replace(pathSepREFirst, "").toPath() }
    var pathAddendum: Path = NameAndWheretoDefaults.path
    override fun path(concat: String) { pathAddendum/=concat }
    override fun path(concat: Path) { pathAddendum/=concat }


    var classPrefixAbsolute: String = NameAndWheretoDefaults.classPrefix
    override fun classPrefixAbsolute(absolute: String) { classPrefixAbsolute = absolute }
    var classPrefixAddendum: String = NameAndWheretoDefaults.classPrefix
    override fun classPrefix(concat: String) { classPrefixBefore(concat) }
    override fun classPrefixBefore(concat: String) { classPrefixAddendum = "$concat$classPrefixAddendum" }
    override fun classPrefixAfter(concat: String)  { classPrefixAddendum = "$classPrefixAddendum$concat" }
    var classPostfixAbsolute: String = NameAndWheretoDefaults.classPostfix
    override fun classPostfixAbsolute(absolute: String) { classPostfixAbsolute = absolute }
    var classPostfixAddendum: String = NameAndWheretoDefaults.classPostfix
    override fun classPostfix(concat: String) { classPostfixAfter(concat) }
    override fun classPostfixBefore(concat: String) { classPostfixAddendum = "$concat$classPostfixAddendum" }
    override fun classPostfixAfter(concat: String)  { classPostfixAddendum = "$classPostfixAddendum$concat" }

    var basePackageAbsolute: String = NameAndWheretoDefaults.basePackage
    override fun basePackageAbsolute(absolute: String) { basePackageAbsolute = absolute }
    var basePackageAddendum: String = NameAndWheretoDefaults.packageName
    override fun basePackage(concat: String) { basePackageAddendum = "${basePackageAddendum.ifNotBlank{"$basePackageAddendum."}}${concat.replace(pathSepRE, ".")}" }
    var packageNameAbsolute: String = NameAndWheretoDefaults.packageName
    override fun packageNameAbsolute(absolute: String) { packageNameAbsolute = absolute }
    var packageNameAddendum: String = NameAndWheretoDefaults.packageName
    override fun packageName(concat: String) { packageNameAddendum = "${packageNameAddendum.ifNotBlank{"$packageNameAddendum."}}${concat.replace(pathSepRE, ".")}" }
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
            dslCtx.PASS_1_BASEMODELS-> {
                val dslImpl = nameAndWheretos.getOrPut(simpleName) { DslNameAndWheretoPropsImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            else -> { }
        }
    }
}

context(DslCtxWrapper)
@ChassisDslMarker
class DslNameAndWheretoWithSubelementsDelegateImpl(
    simpleNameOfParentDslBlock: String,
    parentRef: IDslRef
) : ADslDelegateClass(simpleNameOfParentDslBlock, parentRef), IDslImplNameAndWheretoWithSubelementsDelegate, IDslParticipator {
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
            dslCtx.PASS_0_CONFIGURE -> {
                val dslImpl = nameAndWheretos.getOrPut(simpleName) { DslNameAndWheretoOnSubElementsDelegateImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            dslCtx.PASS_1_BASEMODELS -> {
                val dslImpl = nameAndWheretos.getOrPut(simpleName) { DslNameAndWheretoOnSubElementsDelegateImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            else -> { }
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
    val parentDslClass = dslCtx.ctxObj<DslNameAndWheretoWithSubelementsDelegateImpl>(parentRef)

    //context(DslCtxWrapper)
    override fun dtoNameAndWhereto(simpleName: String, block: IDslApiNameAndWheretoProps.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_0_CONFIGURE -> {
                val dslImpl = parentDslClass.dtoNameAndWheretos.getOrPut(simpleName) { DslNameAndWheretoPropsImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            dslCtx.PASS_1_BASEMODELS -> {
                val dslImpl = parentDslClass.dtoNameAndWheretos.getOrPut(simpleName) { DslNameAndWheretoPropsImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            else -> { }
        }
    }
    //context(DslCtxWrapper)
    override fun tableNameAndWhereto(simpleName: String, block: IDslApiNameAndWheretoProps.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_0_CONFIGURE -> {
                val dslImpl = parentDslClass.tableNameAndWheretos.getOrPut(simpleName) { DslNameAndWheretoPropsImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            dslCtx.PASS_1_BASEMODELS -> {
                val dslImpl = parentDslClass.tableNameAndWheretos.getOrPut(simpleName) { DslNameAndWheretoPropsImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            else -> { }
        }
    }
}
