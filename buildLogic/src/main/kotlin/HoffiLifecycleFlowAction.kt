import org.gradle.api.Plugin
import org.gradle.api.flow.*
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import javax.inject.Inject


/**
 * also see: https://github.com/TWiStErRob/repros/blob/97880ba5f3e039e1d76a2abc21a158669b93d67a/gradle/buildFinished-deprecated/settings.gradle.kts
 */


abstract class HoffiLifecycleFlowAction : FlowAction<HoffiLifecycleFlowAction.Parameters> {
    interface Parameters : FlowParameters {
        @Input
        fun message(): Property<StringBuilder>
    }

    @Override
    override fun execute(parameters: Parameters) {
        val message: StringBuilder = parameters.message().get()
        println(message.toString())
    }
}

abstract class HoffiLifecyclePlugin : Plugin<Settings?> {
    @get:Inject
    protected abstract val flowScope: FlowScope

    @get:Inject
    protected abstract val flowProviders: FlowProviders

    @Override
    override fun apply(settings: Settings) {
        flowScope.always(HoffiLifecycleFlowAction::class.java) {
            val buildWorkResult: BuildWorkResult = flowProviders.buildWorkResult.get()
            val sb = StringBuilder()
            if ( ! buildWorkResult.failure.isPresent) {
                sb.append("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
                sb.append("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
                sb.append("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
                sb.append("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
                sb.append("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
                sb.append("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
                sb.append("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
                sb.append("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
                sb.append("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
            }
            this.parameters.message().set(sb)
        }
    }
}
