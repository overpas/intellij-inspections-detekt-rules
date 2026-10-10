package by.overpas.detekt.intellij

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class OppositeRule(vararg val rules: KClass<*>)
