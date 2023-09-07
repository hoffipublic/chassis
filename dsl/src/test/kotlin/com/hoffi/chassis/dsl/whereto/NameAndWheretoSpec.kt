package com.hoffi.chassis.dsl.whereto

//import org.koin.core.component.inject
import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Cap
import com.hoffi.chassis.dsl.internal.DslCtx
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.internal.DslRun
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.dsl.modelgroup.DslDto
import com.hoffi.chassis.dsl.modelgroup.DslModel
import com.hoffi.chassis.dsl.modelgroup.DslModelgroup
import com.hoffi.chassis.shared.dsl.DslRefString
import com.hoffi.chassis.shared.parsedata.GenModel
import com.squareup.kotlinpoet.ClassName
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import okio.Path.Companion.toPath

context(DslCtxWrapper)
fun dslOverlapping() {
    dslCtx.topLevelDslFunctionName = object {}.javaClass.enclosingMethod.name
//    apigroup(SIMPLE) {
//
//    }
    modelgroup("SpecNameAndWhereto") {
        nameAndWhereto {
            classPrefix("modelgroupClassPrefix")
            packageName("modelgroupPackageName")
            dtoNameAndWhereto {
                classPostfix("groupDtoClassPostfix")
                packageName("groupDtoPackageName")
            }
            tableNameAndWhereto {
                classPostfix("groupTableClassPostfix")
                packageName("groupTablePackageName")
            }
        }
        model("SpecModel") {
            nameAndWhereto {
                classPrefix("modelClassPrefix")
                packageName("modelPackageName")
                dtoNameAndWhereto {
                    classPostfix("modelDtoClassPostfix")
                    packageName("modelDtoPackageName")
                }
                tableNameAndWhereto {
                    classPostfix("modelTableClassPostfix")
                    packageName("modelTablePackageName")
                }
            }
            dto {
                nameAndWhereto {
                    classPrefix("dtoClassPrefix")
                    packageName("dtoPackageName")
                }
            }
            table {
                nameAndWhereto {
                    classPrefix("tableClassPrefix")
                    packageName("tablePackageName")
                }
            }
        }

    }
}
fun mainOverlapping(): Pair<DslCtx, DslRun> {
    val specRunName = "SpecNameAndWheretoRun"
    val specDslRun = DslRun(specRunName)
    specDslRun.configure {
        nameAndWhereto {
            baseDirAbsolute("../specs/nameAndWhereto/src/main/kotlin")
            basePackageAbsolute("com.hoffi.specs.whereto")
            classPrefix("runClassPrefix")
            packageName("runPackageName")
            dtoNameAndWhereto {
                classPostfix("runDtoClassPostfix")
                packageName("runDtoPackageName")
            }
            tableNameAndWhereto {
                classPostfix("runTableClassPostfix")
                packageName("runTablePackageName")
            }
        }
    }
    // actually parsing the spec DSL:
    specDslRun.start("specDisc") {
        dslOverlapping()
    }
    return Pair(specDslRun.dslCtx, specDslRun)
}

