package com.hoffi.chassis.shared.fix

import com.hoffi.chassis.chassismodel.PoetHelpers.nullable
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.RuntimeDefaults.ANNOTATION_DTO_CLASSNAME
import com.hoffi.chassis.chassismodel.RuntimeDefaults.ANNOTATION_TABLE_CLASSNAME
import com.hoffi.chassis.chassismodel.RuntimeDefaults.DEFAULT_MEMBER_INT
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UNIVERSE___DEFAULTS
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UNIVERSE___PACKAGE
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UUIDDTO_INTERFACE_CLASSNAME
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UUIDTABLE_CLASSNAME
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UUID_PROPNAME
import com.hoffi.chassis.chassismodel.RuntimeDefaults.WAS_GENERATED_INTERFACE_ClassName
import com.hoffi.chassis.chassismodel.RuntimeDefaults.classNameInstant
import com.hoffi.chassis.chassismodel.RuntimeDefaults.classNameInstant_toLocalDateTime
import com.hoffi.chassis.chassismodel.RuntimeDefaults.classNameLocalDateTime
import com.hoffi.chassis.chassismodel.RuntimeDefaults.classNameTimeZone
import com.hoffi.chassis.chassismodel.RuntimeDefaults.classNameUUID
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.DEFAULT_INSTANT
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.DEFAULT_INT
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.DEFAULT_LONG
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.DEFAULT_STRING
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.DEFAULT_UUID
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.NULL_INSTANT
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.NULL_INT
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.NULL_LONG
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.NULL_STRING
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.NULL_UUID
import com.hoffi.chassis.shared.db.DB
import com.hoffi.chassis.shared.helpers.PoetHelpers.kdocGenerated
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

object Universe {
    @JvmStatic
    fun main(args: Array<String>) {
        codeGen()
    }
    fun codeGen() {
        universe()
        fixedClasses()
        annotations()
        dummy()
    }

