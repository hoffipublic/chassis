package com.hoffi.chassis.dsl.whereto

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.ChassisDsl
import com.hoffi.chassis.chassismodel.dsl.ChassisDslMarker
import com.hoffi.chassis.dsl.internal.DslDelegators
import com.hoffi.chassis.dsl.internal.DslRun
import com.hoffi.chassis.dsl.modelgroup.DslDto
import com.hoffi.chassis.dsl.modelgroup.DslModel
import com.hoffi.chassis.dsl.modelgroup.DslModelgroup
import com.hoffi.chassis.dsl.modelgroup.DslTable
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDelegatee
import com.hoffi.chassis.shared.helpers.pathSepRE
import okio.Path
import okio.Path.Companion.toPath

/** DSL classes, that are contained (and delegated to)
 * by multiple @ChassisDsl IDslClass'es
 * - are no IDslClass themselves
 * - and do not have a ref to the parent IDslClass
 * as these are multiple different ones, so no help to have them anyway */
@DslDelegators(DslRun::class, DslModelgroup::class, DslModel::class, DslDto::class, DslTable::class) //, DslFiller::class)
abstract class NameAndWheretoImpl(
    val runOrModelgroupOrModelOrDtoOrTable: DslRef.ICrosscuttingNameAndWhereto
)
    : IDelegatee
{
    override fun toString() = runOrModelgroupOrModelOrDtoOrTable.toString()

    var baseDir: String
        get() = baseDirPath.toString()
        set(value) { baseDirPath = value.toPath()}
    var baseDirPath: Path = java.io.File("./generated").absolutePath.toPath(normalize = true) // current working directory + "generated"
    internal var baseDirAddendum: Path = "".toPath(normalize = false)
    fun baseDir(concat: String) { baseDirAddendum/=concat }
    fun baseDir(concat: Path)   { baseDirPath/=concat }

    var classPrefix: String = ""
    var classPrefixAddendum: String = ""
    fun classPrefix(concat: String) { classPrefixBefore(concat) }
    fun classPrefixBefore(concat: String) { classPrefixAddendum = "$concat$classPrefixAddendum" }
    fun classPrefixAfter(concat: String)  { classPrefixAddendum = "$classPrefixAddendum$concat" }
    var classPostfix: String = ""
    internal var classPostfixAddendum: String = ""
    fun classPostfix(concat: String) { classPostfixAfter(concat) }
    fun classPostfixBefore(concat: String) { classPostfixAddendum = "$concat$classPostfixAddendum" }
    fun classPostfixAfter(concat: String)  { classPostfixAddendum = "$classPostfixAddendum$concat" }

    var packageName: String = "generated"
    internal var packageNameAddendum: String = ""
    fun packageName(concat: String) { packageNameAddendum = "$packageNameAddendum.${concat.replace(pathSepRE, ".")}" }
    internal var packagePrefix: String = ""
    fun packagePrefixBefore(concat: String) { packagePrefix = "$concat$packagePrefix" }
    fun packagePrefixAfter(concat: String)  { packagePrefix = "$packagePrefix$concat" }
    internal var packagePostfix: String = ""
    fun packagePostfixBefore(concat: String) { packagePostfix= "$concat$packagePostfix" }
    fun packagePostfixAfter(concat: String)  { packagePostfix= "$packagePostfix$concat" }
}
@ChassisDsl
interface INameAndWheretoWithoutModelSubtypes {
    fun nameAndWhereto(block: NameAndWheretoWithoutModelSubtypesImpl.() -> Unit)
}
@ChassisDsl
class NameAndWheretoWithoutModelSubtypesImpl( // TODO simpleName as first param
    runOrModelgroupOrModelOrDtoOrTable: DslRef.ICrosscuttingNameAndWhereto
)
    : NameAndWheretoImpl(runOrModelgroupOrModelOrDtoOrTable), INameAndWheretoWithoutModelSubtypes
{
    override fun nameAndWhereto(block: NameAndWheretoWithoutModelSubtypesImpl.() -> Unit) {
        this.apply(block)
    }
}
@ChassisDslMarker
interface INameAndWheretoPlusModelSubtypes {
    fun nameAndWhereto(block: NameAndWheretoPlusModelSubtypesImpl.() -> Unit)
}
@ChassisDslMarker
class NameAndWheretoPlusModelSubtypesImpl( // TODO simpleName as first param
    runOrModelgroupOrModelOrDtoOrTable: DslRef.ICrosscuttingNameAndWhereto
)
    : NameAndWheretoImpl(runOrModelgroupOrModelOrDtoOrTable), INameAndWheretoPlusModelSubtypes
{
    override fun nameAndWhereto(block: NameAndWheretoPlusModelSubtypesImpl.() -> Unit) {
        this.apply(block)
    }

    // TODO simpleName as first param, so do not instantiate upfront, but set to NULL
    val dtoNameAndWheretoImpl =   NameAndWheretoWithoutModelSubtypesImpl(runOrModelgroupOrModelOrDtoOrTable)
    val tableNameAndWheretoImpl = NameAndWheretoWithoutModelSubtypesImpl(runOrModelgroupOrModelOrDtoOrTable)
    fun dtoNameAndWhereto(  name: String = C.DEFAULT, block: NameAndWheretoImpl.() -> Unit) = dtoNameAndWheretoImpl.apply(  block)
    fun tableNameAndWhereto(name: String = C.DEFAULT, block: NameAndWheretoImpl.() -> Unit) = tableNameAndWheretoImpl.apply(block)
}
