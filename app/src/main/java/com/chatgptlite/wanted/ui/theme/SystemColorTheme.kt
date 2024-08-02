import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chatgptlite.wanted.R

@Composable
fun ThemedButton(onClick: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val buttonColor = if (isDarkTheme) {
        colorResource(id = R.color.button_surface_dark)
    } else {
        colorResource(id = R.color.button_surface_light)
    }
    val buttonTextColor = if (isDarkTheme) {
        colorResource(id = R.color.button_text_dark)
    } else {
        colorResource(id = R.color.button_text_light)
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Sign In",
            color = buttonTextColor,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview
@Composable
fun PreviewThemedButton() {
    ThemedButton(onClick = {})
}
