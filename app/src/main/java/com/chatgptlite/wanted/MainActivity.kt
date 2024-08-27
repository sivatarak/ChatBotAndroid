package com.chatgptlite.wanted

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chatgptlite.wanted.constants.Agent
import com.chatgptlite.wanted.constants.SessionManager
import com.chatgptlite.wanted.ui.common.AppBar
import com.chatgptlite.wanted.ui.common.AppScaffold
import com.chatgptlite.wanted.ui.common.LoadingAnimation
import com.chatgptlite.wanted.ui.conversations.Conversation
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel
import com.chatgptlite.wanted.ui.theme.ChatGPTLiteTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import loginandsignup.AgentListScreen
import loginandsignup.SettingsScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val conversationViewModel: ConversationViewModel by viewModels()
    private var darkTheme: Boolean = false

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the system's current theme
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        // Set darkTheme based on the system's theme
        darkTheme = when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val extras = intent.extras
        val agentID = extras?.getString("agentId")
        val launchNewChat = extras?.getBoolean("launchNewChat")
        val showAgentView = extras?.getBoolean("showAgent") ?: false
        darkTheme = extras?.getBoolean("isDarkTheme") ?: darkTheme // Use the system theme if not provided in extras
        agentID?.let {
            conversationViewModel.updateSelectedAgentId(it)
            conversationViewModel.clearConversation()
            conversationViewModel.currentConversationState
        }
        launchNewChat?.let {
            conversationViewModel.updateIsNewChat(it)
        }
        conversationViewModel.newConversation()
        conversationViewModel.setShowAgent(showAgentView)

        setContentView(
            ComposeView(this).apply {
                consumeWindowInsets = false
                conversationViewModel.setShowAgent(true)
                setContent {
                    MainContent(mainViewModel, conversationViewModel, darkTheme) { newTheme ->
                        darkTheme = newTheme
                    }
                }
            }
        )
    }
}



@Composable
fun MainContent(mainViewModel: MainViewModel, conversationViewModel: ConversationViewModel, darkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerOpen by mainViewModel.drawerShouldBeOpened.collectAsState()
    val settingsOpen by mainViewModel.settingsScreenOpen.collectAsState()
    val agentScreenOpen by mainViewModel.agentScreenOpen.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    Log.d("AgentListFragment", "Using ViewModel instance in mainActivity: ${conversationViewModel.getInstanceId()}")

    if (drawerOpen) {
        LaunchedEffect(Unit) {
            try {
                drawerState.open()
            } finally {
                mainViewModel.resetOpenDrawerAction()
            }
        }
    }

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        } else {
            focusManager.clearFocus()
        }
    }

    val darkTheme = remember { mutableStateOf(darkTheme) }
    val isLoading by conversationViewModel.isLoading.collectAsState()

    ChatGPTLiteTheme(darkTheme.value) {
        Surface(color = MaterialTheme.colorScheme.background) {
            if (settingsOpen) {
                LaunchedEffect(Unit) {
                    scope.launch {
                        drawerState.close()
                      //  mainViewModel.closeSettingsScreen()
                    }
                }
                SettingsScreen(
                    isDarkTheme = darkTheme.value,
                    onDismiss = {
                        mainViewModel.closeSettingsScreen()
                        scope.launch {
                            drawerState.open()
                            //  mainViewModel.closeSettingsScreen()
                        }
                    }
                )
            }else if (agentScreenOpen){
                LaunchedEffect(Unit) {
                    scope.launch {
                        drawerState.close()
                        //  mainViewModel.closeSettingsScreen()
                    }
                }
                AgentListScreen(
                    isDarkTheme = darkTheme.value,
                    onDismiss = {
                         mainViewModel.agentsScreenClose()
                        scope.launch {
                            drawerState.open()

                        }
                    }
                )
            }

            else {
                AppScaffold(
                    drawerState = drawerState,
                    onChatClicked = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onNewChatClicked = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onIconClicked = {
                        darkTheme.value = !darkTheme.value
                    },
                    onThemeChanged = { newTheme ->  // Add this line
                        darkTheme.value = newTheme  // Add this line
                        onThemeChange(newTheme)     // Add this line
                    }
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AppBar(isDarkTheme = darkTheme.value, onClickMenu = {
                            scope.launch {
                                drawerState.open()
                            }
                        })
                        Divider()
                        Box(modifier = Modifier.fillMaxSize()) {
                            Conversation(isDarkTheme = darkTheme.value)

                            if (isLoading) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    LoadingAnimation(
                                        modifier = Modifier.padding(70.dp),
                                        isDarkTheme = darkTheme.value
                                    )
                                }
                            } else {
                                Conversation(isDarkTheme = darkTheme.value)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChatGPTLiteTheme {
    }
}

//this is new code
