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
import dev.detekt.test.TestConfig
import dev.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class DispatcherSwitchOutsideDataLayerTest {

    private val rule = DispatcherSwitchOutsideDataLayer(Config.empty)

    @Test
    fun reducer_withContext_io_is_reported() {
        val code = """
            class HomeReducer {
                suspend fun reduce() = withContext(Dispatchers.IO) { load() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun presenter_launch_default_is_reported() {
        val code = """
            class HomePresenter {
                fun start() {
                    viewModelScope.launch(Dispatchers.Default) { compute() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun use_case_flowOn_is_reported() {
        val code = """
            class GetUserUseCase {
                operator fun invoke() = repository.user().flowOn(Dispatchers.IO)
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun injected_dispatcher_is_reported() {
        val code = """
            class UserRepository(private val ioDispatcher: CoroutineDispatcher, private val dispatchers: AppDispatchers) {
                suspend fun a() = withContext(ioDispatcher) { load() }
                suspend fun b() = withContext(dispatchers.io) { load() }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun named_and_combined_context_is_reported() {
        val code = """
            class HomeViewModel {
                fun start() {
                    scope.launch(start = CoroutineStart.LAZY, context = Dispatchers.IO) { load() }
                    scope.async(SupervisorJob() + (Dispatchers.IO + CoroutineName("x"))) { load() }
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun composable_effect_is_reported() {
        val code = """
            @Composable
            fun HomeScreen() {
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) { load() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun nested_object_inside_presenter_is_reported() {
        val code = """
            class HomePresenter {
                private val callback = object : Callback {
                    override suspend fun onEvent() = withContext(Dispatchers.IO) { load() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun data_layer_is_not_reported() {
        val code = """
            class UserRemoteDatasourceImpl {
                suspend fun fetch() = withContext(Dispatchers.IO) { client.get() }
            }

            class RoomUserDao {
                fun users() = query().flowOn(Dispatchers.IO)
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun main_and_non_dispatcher_contexts_are_not_reported() {
        val code = """
            class HomePresenter(private val mainDispatcher: CoroutineDispatcher, private val dispatchers: AppDispatchers) {
                suspend fun a() = withContext(Dispatchers.Main.immediate) { render() }
                suspend fun b() = withContext(mainDispatcher) { render() }
                suspend fun c() = withContext(dispatchers.main) { render() }
                suspend fun d() = withContext(NonCancellable) { save() }
                fun e() {
                    viewModelScope.launch { load() }
                    viewModelScope.launch(start = CoroutineStart.LAZY) { load() }
                    pickerLauncher.launch(request)
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun forbidden_class_suffixes_are_configurable() {
        val customRule = DispatcherSwitchOutsideDataLayer(TestConfig("forbiddenClassSuffixes" to listOf("Interactor")))
        val code = """
            class LoginInteractor {
                suspend fun run() = withContext(Dispatchers.IO) { load() }
            }

            class LoginUseCase {
                suspend fun run() = withContext(Dispatchers.IO) { load() }
            }
        """.trimIndent()

        assertEquals(1, customRule.lint(code).size)
    }
}
