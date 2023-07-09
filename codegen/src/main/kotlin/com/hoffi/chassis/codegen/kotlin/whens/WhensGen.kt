package com.hoffi.chassis.codegen.kotlin.whens

import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType

object WhensGen {
    /** prototype
    WhensGen.whenTypeAndCollectionType(typModelOrClass, collectionType,
        preFunc = { },
        preNonCollection = { },
        preCollection = { },
        isModel = { },
        isPoetType = { },
        isTyp = { },
        postNonCollection = { },
        isModelList = { },
        isModelSet = { },
        isModelCollection = { },
        isModelIterable = { },
        isPoetTypeList = { },
        isPoetTypeSet = { },
        isPoetTypeCollection = { },
        isPoetTypeIterable = { },
        isTypList = { },
        isTypSet = { },
        isTypCollection = { },
        isTypIterable = { },
        postCollection = { },
    )
    */
    fun whenTypeAndCollectionType(
        typModelOrClass: EitherTypOrModelOrPoetType,
        collectionType: COLLECTIONTYP,
        preFunc: () -> Unit,
        preNonCollection: () -> Unit,
        preCollection: () -> Unit,
        isModel: EitherTypOrModelOrPoetType.EitherModel.() -> Unit,
        isPoetType: EitherTypOrModelOrPoetType.EitherPoetType.() -> Unit,
        isTyp: EitherTypOrModelOrPoetType.EitherTyp.() -> Unit,
        isModelList: EitherTypOrModelOrPoetType.EitherModel.() -> Unit,
        postNonCollection: () -> Unit,
        isModelSet: EitherTypOrModelOrPoetType.EitherModel.() -> Unit,
        isModelCollection: EitherTypOrModelOrPoetType.EitherModel.() -> Unit,
        isModelIterable: EitherTypOrModelOrPoetType.EitherModel.() -> Unit,
        isPoetTypeList: EitherTypOrModelOrPoetType.EitherPoetType.() -> Unit,
        isPoetTypeSet: EitherTypOrModelOrPoetType.EitherPoetType.() -> Unit,
        isPoetTypeCollection: EitherTypOrModelOrPoetType.EitherPoetType.() -> Unit,
        isPoetTypeIterable: EitherTypOrModelOrPoetType.EitherPoetType.() -> Unit,
        isTypList: EitherTypOrModelOrPoetType.EitherTyp.() -> Unit,
        isTypSet: EitherTypOrModelOrPoetType.EitherTyp.() -> Unit,
        isTypCollection: EitherTypOrModelOrPoetType.EitherTyp.() -> Unit,
        isTypIterable: EitherTypOrModelOrPoetType.EitherTyp.() -> Unit,
        postCollection: () -> Unit,
    ) {
        preFunc()
        when (collectionType) {
            is COLLECTIONTYP.NONE -> preNonCollection()
            is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE, is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET -> preCollection()
        }
        when (typModelOrClass) {
            is EitherTypOrModelOrPoetType.EitherModel -> {
                whenModelPropCollectionType(collectionType, typModelOrClass, isModel, isModelList, isModelSet, isModelCollection, isModelIterable)
            }
            is EitherTypOrModelOrPoetType.EitherPoetType -> {
                whenPoetTypePropCollectionType(collectionType, typModelOrClass, isPoetType, isPoetTypeList, isPoetTypeSet, isPoetTypeCollection, isPoetTypeIterable)
            }
            is EitherTypOrModelOrPoetType.EitherTyp -> {
                whenTypPropCollectionType(collectionType, typModelOrClass, isTyp, isTypList, isTypSet, isTypCollection, isTypIterable)
            }
            is EitherTypOrModelOrPoetType.NOTHING -> throw DslException("does not have a Type (NOTHING)")
        }
        when (collectionType) {
            is COLLECTIONTYP.NONE -> postNonCollection()
            is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE, is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET -> postCollection()
        }

    }
    fun whenModelPropCollectionType(
        collectionType: COLLECTIONTYP,
        modelEither: EitherTypOrModelOrPoetType.EitherModel,
        isModel: EitherTypOrModelOrPoetType.EitherModel.() -> Unit,
        isModelList: EitherTypOrModelOrPoetType.EitherModel.() -> Unit,
        isModelSet: EitherTypOrModelOrPoetType.EitherModel.() -> Unit,
        isModelCollection: EitherTypOrModelOrPoetType.EitherModel.() -> Unit,
        isModelIterable: EitherTypOrModelOrPoetType.EitherModel.() -> Unit,
    ) {
        when (collectionType) {
            is COLLECTIONTYP.COLLECTION -> modelEither.isModelCollection()
            is COLLECTIONTYP.ITERABLE -> modelEither.isModelIterable()
            is COLLECTIONTYP.LIST -> modelEither.isModelList()
            is COLLECTIONTYP.SET -> modelEither.isModelSet()
            is COLLECTIONTYP.NONE -> modelEither.isModel()
        }
    }
    fun whenPoetTypePropCollectionType(
        collectionType: COLLECTIONTYP,
        poetTypeEither: EitherTypOrModelOrPoetType.EitherPoetType,
        isPoetType: EitherTypOrModelOrPoetType.EitherPoetType.() -> Unit,
        isPoetTypeList: EitherTypOrModelOrPoetType.EitherPoetType.() -> Unit,
        isPoetTypeSet: EitherTypOrModelOrPoetType.EitherPoetType.() -> Unit,
        isPoetTypeCollection: EitherTypOrModelOrPoetType.EitherPoetType.() -> Unit,
        isPoetTypeIterable: EitherTypOrModelOrPoetType.EitherPoetType.() -> Unit,
    ) {
        when (collectionType) {
            is COLLECTIONTYP.NONE -> poetTypeEither.isPoetType()
            is COLLECTIONTYP.COLLECTION -> poetTypeEither.isPoetTypeCollection()
            is COLLECTIONTYP.ITERABLE -> poetTypeEither.isPoetTypeIterable()
            is COLLECTIONTYP.LIST -> poetTypeEither.isPoetTypeList()
            is COLLECTIONTYP.SET -> poetTypeEither.isPoetTypeSet()
        }
    }
    fun whenTypPropCollectionType(
        collectionType: COLLECTIONTYP,
        TypEither: EitherTypOrModelOrPoetType.EitherTyp,
        isTyp: EitherTypOrModelOrPoetType.EitherTyp.() -> Unit,
        isTypList: EitherTypOrModelOrPoetType.EitherTyp.() -> Unit,
        isTypSet: EitherTypOrModelOrPoetType.EitherTyp.() -> Unit,
        isTypCollection: EitherTypOrModelOrPoetType.EitherTyp.() -> Unit,
        isTypIterable: EitherTypOrModelOrPoetType.EitherTyp.() -> Unit,
    ) {
        when (collectionType) {
            is COLLECTIONTYP.NONE -> TypEither.isTyp()
            is COLLECTIONTYP.COLLECTION -> TypEither.isTypCollection()
            is COLLECTIONTYP.ITERABLE -> TypEither.isTypIterable()
            is COLLECTIONTYP.LIST -> TypEither.isTypList()
            is COLLECTIONTYP.SET -> TypEither.isTypSet()
        }
    }

}
