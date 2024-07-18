package com.chatgptlite.wanted

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.core.view.WindowCompat
import com.chatgptlite.wanted.ui.common.AppBar
import com.chatgptlite.wanted.ui.common.AppScaffold
import com.chatgptlite.wanted.ui.common.LoadingAnimation
import com.chatgptlite.wanted.ui.conversations.Conversation
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel
import com.chatgptlite.wanted.ui.theme.ChatGPTLiteTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val conversationViewModel: ConversationViewModel by viewModels()
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val extras = intent.extras
        val agentID = extras?.getString("agentId")
        val launchNewChat = extras?.getBoolean("launchNewChat")
        val showAgentView = extras?.getBoolean("showAgent") ?: false
        agentID?.let { conversationViewModel.updateSelectedAgentId(it)
        conversationViewModel.clearConversation()
        conversationViewModel.currentConversationState}
        launchNewChat?.let { conversationViewModel.updateIsNewChat(it) }
        conversationViewModel.newConversation()
        conversationViewModel.setShowAgent(showAgentView)
        setContentView(
            ComposeView(this).apply {
                consumeWindowInsets = false
                setContent {
                        MainContent(mainViewModel, conversationViewModel)
                }

            }

        )
    }
}

@Composable
fun MainContent(mainViewModel: MainViewModel, conversationViewModel: ConversationViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerOpen by mainViewModel.drawerShouldBeOpened.collectAsState()
    Log.d("AgentListFragment", "Using ViewModel instance in mainActivity: ${conversationViewModel.getInstanceId()}")


    if (drawerOpen) {
        // Open drawer and reset state in VM.
        LaunchedEffect(Unit) {
            // wrap in try-finally to handle interruption whiles opening drawer
            try {
                drawerState.open()
            } finally {
                mainViewModel.resetOpenDrawerAction()
            }
        }
    }

    // Intercepts back navigation when the drawer is open
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        } else {
            focusManager.clearFocus()
        }
    }

    val darkTheme = remember { mutableStateOf(true) }
    val isLoading by conversationViewModel.isLoading.collectAsState()

    ChatGPTLiteTheme(darkTheme.value) {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
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
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    AppBar(onClickMenu = {
                        scope.launch {
                            drawerState.open()
                        }
                    })
                    Divider()
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Conversation()

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                // Optional: semi-transparent background to emphasize loading

                            ) {
                                LoadingAnimation()
                            }
                        }else {
                            Conversation()
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
