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

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class LunabeeRuleSetProvider : RuleSetProvider {
    override val ruleSetId: RuleSetId = RuleSetId("lunabee")

    override fun instance(): RuleSet = RuleSet(
        ruleSetId,
        listOf(::DispatcherSwitchOutsideDataLayer),
    )
}
