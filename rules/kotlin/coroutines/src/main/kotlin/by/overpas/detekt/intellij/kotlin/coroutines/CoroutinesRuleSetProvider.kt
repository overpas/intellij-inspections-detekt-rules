package by.overpas.detekt.intellij.kotlin.coroutines

import by.overpas.detekt.intellij.IntellijInspectionGroup
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

@IntellijInspectionGroup("group.names.coroutine")
class CoroutinesRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-coroutines")

    override fun instance() =
        RuleSet(
            ruleSetId,
            listOf(
                ::CoroutineContextWithJob,
                ::DeferredResultUnused,
                ::ForEachJoinOnCollectionOfJob,
                ::MapAwaitOnCollectionOfDeferred,
                ::PreferCurrentCoroutineContextToCoroutineContext,
                ::RunBlockingInSuspendFunction,
                ::SimplifiableFlowCall,
                ::SimplifiableFlowCallChain,
                ::SuspendCoroutineLacksCancellationGuarantees,
                ::SuspiciousImplicitCoroutineScopeReceiverAccess,
                ::SuspiciousMutableCollectionInStateFlow,
                ::UnusedFlow,
                ::UselessCallOnFlow,
            ),
        )
}
