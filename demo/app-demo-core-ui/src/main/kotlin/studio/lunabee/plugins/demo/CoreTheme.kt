package studio.lunabee.plugins.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun CoreTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme {
        content()
    }
}
