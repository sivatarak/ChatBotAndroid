import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chatgptlite.wanted.R
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel

@Composable
fun getColors(): Pair<Color, Color> {
    val isDarkTheme = isSystemInDarkTheme()
    val buttonColor = if (isDarkTheme) {
        colorResource(id = R.color.button_surface_light)
    } else {
        colorResource(id = R.color.button_surface_dark)
    }
    val buttonTextColor = if (isDarkTheme) {
        colorResource(id = R.color.button_text_light)
    } else {
        colorResource(id = R.color.button_text_dark)
    }
    return Pair(buttonColor, buttonTextColor)
}

@Composable
fun ThemedButton(onClick: () -> Unit, isVisible : Boolean ) {
    val (buttonColor, buttonTextColor) = getColors()

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = buttonColor.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth(),
        enabled = isVisible
    ) {
        Text(
            text = "Sign In",
            color = if (isVisible) buttonTextColor else buttonTextColor.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun EmailTextField(value: String, onValueChange: (String) -> Unit, isVisible: Boolean) {
    val (buttonColor, buttonTextColor) = getColors()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Type your Username", color = buttonColor) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        singleLine = true,
        enabled = isVisible, // Controls if the text field is editable
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = buttonColor,
            unfocusedBorderColor = buttonColor,
            focusedLabelColor = buttonTextColor,
            unfocusedLabelColor = buttonTextColor,
            cursorColor = buttonColor,
            focusedTextColor = buttonColor,
            unfocusedTextColor = buttonColor,
            disabledBorderColor = buttonColor.copy(alpha = 0.5f),
            disabledLabelColor = buttonTextColor.copy(alpha = 0.5f),
            disabledTextColor = buttonColor.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun PasswordTextField(value: String, onValueChange: (String) -> Unit, isVisible: Boolean) {
    val (buttonColor, buttonTextColor) = getColors()
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Type Your Password", color = buttonColor) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        singleLine = true,
        enabled = isVisible, // Controls if the text field is editable
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = buttonColor,
            unfocusedBorderColor = buttonColor,
            focusedLabelColor = buttonTextColor,
            unfocusedLabelColor = buttonTextColor,
            cursorColor = buttonColor,
            focusedTextColor = buttonColor,
            unfocusedTextColor = buttonColor,
            disabledBorderColor = buttonColor.copy(alpha = 0.5f),
            disabledLabelColor = buttonTextColor.copy(alpha = 0.5f),
            disabledTextColor = buttonColor.copy(alpha = 0.5f)
        ),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = isVisible) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = if (isVisible) buttonColor else buttonColor.copy(alpha = 0.5f)
                )
            }
        }
    )
}


@Composable
fun CustomCircularProgressScreen(isVisible: Boolean) {
    // Ensure the Box only has one child to avoid layout issues
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Display the loading indicator only if isVisible is true
        if (isVisible) {
            CustomCircularProgressAnimated()
        }
    }
}

@Composable
fun CustomCircularProgressAnimated() {
    val (buttonColor, _) = getColors()

    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.size(64.dp)) {
        // Draw the rotating arc
        drawArc(
            color = buttonColor,
            startAngle = rotation,
            sweepAngle = 270f, // Sweep angle less than 360 to create an arc
            useCenter = false,
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}


    //@Composable
//fun IndeterminateCircularIndicator(isVisible: Boolean) {
//    if (isVisible) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
//            val rotation by infiniteTransition.animateFloat(
//                initialValue = 0f,
//                targetValue = 360f,
//                animationSpec = infiniteRepeatable(
//                    animation = tween(1000, easing = LinearEasing),
//                    repeatMode = RepeatMode.Restart
//                ),
//                label = "rotation animation"
//            )
//
//            Canvas(modifier = Modifier
//                .size(64.dp)
//                .graphicsLayer { this.rotationZ = rotation }
//            ) {
//                drawArc(
//                    color = Color.Red,
//                    startAngle = 0f,
//                    sweepAngle = 90f,
//                    useCenter = false,
//                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
//                )
//            }
//        }
//    }
//}
    @Composable
    fun SignInScreen(
        viewModel: ConversationViewModel,
        onSignInClick: (String, String) -> Unit
    ) {
        val email by viewModel.email.collectAsState()
        val password by viewModel.password.collectAsState()
        val isLoading by viewModel.progressLoad.collectAsState()
        val (buttonColor, buttonTextColor) = getColors()

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Please, Sign in to continue",
                        color = buttonColor,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                EmailTextField(
                    value = email,
                    onValueChange = { viewModel.updateEmail(it) },
                    isVisible = !isLoading // Disable when loading
                )
                Spacer(modifier = Modifier.height(24.dp))
                PasswordTextField(
                    value = password,
                    onValueChange = { viewModel.updatePassword(it) },
                    isVisible = !isLoading // Disable when loading
                )
                Spacer(modifier = Modifier.height(24.dp))
                ThemedButton(
                    onClick = { onSignInClick(email, password) },
                    isVisible = !isLoading // Disable when loading
                )
            }

            CustomCircularProgressScreen(isVisible = isLoading)
        }
    }

@Preview
@Composable
fun PreviewThemedButton() {
    ThemedButton(onClick = {}, isVisible = true) // or isVisible = false
}
