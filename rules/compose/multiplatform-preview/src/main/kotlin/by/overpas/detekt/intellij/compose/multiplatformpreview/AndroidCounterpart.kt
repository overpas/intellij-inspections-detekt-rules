package by.overpas.detekt.intellij.compose.multiplatformpreview

import dev.detekt.api.Config
import dev.detekt.api.Rule

private const val ANDROID_RULE_SET_ID = "intellij-android-compose-preview"

internal fun Rule.hasActiveAndroidCounterpart(): Boolean {
    val ruleSetConfig = config.parent?.parent?.subConfig(ANDROID_RULE_SET_ID) ?: return false
    val counterpart = ruleName.value.removePrefix("Multiplatform")
    return counterpart in ruleSetConfig.subConfigKeys() &&
        ruleSetConfig.valueOrDefault(Config.ACTIVE_KEY, true) &&
        ruleSetConfig.subConfig(counterpart).valueOrDefault(Config.ACTIVE_KEY, false)
}
