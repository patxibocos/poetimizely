package io.github.patxibocos.poetimizely.core.generator

import com.pinterest.ktlint.core.Code
import com.pinterest.ktlint.core.KtLintRuleEngine
import com.pinterest.ktlint.core.RuleSetProviderV2
import java.util.*

fun ktLint(code: String): String {
    val ruleProviders = buildSet {
        ServiceLoader.load(RuleSetProviderV2::class.java).flatMapTo(this) { it.getRuleProviders() }
    }
    val ktLintRuleEngine = KtLintRuleEngine(ruleProviders = ruleProviders)
    return ktLintRuleEngine.format(
        Code.CodeSnippet(
            code,
        ),
    )
}

fun something(a: String): Boolean {
    return a == "1" || true
}