package by.overpas.detekt.intellij.kotlin.styleissues

private const val DESTRUCTURING_DECLARATION_MAX_UNUSED_COMPONENTS = 2

private const val DESTRUCTURING_DECLARATION_MIN_USED_COMPONENTS = 2

internal class DestructuringDeclarationUsages(usages: List<DestructuringDeclarationUsage>) {

    private val claims = usages.map { it.claimedIndices() }

    private val destructured = usages.filter { it.isDestructuring() }.flatMap { it.claimedIndices().orEmpty() }

    private val dropped = usages.filter { it.dropsStatement() && !it.isDestructuring() }
        .flatMap { it.claimedIndices().orEmpty() }

    fun isDestructurable(): Boolean {
        val used = claims.filterNotNull().flatten().toSet()
        val unused = (0..(used.maxOrNull() ?: -1)).count { it !in used }
        return claims.none { it == null } &&
            destructured.size == destructured.toSet().size &&
            destructured.none { it in dropped } &&
            used.size >= DESTRUCTURING_DECLARATION_MIN_USED_COMPONENTS &&
            unused <= DESTRUCTURING_DECLARATION_MAX_UNUSED_COMPONENTS
    }
}
