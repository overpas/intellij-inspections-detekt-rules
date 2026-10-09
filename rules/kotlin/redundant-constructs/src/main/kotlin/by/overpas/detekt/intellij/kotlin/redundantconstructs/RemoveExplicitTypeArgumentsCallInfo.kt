@file:OptIn(KaExperimentalApi::class)

package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypePointer

internal data class RemoveExplicitTypeArgumentsCallInfo(
    val types: List<KaTypePointer<KaType>>,
    val diagnosticCount: Int,
)
