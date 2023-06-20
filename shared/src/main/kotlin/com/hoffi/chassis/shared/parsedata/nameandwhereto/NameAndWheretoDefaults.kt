package com.hoffi.chassis.shared.parsedata.nameandwhereto

import com.hoffi.chassis.shared.strategies.ClassNameStrategyCamelCase
import com.hoffi.chassis.shared.strategies.TableNameStrategyLowerSnakeCase
import okio.Path
import okio.Path.Companion.toPath

object NameAndWheretoDefaults {
    val basePath =  ".".toPath()/"generated"
    var classNameStrategy = ClassNameStrategyCamelCase
    var tableNameStrategy = TableNameStrategyLowerSnakeCase
    var path: Path = ".".toPath()
    var basePackage = "com.chassis.generated"
    var packageName = ""
    var classPrefix = ""
    var classPostfix = ""
}
