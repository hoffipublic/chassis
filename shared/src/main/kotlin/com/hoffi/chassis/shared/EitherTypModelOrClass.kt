package com.hoffi.chassis.shared

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.nameandwhereto.ModelClassName
import com.hoffi.chassis.shared.parsedata.nameandwhereto.NameAndWheretoDefaults
import com.squareup.kotlinpoet.ClassName

interface ITypOrModelOrPoetType {
    //fun classname(prefix: String = "", postfix: String = "")
}

sealed class EitherTypOrModelOrPoetType : ITypOrModelOrPoetType {
    lateinit var modelClassName: ModelClassName
    class EitherTyp(val typ: TYP) : EitherTypOrModelOrPoetType() {
        init { modelClassName = fakePoetTypeClassName(typ.poetType) }
    }
    class EitherModel(val modelSubElement: DslRef.IModelSubelement) : EitherTypOrModelOrPoetType() {
        // modelClassName (with poetType) init'ed in finish of modelSubelement
    }
    class EitherPoetType(val poetType: ClassName) : EitherTypOrModelOrPoetType() {
        init { fakePoetTypeClassName(poetType) }
    }

    protected fun fakePoetTypeClassName(thePoetType: ClassName): ModelClassName = ModelClassName(IDslRef.NULL).apply {
            classNameStrategy = NameAndWheretoDefaults.classNameStrategy
            tableNameStrategy = NameAndWheretoDefaults.tableNameStrategy
            basePath = NameAndWheretoDefaults.basePath
            path = NameAndWheretoDefaults.path
            basePackage = NameAndWheretoDefaults.basePackage
            packageName = NameAndWheretoDefaults.packageName
            classPrefix = NameAndWheretoDefaults.classPrefix
            classPostfix = NameAndWheretoDefaults.classPostfix

            poetType = thePoetType
        }
}

