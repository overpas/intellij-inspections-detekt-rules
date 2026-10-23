package by.overpas.detekt.intellij.android.kotlin.illegalidentifier

import org.jetbrains.kotlin.psi.KtFile

private val testSourceSet = Regex("^test(?![a-z])|Test(?![a-z])")
private val deviceTestSourceSet = Regex("androidTest|AndroidTest|InstrumentedTest|DeviceTest")

internal fun KtFile.isInHostTestSourceSet(): Boolean {
    val sourceSet = virtualFilePath.split('/').zipWithNext().lastOrNull { (dir, _) -> dir == "src" }?.second
    return sourceSet != null && testSourceSet.containsMatchIn(
        sourceSet,
    ) && !deviceTestSourceSet.containsMatchIn(sourceSet)
}
