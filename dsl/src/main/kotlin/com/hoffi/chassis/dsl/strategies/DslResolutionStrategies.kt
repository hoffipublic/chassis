//package com.hoffi.chassis.dsl.strategies
//
//import com.hoffi.chassis.chassismodel.C
//import com.hoffi.chassis.chassismodel.dsl.DslException
//import com.hoffi.chassis.dsl.modelgroup.AModelSubelement
//import com.hoffi.chassis.shared.parsedata.nameandwhereto.ModelClassName
//import com.hoffi.chassis.shared.shared.GatherPropertys
//import okio.Path.Companion.toPath
//
//// TODO throw all away???
//interface IChassisStrategy {
//    val strategyName: String
//}
//
//interface IModelNameAndWheretoStrategy : IChassisStrategy {
//    fun resolve(dslModelSubElement: AModelSubelement): ModelClassName
//}
//class ModelNameAndWheretoStrategySpecialWins
//    : IModelNameAndWheretoStrategy {
//    override val strategyName = this::class.simpleName!!
//
//    override fun resolve(dslModelSubElement: AModelSubelement): ModelClassName {
//        val modelClassName =  ModelClassName(dslModelSubElement.selfDslRef).apply {
//            // TODO XXX
//            basePath = ".".toPath()
//            path = basePath
//            basePackage = "com.chassis"
//            packageName = basePackage
//            classPrefix = ""
//            classPostfix = ""
//        }
//        TODO("NOT yet implemented")
//        return modelClassName
//    }
//}
//
//interface IModelGatherPropertiesStrategy : IChassisStrategy {
//    fun resolve(dslModelSubElement: AModelSubelement): Set<GatherPropertys>
//}
//class ModelGatherPropertiesStrategySpecialWins
//    : IModelGatherPropertiesStrategy {
//    override val strategyName = this::class.simpleName!!
//    override fun resolve(dslModelSubElement: AModelSubelement): Set<GatherPropertys> {
//        TODO("Not yet implemented")
//    }
//}
//class ModelGatherPropertiesStrategyAdditive
//    : IModelGatherPropertiesStrategy {
//    override val strategyName = this::class.simpleName!!
//    override fun resolve(dslModelSubElement: AModelSubelement): Set<GatherPropertys> {
//        TODO("Not yet implemented")
//    }
//}
//
//object DslResolutionStrategies {
//    private val modelNameAndWheretoResolveStrategies= mutableMapOf<String, IModelNameAndWheretoStrategy>(
//        ModelNameAndWheretoStrategySpecialWins().let { it.strategyName to it }
//    ).also {
//        it[C.DEFAULT] = it[ModelNameAndWheretoStrategySpecialWins::class.simpleName]!!
//    }
//    fun resolveNameAndWheretoStrategy(dslModelSubElement: AModelSubelement, strategyName: String = C.DEFAULT): ModelClassName {
//        return modelNameAndWheretoResolveStrategies[strategyName]?.resolve(dslModelSubElement) ?: throw DslException("ref: '$dslModelSubElement' unknown modelNameAndWheretoStrategy: '$strategyName'")
//    }
//
//    private val modelGatherPropertiesStrategies = mutableMapOf<String, IModelGatherPropertiesStrategy>(
//        ModelGatherPropertiesStrategyAdditive().let { it.strategyName to it },
//        ModelGatherPropertiesStrategySpecialWins().let { it.strategyName to it }
//    ).also {
//        it[C.DEFAULT] = it[ModelGatherPropertiesStrategyAdditive::class.simpleName]!!
//    }
//    fun resolveGatherPropertiesStrategy(dslModelSubElement: AModelSubelement, strategyName: String = C.DEFAULT): Set<GatherPropertys> {
//        return modelGatherPropertiesStrategies[strategyName]?.resolve(dslModelSubElement) ?: throw DslException("ref: '$dslModelSubElement' unknown resolveGatherPropertiesStrategy: '$strategyName'")
//    }
//}
