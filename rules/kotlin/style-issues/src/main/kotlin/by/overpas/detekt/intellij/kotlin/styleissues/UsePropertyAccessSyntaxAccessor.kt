package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSyntheticJavaPropertySymbol
import org.jetbrains.kotlin.load.java.propertyNamesByAccessorName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression
import org.jetbrains.kotlin.psi.KtSuperExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis

private val forbiddenGetterPrefixes = listOf("^getOr[A-Z]", "^getAnd[A-Z]", "^getIf[A-Z]").map(::Regex)

internal class UsePropertyAccessSyntaxAccessor(
    val call: KtCallExpression,
    val methodName: String,
    val argument: KtExpression?,
) {

    val propertyNames: List<String> = propertyNamesByAccessorName(Name.identifier(methodName)).map { it.identifier }

    val qualified: KtExpression = call.getQualifiedExpressionForSelectorOrThis()

    val message: String
        get() = "Use of ${if (argument == null) "getter" else "setter"} method instead of property access syntax"

    context(session: KaSession)
    fun matches(
        property: KaSyntheticJavaPropertySymbol,
        function: KaFunctionSymbol,
    ): Boolean =
        with(session) {
            val returnType = function.returnType.lowerBoundIfFlexible()
            val propertyType = property.returnType.lowerBoundIfFlexible()
            if (argument == null) {
                (!methodName.startsWith("is") || (returnType.isBooleanType && !returnType.isMarkedNullable)) &&
                    (call.parent as? KtProperty)?.name != property.name.asString() &&
                    propertyType.semanticallyEquals(returnType)
            } else {
                val isBody = (qualified.parent as? KtDeclarationWithBody)?.bodyExpression === qualified
                (if (isBody) returnType.isUnitType else !qualified.isUsedAsExpression) &&
                    argument.expressionType?.isSubtypeOf(propertyType) == true
            }
        }

    fun replacementText(propertyName: String): String {
        val receiver = call.getQualifiedExpressionForSelector()
            ?.let { it.receiverExpression.text + if (it is KtSafeQualifiedExpression) "?." else "." }
            .orEmpty()
        return receiver + propertyName + argument?.let { " = ${it.text}" }.orEmpty()
    }
}

internal fun KtCallExpression.usePropertyAccessSyntaxCall(): UsePropertyAccessSyntaxAccessor? {
    val name = (calleeExpression as? KtNameReferenceExpression)?.getReferencedName()
    val isSetter = name?.startsWith("set") == true
    val argument = valueArguments.singleOrNull()?.getArgumentExpression()
        ?.takeUnless { it is KtLambdaExpression || it is KtNamedFunction || it is KtCallableReferenceExpression }
    val qualified = getQualifiedExpressionForSelectorOrThis()
    val isApplicable = name != null &&
        (name.startsWith("get") || name.startsWith("is") || isSetter) &&
        forbiddenGetterPrefixes.none { it.containsMatchIn(name) } &&
        (qualified as? KtQualifiedExpression)?.receiverExpression !is KtSuperExpression &&
        if (isSetter) argument != null && !qualified.isChainedSetter() else valueArguments.isEmpty()
    return if (isApplicable) UsePropertyAccessSyntaxAccessor(this, name, argument.takeIf { isSetter }) else null
}

private fun KtExpression.isChainedSetter(): Boolean =
    parent is KtDotQualifiedExpression || parent is KtReturnExpression
