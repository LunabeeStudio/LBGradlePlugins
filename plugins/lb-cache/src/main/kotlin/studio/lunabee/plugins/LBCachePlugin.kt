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
 * Created by Lunabee Studio / Date - 1/12/2026
 * Last modified 2/7/25, 11:39 AM
 */

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Provider
import org.gradle.caching.http.HttpBuildCache
import java.net.URI

/**
 * This plugin allows you to configure the Lunabee remote Gradle cache.
 * It uses Gradle properties for customization with default values for read-only access:
 *  • Boolean `studio.lunabee.cacheEnableLocal` Enable the local cache. Default true.
 *  • Boolean `studio.lunabee.cacheEnableRemote` Enable the remote cache. Default true.
 *  • Boolean `studio.lunabee.cacheIsPush` Enable cache push to server (CI only). Default false.
 *  • String `studio.lunabee.cacheUsername` Cache credential username. Default to read-only credentials.
 *  • String `studio.lunabee.cachePassword` Cache credential password. Default to read-only credentials.
 */
class LBCachePlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        val cacheEnableLocal: Provider<String> = target.providers.gradleProperty("studio.lunabee.cacheEnableLocal")
        val cacheEnableRemote: Provider<String> = target.providers.gradleProperty("studio.lunabee.cacheEnableRemote")
        val cacheIsPush: Provider<String> = target.providers.gradleProperty("studio.lunabee.cacheIsPush")
        val cacheUsername: Provider<String> = target.providers.gradleProperty("studio.lunabee.cacheUsername")
        val cachePassword: Provider<String> = target.providers.gradleProperty("studio.lunabee.cachePassword")

        target.buildCache {
            local {
                isEnabled = cacheEnableLocal.orNull?.toBoolean() ?: true
            }
            remote(HttpBuildCache::class.java) {
                isEnabled = cacheEnableRemote.orNull?.toBoolean() ?: true
                url = URI.create("http://lunabees-mac-studio.local:5071/cache")
                credentials {
                    username = cacheUsername.orNull ?: "lunabee"
                    password = cachePassword.orNull ?: "MF%f^Q[u7{n8-5z;pdH2U#"
                }
                isAllowInsecureProtocol = true
                isPush = cacheIsPush.orNull?.toBoolean() ?: false
            }
        }
    }
}
