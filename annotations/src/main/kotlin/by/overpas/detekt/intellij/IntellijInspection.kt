package by.overpas.detekt.intellij

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class IntellijInspection(val shortName: String)
