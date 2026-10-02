/*
 * Copyright (c) 2026 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 9/30/2026
 * Last modified 9/30/26, 10:00 AM
 */

package studio.lunabee.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.psiUtil.parents
import java.net.URI

/**
 * Reports a switch to a background dispatcher (`withContext`, `flowOn`, `launch`, `async`) made from a class whose name contains
 * one of [forbiddenClassNameParts] (presenter, reducer, use case, repository, view model, fragment, activity) or from a
 * composable. Datasources, DAOs and remote clients are main-safe and own the dispatcher switch.
 *
 * Detection is syntactic (no type resolution): the context argument is flagged when one of its `+` terms mentions a dispatcher
 * other than Main, so injected dispatchers (`ioDispatcher`, `dispatchers.io`) are caught as well as `Dispatchers.IO`.
 */
class DispatcherSwitchOutsideDataLayer(config: Config) : Rule(
    config,
    "Only datasources, DAOs and remote clients switch dispatcher. Callers above them stay dispatcher-agnostic.",
    URI("https://developer.android.com/kotlin/coroutines/coroutines-best-practices#main-safe"),
) {
    private val forbiddenClassNameParts: List<String> by config(
        listOf("Presenter", "Reducer", "UseCase", "Repository", "ViewModel", "Fragment", "Activity"),
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callName = expression.calleeExpression?.text ?: return
        if (callName !in DispatcherSwitchCalls) return
        val context = contextArgument(expression) ?: return
        if (!mentionsBackgroundDispatcher(context)) return
        if (!isInForbiddenScope(expression)) return

        report(
            Finding(
                Entity.from(expression),
                "`$callName(${context.text})` switches dispatcher outside the data layer. Make the datasource/DAO/remote client " +
                    "main-safe and call it directly: $url",
            ),
        )
    }

    private fun contextArgument(expression: KtCallExpression): KtExpression? {
        val arguments = expression.valueArguments.filterNot { it is KtLambdaArgument }
        val argument = arguments.firstOrNull { it.getArgumentName()?.asName?.asString() == ContextParameterName }
            ?: arguments.firstOrNull { it.getArgumentName() == null }
        return argument?.getArgumentExpression()
    }

    private fun mentionsBackgroundDispatcher(context: KtExpression): Boolean = contextTerms(context).any { term ->
        term.text.contains(DispatcherKeyword, ignoreCase = true) && !MainDispatcherRegex.containsMatchIn(term.text)
    }

    private fun contextTerms(expression: KtExpression): List<KtExpression> = when {
        expression is KtParenthesizedExpression -> expression.expression?.let(::contextTerms).orEmpty()

        expression is KtBinaryExpression && expression.operationToken == KtTokens.PLUS ->
            listOfNotNull(expression.left, expression.right).flatMap(::contextTerms)

        else -> listOf(expression)
    }

    private fun isInForbiddenScope(expression: KtCallExpression): Boolean = expression.parents.any { parent ->
        when (parent) {
            is KtClassOrObject -> parent.name?.let { name -> forbiddenClassNameParts.any(name::contains) } == true
            is KtNamedFunction -> parent.annotationEntries.any { it.shortName?.asString() == ComposableAnnotation }
            else -> false
        }
    }
}

private val DispatcherSwitchCalls: Set<String> = setOf("withContext", "flowOn", "launch", "async")
private const val ContextParameterName: String = "context"
private const val DispatcherKeyword: String = "dispatcher"
private const val ComposableAnnotation: String = "Composable"

// Matches `Dispatchers.Main`, `mainDispatcher`, `dispatchers.main` but not `domainDispatcher`.
private val MainDispatcherRegex: Regex = Regex("(^|[^A-Za-z])main|Main")
