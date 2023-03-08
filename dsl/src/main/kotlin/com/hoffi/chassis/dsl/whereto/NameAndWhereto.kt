package com.hoffi.chassis.dsl.whereto

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.ChassisDslMarker
import com.hoffi.chassis.shared.dsl.DslBlockName
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither
import com.hoffi.chassis.shared.dsl.IDslClass
import com.hoffi.chassis.shared.helpers.pathSepRE
import okio.Path
import okio.Path.Companion.toPath

@ChassisDslMarker
abstract class NameAndWheretoImpl(
    final override val parent: IDslClass
)
    : IDslClass
{
    val wheretoRef: DslRef.DslWheretoRef = parent.selfDslRef.wheretoRef(DslBlockName.WHERETO.name)
    override val selfDslRef: DslRef = wheretoRef
    override val parentDslRef: DslRef = parent.selfDslRef
    override val groupDslRef: DslRef.DslGroupRefEither = parent.groupDslRef

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
@ChassisDslMarker
interface INameAndWheretoWithoutModelSubtypes {
    fun nameAndWhereto(block: NameAndWheretoWithoutModelSubtypesImpl.() -> Unit)
}
@ChassisDslMarker
class NameAndWheretoWithoutModelSubtypesImpl(
    parent: IDslClass
)
    : NameAndWheretoImpl(parent), INameAndWheretoWithoutModelSubtypes
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
class NameAndWheretoPlusModelSubtypesImpl(
    parent: IDslClass
)
    : NameAndWheretoImpl(parent), INameAndWheretoPlusModelSubtypes
{
    init {
        // ensure at compile-time that all ModelSubtypes are handled in this class
        when (DslSubElementRefEither.NULL) {
            is DslSubElementRefEither.NoneRef -> { }
            is DslSubElementRefEither.DslDtoRef -> { }
            is DslSubElementRefEither.DslTableRef -> { }
        }
    }

    override fun nameAndWhereto(block: NameAndWheretoPlusModelSubtypesImpl.() -> Unit) {
        this.apply(block)
    }

    val dtoNameAndWheretoImpl =   NameAndWheretoWithoutModelSubtypesImpl(this)
    val tableNameAndWheretoImpl = NameAndWheretoWithoutModelSubtypesImpl(this)
    fun dtoNameAndWhereto(  name: String = C.DEFAULT, block: NameAndWheretoImpl.() -> Unit) = dtoNameAndWheretoImpl.apply(  block)
    fun tableNameAndWhereto(name: String = C.DEFAULT, block: NameAndWheretoImpl.() -> Unit) = tableNameAndWheretoImpl.apply(block)
}
