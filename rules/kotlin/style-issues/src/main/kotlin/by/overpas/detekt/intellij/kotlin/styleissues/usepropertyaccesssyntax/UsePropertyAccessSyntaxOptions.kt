package by.overpas.detekt.intellij.kotlin.styleissues.usepropertyaccesssyntax

internal val USE_PROPERTY_ACCESS_SYNTAX_IGNORED_FUNCTIONS = buildList {
    listOf("java.net.Socket", "java.net.URLConnection")
        .flatMapTo(this) { owner -> listOf("getInputStream", "getOutputStream").map { "$owner.$it" } }
    listOf("AtomicInteger", "AtomicLong").flatMapTo(this) { owner ->
        listOf("getAndIncrement", "getAndDecrement", "getAcquire", "getOpaque", "getPlain")
            .map { method -> "java.util.concurrent.atomic.$owner.$method" }
    }
    listOf("getChar", "getDouble", "getFloat", "getInt", "getLong", "getShort")
        .mapTo(this) { "java.nio.ByteBuffer.$it" }
}

internal data class UsePropertyAccessSyntaxOptions(
    val ignoredFunctions: Set<String>,
    val areNonTrivialAccessorsReported: Boolean,
)