@OptIn(ExperimentalKotest::class)
class NameAndWheretoSpec : BehaviorSpec({
    Given("nameAndWhereto related DSLs") {
        When("parsing overlapping DSL") {
            val (dslCtx, specDslRun) = mainOverlapping()
            Then("values of dslRun should be there") {
                val a = specDslRun.wheretoImpl.nameAndWheretos[C.DEFAULT]!!
                val b = specDslRun.wheretoImpl.dtoNameAndWheretos[C.DEFAULT]!!
                val c = specDslRun.wheretoImpl.tableNameAndWheretos[C.DEFAULT]!!
                a.baseDirPathAbsolute shouldBe "../specs/nameAndWhereto/src/main/kotlin".toPath()
                a.baseDirAddendum shouldBe ".".toPath()
                a.pathAbsolute shouldBe ".".toPath()
                a.pathAddendum shouldBe ".".toPath()
                a.basePackageAbsolute shouldBe "com.hoffi.specs.whereto"
                a.basePackageAddendum shouldBe ""
                a.packageNameAbsolute shouldBe ""
                a.packageNameAddendum shouldBe "runPackageName"
                a.classPrefixAbsolute shouldBe ""
                a.classPrefixAddendum shouldBe "runClassPrefix"
                a.classPostfixAbsolute shouldBe ""
                a.classPostfixAddendum shouldBe ""

                b.baseDirPathAbsolute shouldBe "generated".toPath()
                b.baseDirAddendum shouldBe ".".toPath()
                b.pathAbsolute shouldBe ".".toPath()
                b.pathAddendum shouldBe ".".toPath()
                b.basePackageAbsolute shouldBe "com.chassis.generated"
                b.basePackageAddendum shouldBe ""
                b.packageNameAbsolute shouldBe ""
                b.packageNameAddendum shouldBe "runDtoPackageName"
                b.classPrefixAbsolute shouldBe ""
                b.classPrefixAddendum shouldBe ""
                b.classPostfixAbsolute shouldBe ""
                b.classPostfixAddendum shouldBe "runDtoClassPostfix"

                c.baseDirPathAbsolute shouldBe "generated".toPath()
                c.baseDirAddendum shouldBe ".".toPath()
                c.pathAbsolute shouldBe ".".toPath()
                c.pathAddendum shouldBe ".".toPath()
                c.basePackageAbsolute shouldBe "com.chassis.generated"
                c.basePackageAddendum shouldBe ""
                c.packageNameAbsolute shouldBe ""
                c.packageNameAddendum shouldBe "runTablePackageName"
                c.classPrefixAbsolute shouldBe ""
                c.classPrefixAddendum shouldBe ""
                c.classPostfixAbsolute shouldBe ""
                c.classPostfixAddendum shouldBe "runTableClassPostfix"
            }
            Then("values of modelgroup should be there") {
                val group: DslModelgroup = dslCtx.ctxObj(DslRefString.REF("modelgroup:SpecNameAndWhereto"))
                val a = group.nameAndWheretoWithSubelements.nameAndWheretos[C.DEFAULT]!!
                val b = group.nameAndWheretoWithSubelements.dtoNameAndWheretos[C.DEFAULT]!!
                val c = group.nameAndWheretoWithSubelements.tableNameAndWheretos[C.DEFAULT]!!
                a.baseDirPathAbsolute shouldBe "generated".toPath()
                a.baseDirAddendum shouldBe ".".toPath()
                a.pathAbsolute shouldBe ".".toPath()
                a.pathAddendum shouldBe ".".toPath()
                a.basePackageAbsolute shouldBe "com.chassis.generated"
                a.basePackageAddendum shouldBe ""
                a.packageNameAbsolute shouldBe ""
                a.packageNameAddendum shouldBe "modelgroupPackageName"
                a.classPrefixAbsolute shouldBe ""
                a.classPrefixAddendum shouldBe "modelgroupClassPrefix"
                a.classPostfixAbsolute shouldBe ""
                a.classPostfixAddendum shouldBe ""

                b.baseDirPathAbsolute shouldBe "generated".toPath()
                b.baseDirAddendum shouldBe ".".toPath()
                b.pathAbsolute shouldBe ".".toPath()
                b.pathAddendum shouldBe ".".toPath()
                b.basePackageAbsolute shouldBe "com.chassis.generated"
                b.basePackageAddendum shouldBe ""
                b.packageNameAbsolute shouldBe ""
                b.packageNameAddendum shouldBe "groupDtoPackageName"
                b.classPrefixAbsolute shouldBe ""
                b.classPrefixAddendum shouldBe ""
                b.classPostfixAbsolute shouldBe ""
                b.classPostfixAddendum shouldBe "groupDtoClassPostfix"

                c.baseDirPathAbsolute shouldBe "generated".toPath()
                c.baseDirAddendum shouldBe ".".toPath()
                c.pathAbsolute shouldBe ".".toPath()
                c.pathAddendum shouldBe ".".toPath()
                c.basePackageAbsolute shouldBe "com.chassis.generated"
                c.basePackageAddendum shouldBe ""
                c.packageNameAbsolute shouldBe ""
                c.packageNameAddendum shouldBe "groupTablePackageName"
                c.classPrefixAbsolute shouldBe ""
                c.classPrefixAddendum shouldBe ""
                c.classPostfixAbsolute shouldBe ""
                c.classPostfixAddendum shouldBe "groupTableClassPostfix"
            }
            Then("values of model should be there") {
                val model: DslModel = dslCtx.ctxObj(DslRefString.REF("modelgroup:SpecNameAndWhereto|model:SpecModel"))
                val a = model.nameAndWheretoWithSubelements.nameAndWheretos[C.DEFAULT]!!
                val b = model.nameAndWheretoWithSubelements.dtoNameAndWheretos[C.DEFAULT]!!
                val c = model.nameAndWheretoWithSubelements.tableNameAndWheretos[C.DEFAULT]!!
                a.baseDirPathAbsolute shouldBe "generated".toPath()
                a.baseDirAddendum shouldBe ".".toPath()
                a.pathAbsolute shouldBe ".".toPath()
                a.pathAddendum shouldBe ".".toPath()
                a.basePackageAbsolute shouldBe "com.chassis.generated"
                a.basePackageAddendum shouldBe ""
                a.packageNameAbsolute shouldBe ""
                a.packageNameAddendum shouldBe "modelPackageName"
                a.classPrefixAbsolute shouldBe ""
                a.classPrefixAddendum shouldBe "modelClassPrefix"
                a.classPostfixAbsolute shouldBe ""
                a.classPostfixAddendum shouldBe ""

                b.baseDirPathAbsolute shouldBe "generated".toPath()
                b.baseDirAddendum shouldBe ".".toPath()
                b.pathAbsolute shouldBe ".".toPath()
                b.pathAddendum shouldBe ".".toPath()
                b.basePackageAbsolute shouldBe "com.chassis.generated"
                b.basePackageAddendum shouldBe ""
                b.packageNameAbsolute shouldBe ""
                b.packageNameAddendum shouldBe "modelDtoPackageName"
                b.classPrefixAbsolute shouldBe ""
                b.classPrefixAddendum shouldBe ""
                b.classPostfixAbsolute shouldBe ""
                b.classPostfixAddendum shouldBe "modelDtoClassPostfix"

                c.baseDirPathAbsolute shouldBe "generated".toPath()
                c.baseDirAddendum shouldBe ".".toPath()
                c.pathAbsolute shouldBe ".".toPath()
                c.pathAddendum shouldBe ".".toPath()
                c.basePackageAbsolute shouldBe "com.chassis.generated"
                c.basePackageAddendum shouldBe ""
                c.packageNameAbsolute shouldBe ""
                c.packageNameAddendum shouldBe "modelTablePackageName"
                c.classPrefixAbsolute shouldBe ""
                c.classPrefixAddendum shouldBe ""
                c.classPostfixAbsolute shouldBe ""
                c.classPostfixAddendum shouldBe "modelTableClassPostfix"
            }
            Then("values of dto should be there") {
                val dto: DslDto = dslCtx.ctxObj(DslRefString.REF("modelgroup:SpecNameAndWhereto|model:SpecModel|dto:${C.DEFAULT}"))
                val a = dto.nameAndWheretoWithoutModelSubelementsImpl.nameAndWheretos[C.DEFAULT]!!
                a.baseDirPathAbsolute shouldBe "generated".toPath()
                a.baseDirAddendum shouldBe ".".toPath()
                a.pathAbsolute shouldBe ".".toPath()
                a.pathAddendum shouldBe ".".toPath()
                a.basePackageAbsolute shouldBe "com.chassis.generated"
                a.basePackageAddendum shouldBe ""
                a.packageNameAbsolute shouldBe ""
                a.packageNameAddendum shouldBe "dtoPackageName"
                a.classPrefixAbsolute shouldBe ""
                a.classPrefixAddendum shouldBe "dtoClassPrefix"
                a.classPostfixAbsolute shouldBe ""
                a.classPostfixAddendum shouldBe ""
            }
            Then("dtoGenModel should have the strategy resolved values") {
                val dtoGenModel: GenModel.DtoModel = dslCtx.genCtx.genModel(DslRefString.REF("modelgroup:SpecNameAndWhereto|model:SpecModel|dto:${C.DEFAULT}")) as GenModel.DtoModel
                dtoGenModel.modelClassName.basePath shouldBe "../specs/nameAndWhereto/src/main/kotlin".toPath()
                dtoGenModel.modelClassName.path shouldBe ".".toPath()
                dtoGenModel.modelClassName.basePackage shouldBe "com.hoffi.specs.whereto"
                dtoGenModel.modelClassName.packageName shouldBe "dtoPackageName.modelDtoPackageName.modelPackageName.groupDtoPackageName.modelgroupPackageName.runDtoPackageName.runPackageName"
                dtoGenModel.modelClassName.classPrefix shouldBe "dtoClassPrefixmodelClassPrefixmodelgroupClassPrefixrunClassPrefix"
                dtoGenModel.modelClassName.classPostfix shouldBe "modelDtoClassPostfixgroupDtoClassPostfixrunDtoClassPostfix"
                dtoGenModel.poetType shouldBe ClassName(dtoGenModel.modelClassName.basePackage + "." + dtoGenModel.modelClassName.packageName, dtoGenModel.modelClassName.classPrefix.Cap() + dtoGenModel.modelOrTypeNameString + dtoGenModel.modelClassName.classPostfix)
            }
        }
    }
})
