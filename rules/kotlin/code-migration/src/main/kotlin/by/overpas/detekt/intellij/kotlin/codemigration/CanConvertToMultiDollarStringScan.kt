package by.overpas.detekt.intellij.kotlin.codemigration

import org.jetbrains.kotlin.psi.KtBlockStringTemplateEntry
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtSimpleNameStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateEntry

private const val DOLLAR = '$'

private const val PREFIX_LENGTH_THRESHOLD = 5

private const val IDENTIFIER_OR_BLOCK_STARTS = "_{`"

private val DOLLAR_LITERAL_EXPRESSIONS = setOf("'$'", "\"$\"")

internal class CanConvertToMultiDollarStringScan(
    private val hasEscapedDollar: Boolean,
    private val longestUnsafe: Int,
    private val sequentialDollars: Int,
) {

    fun isConvertible(): Boolean =
        hasEscapedDollar && longestUnsafe < PREFIX_LENGTH_THRESHOLD

    fun accept(entry: KtStringTemplateEntry): CanConvertToMultiDollarStringScan =
        when {
            entry is KtLiteralStringTemplateEntry -> acceptLiteral(entry.text)

            entry is KtSimpleNameStringTemplateEntry -> next(longestUnsafe, 0)

            entry.isEscapedDollar -> CanConvertToMultiDollarStringScan(
                hasEscapedDollar = true,
                longestUnsafe = longestUnsafe,
                sequentialDollars = sequentialDollars + 1,
            )

            else -> next(longestUnsafe, 0)
        }

    private fun acceptLiteral(text: String): CanConvertToMultiDollarStringScan {
        val trailingDollars = text.takeLastWhile { it == DOLLAR }.length
        val isIdentifierOrBlockStart =
            text.firstOrNull()?.let { it.isLetter() || it in IDENTIFIER_OR_BLOCK_STARTS } == true
        return when {
            isIdentifierOrBlockStart ->
                next(maxOf(longestUnsafe, sequentialDollars), trailingDollars)

            trailingDollars == text.length -> next(longestUnsafe, sequentialDollars + text.length)

            else -> next(longestUnsafe, trailingDollars)
        }
    }

    private fun next(
        longest: Int,
        sequential: Int,
    ): CanConvertToMultiDollarStringScan =
        CanConvertToMultiDollarStringScan(
            hasEscapedDollar = hasEscapedDollar,
            longestUnsafe = longest,
            sequentialDollars = sequential,
        )
}

private val KtStringTemplateEntry.isEscapedDollar: Boolean
    get() = when (this) {
        is KtEscapeStringTemplateEntry -> unescapedValue == "$DOLLAR"
        is KtBlockStringTemplateEntry -> expression?.text in DOLLAR_LITERAL_EXPRESSIONS
        else -> false
    }
