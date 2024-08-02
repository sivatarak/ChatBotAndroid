package com.chatgptlite.wanted.ui.common

import android.annotation.SuppressLint
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel
import com.chatgptlite.wanted.ui.theme.BackGroundColor
import kotlinx.coroutines.launch

//import androidx.compose.material3.ModalDrawerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    drawerState: DrawerState = rememberDrawerState(initialValue = Closed),
    onChatClicked: (String) -> Unit,
    onNewChatClicked: () -> Unit,
    onIconClicked: () -> Unit = {},
    onThemeChanged: (Boolean) -> Unit, // Add this line
    conversationViewModel: ConversationViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController() // Initialize navController here
    val systemDarkTheme = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemDarkTheme) }

    LaunchedEffect(Unit) {
        conversationViewModel.initialize()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = BackGroundColor) {
                AppDrawer(
                    onChatClicked = onChatClicked,
                    onNewChatClicked = onNewChatClicked,
                    onIconClicked = onIconClicked,
                    navController = navController, // Pass navController here
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = { newTheme ->
                        isDarkTheme = newTheme
                    },
                )
            }
        },
        content = content
    )
}