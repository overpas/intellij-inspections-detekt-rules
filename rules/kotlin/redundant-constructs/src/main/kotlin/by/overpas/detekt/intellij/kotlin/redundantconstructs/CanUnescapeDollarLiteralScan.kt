package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.psi.KtBlockStringTemplateEntry
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateEntry

private const val DOLLAR = '$'

private const val IDENTIFIER_OR_BLOCK_STARTS = "_{`"

private val DOLLAR_LITERAL_EXPRESSIONS = setOf("'$'", "\"$\"")

internal class CanUnescapeDollarLiteralScan(
    private val prefixLength: Int,
    private val sequentialDollars: Int = 0,
    private val confirmed: Int = 0,
    private val candidates: Int = 0,
) {

    fun hasReplaceableDollars(): Boolean =
        confirmed + candidates > 0

    fun accept(entry: KtStringTemplateEntry): CanUnescapeDollarLiteralScan =
        when {
            entry.isEscapedDollar -> next(sequentialDollars + 1, confirmed, candidates + 1)
            entry is KtLiteralStringTemplateEntry -> acceptLiteral(entry.text)
            sequentialDollars > 0 -> next(0, confirmed + candidates, 0)
            else -> this
        }

    private fun acceptLiteral(text: String): CanUnescapeDollarLiteralScan {
        val lastDollars = text.takeLastWhile { it == DOLLAR }.length
        val firstDollars = text.takeWhile { it == DOLLAR }.length
        val isUnsafe = sequentialDollars + firstDollars >= prefixLength &&
            text.getOrNull(firstDollars)?.let { it.isLetter() || it in IDENTIFIER_OR_BLOCK_STARTS } == true
        val confirmedNow = if (isUnsafe) (candidates - 1).coerceAtLeast(0) else candidates
        return if (lastDollars == text.length) {
            next(sequentialDollars + text.length, confirmed, candidates)
        } else {
            next(lastDollars, confirmed + confirmedNow, 0)
        }
    }

    private fun next(
        sequential: Int,
        confirmedCount: Int,
        candidateCount: Int,
    ): CanUnescapeDollarLiteralScan =
        CanUnescapeDollarLiteralScan(
            prefixLength = prefixLength,
            sequentialDollars = sequential,
            confirmed = confirmedCount,
            candidates = candidateCount,
        )
}

private val KtStringTemplateEntry.isEscapedDollar: Boolean
    get() = when (this) {
        is KtEscapeStringTemplateEntry -> unescapedValue == DOLLAR.toString()
        is KtBlockStringTemplateEntry -> expression?.text in DOLLAR_LITERAL_EXPRESSIONS
        else -> false
    }
