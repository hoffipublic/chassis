import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible

// ============================================================================
// ===       global functions and extension functions                      ====
// ============================================================================
fun <T> ProviderConvertible<T>.v() = this.asProvider().get()
fun <T> Provider<T>.v(): T = get()
