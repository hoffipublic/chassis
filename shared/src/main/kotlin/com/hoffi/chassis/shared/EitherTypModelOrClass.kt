package com.hoffi.chassis.shared

import com.hoffi.chassis.shared.dsl.DslRef
import com.squareup.kotlinpoet.TypeName

sealed class EitherTypOrModelOrPoetType {
    class EitherTyp(val typ: TYP) : EitherTypOrModelOrPoetType()
    class EitherModel(val modelSubElement: DslRef.IModelSubelement) : EitherTypOrModelOrPoetType()
    class EitherPoetType(val poetType: TypeName) : EitherTypOrModelOrPoetType()
}
