package com.hoffi.chassis.chassismodel.dsl

@DslMarker annotation class ChassisDslMarker

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class ChassisDsl

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.LOCAL_VARIABLE)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@ChassisDsl
annotation class DslInstance

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@ChassisDsl
annotation class NonDsl

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@ChassisDsl
annotation class DslTopLevel
