package io.github.patxibocos.poetimizely.core.generator

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import java.util.*

fun ktLint(code: String): String {
    val ruleProviders = buildSet {
        ServiceLoader.load(RuleSetProviderV3::class.java).flatMapTo(this) { it.getRuleProviders() }
    }
    val ktLintRuleEngine = KtLintRuleEngine(ruleProviders = ruleProviders)
    return ktLintRuleEngine.format(
        Code.fromSnippet(code),
    )
}
