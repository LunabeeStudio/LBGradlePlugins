package studio.lunabee.plugins

/**
 * Localization provider
 */
sealed interface StringsProvider {
    /**
     * localise.biz provider
     *
     * @property key The key used to access the API
     */
    class Loco(val key: String) : StringsProvider
}
