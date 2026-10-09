package by.overpas.detekt.intellij.kotlin.styleissues

private const val SIMPLIFIABLE_CALL_CHAIN_COLLECTIONS = "kotlin.collections"

private const val SIMPLIFIABLE_CALL_CHAIN_TEXT = "kotlin.text"

private const val SIMPLIFIABLE_CALL_CHAIN_MAP = "map"

private val SIMPLIFIABLE_CALL_CHAIN_FIRST_ENDS = listOf("first", "firstOrNull")

private val SIMPLIFIABLE_CALL_CHAIN_SORTED_ENDS = SIMPLIFIABLE_CALL_CHAIN_FIRST_ENDS + listOf("last", "lastOrNull")

private val SIMPLIFIABLE_CALL_CHAIN_FILTER_ENDS =
    SIMPLIFIABLE_CALL_CHAIN_SORTED_ENDS + listOf("single", "singleOrNull", "count", "any", "none")

private val SIMPLIFIABLE_CALL_CHAIN_SHAPES = listOf(
    Triple(listOf("filter"), SIMPLIFIABLE_CALL_CHAIN_FILTER_ENDS, SimplifiableCallChainKind.PLAIN),
    Triple(listOf("sorted", "sortedDescending"), SIMPLIFIABLE_CALL_CHAIN_SORTED_ENDS, SimplifiableCallChainKind.PLAIN),
    Triple(listOf("sortedBy", "sortedByDescending"), SIMPLIFIABLE_CALL_CHAIN_SORTED_ENDS, SimplifiableCallChainKind.BY),
    Triple(listOf("mapNotNull"), SIMPLIFIABLE_CALL_CHAIN_FIRST_ENDS, SimplifiableCallChainKind.PLAIN),
    Triple(listOf(SIMPLIFIABLE_CALL_CHAIN_MAP, "mapIndexed"), listOf("flatten"), SimplifiableCallChainKind.PLAIN),
    Triple(
        listOf(SIMPLIFIABLE_CALL_CHAIN_MAP),
        listOf("max", "maxOrNull", "min", "minOrNull"),
        SimplifiableCallChainKind.PLAIN,
    ),
    Triple(listOf(SIMPLIFIABLE_CALL_CHAIN_MAP), listOf("joinTo", "joinToString"), SimplifiableCallChainKind.JOIN),
    Triple(listOf(SIMPLIFIABLE_CALL_CHAIN_MAP), listOf("filterNotNull"), SimplifiableCallChainKind.MAP_NOT_NULL),
    Triple(listOf(SIMPLIFIABLE_CALL_CHAIN_MAP), listOf("sum"), SimplifiableCallChainKind.SUM_OF),
)

private val SIMPLIFIABLE_CALL_CHAIN_ITERABLE_CONVERSIONS =
    listOf(SIMPLIFIABLE_CALL_CHAIN_COLLECTIONS, "kotlin.sequences").flatMap { pkg ->
        SIMPLIFIABLE_CALL_CHAIN_SHAPES.flatMap { (firsts, seconds, kind) ->
            firsts.flatMap { first ->
                seconds.map { second -> SimplifiableCallChainConversion("$pkg.$first", "$pkg.$second", kind) }
            }
        } +
            SimplifiableCallChainConversion(
                "$pkg.$SIMPLIFIABLE_CALL_CHAIN_MAP",
                "$SIMPLIFIABLE_CALL_CHAIN_COLLECTIONS.toMap",
            )
    }

private val SIMPLIFIABLE_CALL_CHAIN_TEXT_CONVERSIONS =
    (SIMPLIFIABLE_CALL_CHAIN_FILTER_ENDS + listOf("isEmpty", "isNotEmpty")).map { second ->
        SimplifiableCallChainConversion("$SIMPLIFIABLE_CALL_CHAIN_TEXT.filter", "$SIMPLIFIABLE_CALL_CHAIN_TEXT.$second")
    } +
        listOf(SIMPLIFIABLE_CALL_CHAIN_MAP, "mapIndexed").map { first ->
            SimplifiableCallChainConversion(
                "$SIMPLIFIABLE_CALL_CHAIN_TEXT.$first",
                "$SIMPLIFIABLE_CALL_CHAIN_COLLECTIONS.flatten",
            )
        }

internal val SIMPLIFIABLE_CALL_CHAIN_CONVERSIONS =
    buildList {
        addAll(SIMPLIFIABLE_CALL_CHAIN_ITERABLE_CONVERSIONS)
        addAll(SIMPLIFIABLE_CALL_CHAIN_TEXT_CONVERSIONS)
        listOf("isNotEmpty", "List.isEmpty").mapTo(this) { second ->
            SimplifiableCallChainConversion(
                "$SIMPLIFIABLE_CALL_CHAIN_COLLECTIONS.filter",
                "$SIMPLIFIABLE_CALL_CHAIN_COLLECTIONS.$second",
            )
        }
        add(
            SimplifiableCallChainConversion(
                "$SIMPLIFIABLE_CALL_CHAIN_COLLECTIONS.listOf",
                "$SIMPLIFIABLE_CALL_CHAIN_COLLECTIONS.filterNotNull",
            ),
        )
    }

internal data class SimplifiableCallChainConversion(
    val first: String,
    val second: String,
    val kind: SimplifiableCallChainKind = SimplifiableCallChainKind.PLAIN,
) {

    val firstName = first.substringAfterLast('.')

    val secondName = second.substringAfterLast('.')
}