    private fun dummy() {
        //class Dummy(var i: Int) {
        //    companion object {
        //        val NULL = Dummy(Defaults.DEFAULT_INT)
        //    }
        //}
        val fileSpecDummy = FileSpec.builder(UNIVERSE___PACKAGE, "Dummy")
            .addType(
                TypeSpec.classBuilder(ClassName(UNIVERSE___PACKAGE, "Dummy"))
                    .kdocGenerated("Universe Dummy")
                    .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder("i", Int::class).build())
                        .build()
                    )
                    .addProperty(PropertySpec.builder("i", Int::class).initializer("i").mutable().build())
                    .addType(TypeSpec.companionObjectBuilder()
                        .addProperty(PropertySpec.builder("NULL", ClassName(UNIVERSE___PACKAGE, "Dummy"))
                            .initializer("%T(%M)", ClassName(UNIVERSE___PACKAGE, "Dummy"), DEFAULT_MEMBER_INT)
                            .build()
                        )
                        .build()
                    )
                    .build()
            ).build()
        fileSpecDummy.writeTo(RuntimeDefaults.UNIVERSE__BASEDIR.toNioPath())
    }

    fun universe() {
        val fileSpecWasGeneratedInterface = FileSpec.builder(WAS_GENERATED_INTERFACE_ClassName.packageName, WAS_GENERATED_INTERFACE_ClassName.simpleName)
            .addType(
                TypeSpec.interfaceBuilder(WAS_GENERATED_INTERFACE_ClassName)
                .kdocGenerated("Universe ${WAS_GENERATED_INTERFACE_ClassName.simpleName}")
                .build())
            .build()
        //var universeModelGenRef = ModelGenRef(ModelRef(ModelgroupName(UNIVERSE___MODELGROUP), ModelName(WAS_GENERATED_INTERFACE_ClassName.simpleName)), GENS.COMMON)
        //var universeEitherModelNew = EitherModelNew.DtoModelFromDsl(universeModelGenRef, DslModel.universeCreate(universeModelGenRef))
        //ctx.fileSpecs.getOrPut(universeEitherModelNew){ mutableListOf()}.add(fileSpec)

        val universeDefaultsClassName = ClassName(UNIVERSE___PACKAGE, UNIVERSE___DEFAULTS)
        val typeSpecBuilder = TypeSpec.objectBuilder(UNIVERSE___DEFAULTS)
            .kdocGenerated("Universe $UNIVERSE___DEFAULTS")
            .addSuperinterface(WAS_GENERATED_INTERFACE_ClassName)
            .addProperty(PropertySpec.builder("DEFAULT_INT", Int::class).initializer("%L", DEFAULT_INT).build())
            .addProperty(PropertySpec.builder("DEFAULT_LONG", Long::class).initializer("%LL", DEFAULT_LONG).build())
            .addProperty(PropertySpec.builder("DEFAULT_STRING", String::class).initializer("%S", DEFAULT_STRING).build())
            .addProperty(PropertySpec.builder("DEFAULT_UUID", classNameUUID).initializer("%T.fromString(\"${DEFAULT_UUID}\")", classNameUUID).build())
            .addProperty(PropertySpec.builder("DEFAULT_INSTANT", classNameInstant).initializer("%T.fromEpochMilliseconds(${DEFAULT_INSTANT.toEpochMilliseconds()}L)", classNameInstant).build())
            .addProperty(PropertySpec.builder("DEFAULT_LOCALDATETIME", classNameLocalDateTime).initializer("DEFAULT_INSTANT.%M(%T.UTC)", classNameInstant_toLocalDateTime, classNameTimeZone).build())
            .addProperty(PropertySpec.builder("DEFAULT_LOCALDATETIME_DB", classNameLocalDateTime).initializer("%L", "DEFAULT_LOCALDATETIME").build())
            .addProperty(PropertySpec.builder("NULL_INT", Int::class).initializer("%L", NULL_INT).build())
            .addProperty(PropertySpec.builder("NULL_LONG", Long::class).initializer("%LL", NULL_LONG).build())
            .addProperty(PropertySpec.builder("NULL_STRING", String::class).initializer("%S", NULL_STRING).build())
            .addProperty(PropertySpec.builder("NULL_UUID", classNameUUID).initializer("%T.fromString(\"${NULL_UUID}\")", classNameUUID).build())
            .addProperty(PropertySpec.builder("NULL_INSTANT", classNameInstant).initializer("%T.fromEpochMilliseconds(${NULL_INSTANT.toEpochMilliseconds()}L)", classNameInstant).build())
            .addProperty(PropertySpec.builder("NULL_LOCALDATETIME", classNameLocalDateTime).initializer("NULL_INSTANT.%M(%T.UTC)", classNameInstant_toLocalDateTime, classNameTimeZone).build())
            .addProperty(PropertySpec.builder("NULL_LOCALDATETIME_DB", classNameLocalDateTime).initializer("%L", "NULL_LOCALDATETIME").build())
        val fileSpecUniverseDefaults = FileSpec.builder(universeDefaultsClassName.packageName, universeDefaultsClassName.simpleName)
            .addType(typeSpecBuilder.build()).build()

        fileSpecWasGeneratedInterface.writeTo(RuntimeDefaults.UNIVERSE__BASEDIR.toNioPath())
        fileSpecUniverseDefaults.writeTo(RuntimeDefaults.UNIVERSE__BASEDIR.toNioPath())


        //universeModelGenRef = ModelGenRef(ModelRef(ModelgroupName(UNIVERSE___MODELGROUP), ModelName(universeDefaultsClassName.simpleName)), GENS.COMMON)
        //universeEitherModelNew = EitherModelNew.DtoModelFromDsl(universeModelGenRef, DslModel.universeCreate(universeModelGenRef))
        //ctx.fileSpecs.getOrPut(universeEitherModelNew){ mutableListOf()}.add(fileSpec)
    }

    fun fixedClasses() {
        var typeSpec = TypeSpec.interfaceBuilder(RuntimeDefaults.UUIDDTO_INTERFACE_CLASSNAME)
            .kdocGenerated("Universe ${RuntimeDefaults.UUIDDTO_INTERFACE_CLASSNAME.simpleName}")
            .addSuperinterface(WAS_GENERATED_INTERFACE_ClassName)
            .addProperty(PropertySpec.builder(UUID_PROPNAME, classNameUUID).mutable().build())
            .build()
        var fileSpec = FileSpec.builder(UUIDDTO_INTERFACE_CLASSNAME.packageName, UUIDDTO_INTERFACE_CLASSNAME.simpleName)
            .addType(typeSpec).build()
        //val universeModelGenRef = ModelGenRef(ModelRef(ModelgroupName(UNIVERSE___MODELGROUP), ModelName(UUIDDTO_INTERFACE_CLASSNAME.simpleName)), GENS.DTO)
        //val universeEitherModelNew = EitherModelNew.DtoModelFromDsl(universeModelGenRef, DslModel.universeCreate(universeModelGenRef))
        //ctx.fileSpecs.getOrPut(universeEitherModelNew){ mutableListOf()}.add(fileSpec)
        fileSpec.writeTo(RuntimeDefaults.UNIVERSE__BASEDIR.toNioPath())

        typeSpec = TypeSpec.classBuilder(UUIDTABLE_CLASSNAME)
            .kdocGenerated("Universe ${RuntimeDefaults.UUIDTABLE_CLASSNAME.simpleName}")
            .addSuperinterface(WAS_GENERATED_INTERFACE_ClassName)
            .addModifiers(KModifier.ABSTRACT)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("name", String::class)
                    .build())
            .superclass(DB.TableClassName)
            .addSuperclassConstructorParameter("name")
            .addProperty(
                PropertySpec.builder(UUID_PROPNAME, DB.Column(RuntimeDefaults.classNameUUID))
                    .initializer("uuid(%S)", UUID_PROPNAME)
                    .build())
            .addProperty(
                PropertySpec.builder("primaryKey", DB.TablePrimaryKeyClassName, KModifier.OVERRIDE)
                    .initializer("this.PrimaryKey(uuid, name = %P)", "PK_\$name")
                    .build())
            .addType(
                TypeSpec.classBuilder("FK")
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(ParameterSpec.builder("uuidTable", UUIDTABLE_CLASSNAME).build())
                            .addParameter(ParameterSpec.builder("parentUuidTable", UUIDTABLE_CLASSNAME).build())
                            .addParameter(ParameterSpec.builder("via", KProperty1::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY), ANY.nullable())).defaultValue("NULL").build())
                            .addParameter(ParameterSpec.builder("multiplicity", Int::class.asTypeName()).build())
                            .build())
                    .addProperty(PropertySpec.builder("uuidTable", UUIDTABLE_CLASSNAME).initializer("uuidTable").build())
                    .addProperty(PropertySpec.builder("parentUuidTable", UUIDTABLE_CLASSNAME).initializer("parentUuidTable").build())
                    .addProperty(PropertySpec.builder("via", KProperty1::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY), ANY.nullable())).initializer("via").build())
                    .addProperty(PropertySpec.builder("multiplicity", Int::class.asTypeName()).initializer("multiplicity").build())
                    .addProperty(PropertySpec.builder("NULLPROP", String::class.asTypeName(), KModifier.PRIVATE).initializer("%S", "NULLPROP").build())
                    .addType(
                        TypeSpec.companionObjectBuilder()
                        .addProperty(PropertySpec.builder("NULL", KProperty1::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY), ANY)).initializer("FK::NULLPROP").build())
                        .build())
                    .addFunction(
                        FunSpec.builder("via")
                        .addModifiers(KModifier.INFIX)
                        .returns(ClassName(UUIDTABLE_CLASSNAME.packageName, UUIDTABLE_CLASSNAME.simpleName, "FK"))
                        .addParameter("via", KProperty1::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY), ANY.nullable()))
                        .addStatement("return this.copy(via = %L)", "via")
                        .build())
                    .build())
            .addFunction(
                FunSpec.builder("hasOne")
                    .addModifiers(KModifier.INFIX)
                    .returns(ClassName(UUIDTABLE_CLASSNAME.packageName, UUIDTABLE_CLASSNAME.simpleName, "FK"))
                    .addParameter("parentUuidTable", UUIDTABLE_CLASSNAME)
                    .addStatement("return FK(this, parentUuidTable, multiplicity=1)")
                    .build())
            .addFunction(
                FunSpec.builder("hasMany")
                    .addModifiers(KModifier.INFIX)
                    .returns(ClassName(UUIDTABLE_CLASSNAME.packageName, UUIDTABLE_CLASSNAME.simpleName, "FK"))
                    .addParameter("parentUuidTable", UUIDTABLE_CLASSNAME)
                    .addStatement("return FK(this, parentUuidTable, multiplicity=2)")
                    .build())
            .addFunction(
                FunSpec.builder("mappedBy")
                    .returns(Any::class.asTypeName().nullable())
                    .addParameter("mappedBy", KProperty0::class.asTypeName().parameterizedBy(STAR))
                    .addStatement("return null")
                    .build())
            .build()
        fileSpec = FileSpec.builder(UUIDTABLE_CLASSNAME.packageName, UUIDTABLE_CLASSNAME.simpleName)
            .addType(typeSpec).build()
        //val universeModelGenRef = ModelGenRef(ModelRef(ModelgroupName(UNIVERSE___MODELGROUP), ModelName(UUIDTABLE_CLASSNAME.simpleName)), GENS.TABLE)
        //val universeEitherModelNew = EitherModelNew.DtoModelFromDsl(universeModelGenRef, DslModel.universeCreate(universeModelGenRef))
        //ctx.fileSpecs.getOrPut(universeEitherModelNew){ mutableListOf()}.add(fileSpec)

        fileSpec.writeTo(RuntimeDefaults.UNIVERSE__BASEDIR.toNioPath())
    }

    fun annotations() {
        val fileSpec = FileSpec.builder(ANNOTATION_DTO_CLASSNAME.packageName, "Annotations")
            .addType(TypeSpec.annotationBuilder(ANNOTATION_DTO_CLASSNAME.simpleName)
                .primaryConstructor(FunSpec.constructorBuilder()
                    .addParameter("thisTable", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)))
                    .addParameter("targetDto", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)))
                    .build())
                .addProperty(PropertySpec.builder("thisTable", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)))
                    .initializer("thisTable")
                    .build())
                .addProperty(PropertySpec.builder("targetDto", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)))
                    .initializer("targetDto")
                    .build())
                .build())
            .addType(TypeSpec.annotationBuilder(ANNOTATION_TABLE_CLASSNAME.simpleName)
                .primaryConstructor(FunSpec.constructorBuilder()
                    .addParameter("thisDto", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)))
                    .addParameter("targetTable", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)))
                    .build())
                .addProperty(PropertySpec.builder("thisDto", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)))
                    .initializer("thisDto")
                    .build())
                .addProperty(PropertySpec.builder("targetTable", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)))
                    .initializer("targetTable")
                    .build())
                .build())
            .addType(TypeSpec.annotationBuilder("FKFROM")
                .primaryConstructor(FunSpec.constructorBuilder()
                    .addParameter("targetClass", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)), KModifier.VARARG)
                    .build())
                .addProperty(PropertySpec.builder("targetClass", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)))
                    .initializer("targetClass")
                    .build())
                .build())
            //// .addType(TypeSpec.annotationBuilder("FKTO")
            ////     .primaryConstructor(FunSpec.constructorBuilder()
            ////         .addParameter("targetClass", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)))
            ////         .build())
            ////     .addProperty(PropertySpec.builder("targetClass", KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(ANY)))
            ////         .initializer("targetClass")
            ////         .build())
            ////     .build())
            .addType(TypeSpec.annotationBuilder("TABLEMETADATA")
                .build())
            .build()

        //val universeModelGenRef = ModelGenRef(ModelRef(ModelgroupName(UNIVERSE___MODELGROUP), ModelName("Annotations")), GENS.TABLE)
        //val universeEitherModelNew = EitherModelNew.DtoModelFromDsl(universeModelGenRef, DslModel.universeCreate(universeModelGenRef))
        //ctx.fileSpecs.getOrPut(universeEitherModelNew){ mutableListOf()}.add(fileSpec)
        fileSpec.writeTo(RuntimeDefaults.UNIVERSE__BASEDIR.toNioPath())
    }
}
