import com.optimizely.ab.Optimizely
import com.optimizely.ab.OptimizelyFactory

fun main() {
    val optimizely = buildOptimizely()
    optimizely.getAllExperiments().forEach(::println)
}

private fun buildOptimizely(): Optimizely =
    OptimizelyFactory.newDefaultInstance(System.getenv("OPTIMIZELY_SDK_KEY"))
