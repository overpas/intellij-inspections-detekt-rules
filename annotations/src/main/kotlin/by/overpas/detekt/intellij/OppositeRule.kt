package by.overpas.detekt.intellij

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class OppositeRule(vararg val ruleNames: String)
