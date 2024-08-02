package com.chatgptlite.wanted.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.ViewGroup
import android.widget.EditText
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.util.TypedValue
import android.widget.LinearLayout
import androidx.compose.ui.graphics.Color
import com.google.android.material.textfield.TextInputEditText
import androidx.core.view.ViewCompat
import com.chatgptlite.wanted.R
import com.google.android.material.textfield.TextInputLayout

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = Purple80,
    tertiary = PrimaryColor,
    tertiaryContainer = AgentButton,
    background = barColor,
    surfaceTint = ConversationColor,
    surface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = BackGroundColor,
    tertiary =  PrimaryColor,
    surfaceTint =  GhostWhite,
    background = Color.White,
    tertiaryContainer = darkWhite,
    surface = BackGroundColor,
)

@Composable
fun ChatGPTLiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            (view.context as Activity).window.statusBarColor = colorScheme.background.toArgb()
//            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
//        }
//    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}





fun TextInputLayout.applyOuterStyle(context: Context) {
    val params = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    params.setMargins(
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt(),
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, context.resources.displayMetrics).toInt(),
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt(),
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, context.resources.displayMetrics).toInt()
    )
    this.layoutParams = params
}

fun TextInputEditText.applyInnerStyle(context: Context) {
    this.layoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    this.setTextColor(context.getColor(R.color.black))
    this.setHintTextColor(context.getColor(R.color.black))
    this.maxLines = 1
    this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    this.isSingleLine = true
    this.setPadding(
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt(),
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt(),
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt(),
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt()
    )
    this.minHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics).toInt()
}

// Extension function to convert Dp to pixels
fun Dp.toPx(context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (this.value * density).toInt()
}

// Extension function to convert TextUnit with Sp type to pixels
fun TextUnit.toPx(context: Context): Float {
    return if (this.type == TextUnitType.Sp) {
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        this.value * scaledDensity
    } else {
        this.value
    }
}
