import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

/** use with:
 * tasks {
 *     withType<Test> {
 *         buildSrcTestConfig()
 *     }
 * }
 */
fun Test.buildSrcTestConfig() {
    // classpath += developmentOnly
    useJUnitPlatform {
        //includeEngines("junit-jupiter", "spek2")
        // includeTags "fast"
        // excludeTags "app", "integration", "messaging", "slow", "trivial"
    }
    failFast = false
    ignoreFailures = false
    // reports.html.isEnabled = true

    testLogging {
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(
            TestLogEvent.PASSED,
            TestLogEvent.FAILED,
            TestLogEvent.SKIPPED
        ) //, STARTED //, standardOut, standardError)
    }

    addTestListener(object : TestListener {
        override fun beforeTest(descriptor: TestDescriptor?) {
            logger.lifecycle("Running $descriptor")
        }

        override fun beforeSuite(p0: TestDescriptor?) = Unit
        override fun afterTest(desc: TestDescriptor, result: TestResult) = Unit
        override fun afterSuite(desc: TestDescriptor, result: TestResult) {
            if (desc.parent == null) { // will match the outermost suite
                println("\nTotal Test Results:")
                println("===================")
                val failsDefault = "${result.failedTestCount} failures"
                val fails =
                    if (result.failedTestCount > 0) BuildSrcGlobal.colorString(
                        BuildSrcGlobal.ConsoleColor.RED,
                        failsDefault
                    ) else failsDefault
                val outcome = if (result.resultType.name == "FAILURE") BuildSrcGlobal.colorString(
                    BuildSrcGlobal.ConsoleColor.RED,
                    result.resultType.name
                ) else BuildSrcGlobal.colorString(BuildSrcGlobal.ConsoleColor.GREEN, result.resultType.name)
                println("Test Results: $outcome (total: ${result.testCount} tests, ${result.successfulTestCount} successes, $fails, ${result.skippedTestCount} skipped)\n")
            }
        }
    })
    // listen to standard out and standard error of the test JVM(s)
    // onOutput { descriptor, event -> logger.lifecycle("Test: " + descriptor + " produced standard out/err: " + event.message ) }
}
