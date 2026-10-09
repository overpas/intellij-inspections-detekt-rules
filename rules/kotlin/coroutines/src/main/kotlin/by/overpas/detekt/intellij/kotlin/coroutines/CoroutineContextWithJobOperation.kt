package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaExplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val JOB_KEY_CLASS_ID = ClassId(FqName("kotlinx.coroutines"), Name.identifier("Job"))
    .createNestedClassId(Name.identifier("Key"))

internal class CoroutineContextWithJobOperation(
    private val session: KaSession,
    private val call: KaFunctionCall<*>,
) {

    private val argument = call.valueArgumentMapping.keys.singleOrNull()

    fun plusStatus(): CoroutineContextWithJobStatus {
        val argumentStatus = argument?.let { CoroutineContextWithJobDetector(session, it).status() }
        return receiverStatus().append(argumentStatus ?: CoroutineContextWithJobStatus.Unknown)
    }

    fun minusKeyStatus(): CoroutineContextWithJobStatus =
        with(session) {
            if (argument?.expressionType?.isSubtypeOf(JOB_KEY_CLASS_ID) == true) {
                CoroutineContextWithJobStatus.NoJob
            } else {
                receiverStatus()
            }
        }

    private fun receiverStatus(): CoroutineContextWithJobStatus =
        (call.dispatchReceiver as? KaExplicitReceiverValue)
            ?.let { CoroutineContextWithJobDetector(session, it.expression).status() }
            ?: CoroutineContextWithJobStatus.Unknown
}
