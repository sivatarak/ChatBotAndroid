package com.chatgptlite.wanted.ui.common

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.chatgptlite.wanted.MainViewModel
import com.chatgptlite.wanted.constants.SessionManager
import com.chatgptlite.wanted.constants.SessionManager.agents
import com.chatgptlite.wanted.constants.urlToImageAppIcon
import com.chatgptlite.wanted.models.ConversationModel
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel
import com.codingstuff.loginandsignup.MainActivity
import kotlinx.coroutines.launch
import loginandsignup.SettingsScreen

@Composable
fun AppDrawer(
    onChatClicked: (String) -> Unit,
    onNewChatClicked: () -> Unit,
    conversationViewModel: ConversationViewModel = hiltViewModel(),
    onIconClicked: () -> Unit = {},
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    navController: NavHostController, // Added NavController parameter
    mainViewModel: MainViewModel = hiltViewModel() // Ensure MainViewModel is used here
) {
    val coroutineScope = rememberCoroutineScope()
    val currentConversationState = conversationViewModel.currentConversationState.collectAsState().value
    val showAgent = conversationViewModel.isShowAgent.collectAsState()
    Log.d(ContentValues.TAG, "currentConversationStateinAppDrawer: ${currentConversationState}")

    AppDrawerIn(
        onChatClicked = onChatClicked,
        onNewChatClicked = onNewChatClicked,
        onIconClicked = onIconClicked,
        conversationViewModel = {
            coroutineScope.launch {
                conversationViewModel.newConversation()
            }
        },
        deleteConversation = { conversationId ->
            coroutineScope.launch {
                conversationViewModel.deleteConversation(conversationId)
                conversationViewModel.newConversation()
            }
        },
        onConversation = { conversationModel: ConversationModel ->
            conversationViewModel.setShowAgent(false)
            coroutineScope.launch {

                conversationViewModel.onConversation(conversationModel)
            }
        },
        onSettingsClicked = { mainViewModel.settingsScreenOpen() },
        currentConversationState = conversationViewModel.currentConversationState.collectAsState().value,
        conversationState = conversationViewModel.conversationsState.collectAsState().value,
        navController = navController, // Pass NavController to AppDrawerIn
        isDarkTheme = isDarkTheme,
        onThemeChanged = onThemeChanged,

    )
}

@Composable
private fun AppDrawerIn(
    onChatClicked: (String) -> Unit,
    onNewChatClicked: () -> Unit,
    onIconClicked: () -> Unit,
    conversationViewModel: () -> Unit,
    deleteConversation: (String) -> Unit,
    onConversation: (ConversationModel) -> Unit,
    onSettingsClicked: () -> Unit,
    currentConversationState: String,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    conversationState: MutableList<ConversationModel>,
    navController: NavHostController? = null, // Pass NavController parameter
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
  //  val isDarkTheme = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
   // val isDarkTheme = isSystemInDarkTheme()
    Log.d(ContentValues.TAG, "the darktheme value is ${isDarkTheme}")
   // var showSettings by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        DrawerHeader(clickAction = onIconClicked,isDarkTheme,onThemeChanged = onThemeChanged)

        // Button to navigate to Agents screen
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(16.dp)
        ) {
            Button(
                onClick = {
                    // Launch the AgentListActivity
                       mainViewModel.agentsScreenOpen()
//                    val intent = Intent(context, AgentListFragment::class.java).apply {
//                        putExtra("isDarkTheme", isDarkTheme)
//                    }
//                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = Color.Green
                )
            ) {
                Text(
                    text = "Agents",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }

        DividerItem()
        DrawerItemHeader("Chats")
        ChatItem("New Chat", Icons.Outlined.AddComment, false) {
            onNewChatClicked()
            conversationViewModel()
        }
        HistoryConversations(
            onChatClicked,
            deleteConversation,
            onConversation,
            currentConversationState,
            conversationState
        )
        DividerItem(modifier = Modifier.padding(horizontal = 28.dp))
        DrawerItemHeader("Settings")
        ChatItem("Settings", Icons.Filled.Settings, false) {
           onSettingsClicked()
        }

    }
}




@Composable
private fun DrawerHeader(clickAction: () -> Unit = {},isDarkTheme: Boolean,onThemeChanged: (Boolean) -> Unit) {
    val paddingSizeModifier = Modifier
        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
        .size(34.dp)

    Row(verticalAlignment = CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f), verticalAlignment = CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(urlToImageAppIcon),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = paddingSizeModifier
                    .then(
                        Modifier
                            .background(MaterialTheme.colorScheme.background) // Set your desired background color
                            .clip(RoundedCornerShape(6.dp))
                    )
            )


            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    "ChatBot",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface,
                )
            }
        }

        IconButton(
            onClick = {
                onThemeChanged(!isDarkTheme)
                clickAction.invoke()
            },
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Filled.WbSunny  else Icons.Filled.DarkMode,
                contentDescription = if (isDarkTheme) "Dark Mode" else "Light Mode",
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

@Composable
fun LoginScreen() {
    // Implement your login UI here
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login Screen", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Your login UI components go here
    }
}

@Composable
fun AgentsScreen(agentIds: Map<String, String>, navigateBack: () -> Unit) {
    // Example implementation
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Agents Screen", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        agentIds.forEach { (_, value) ->
            Text(text = value, fontSize = 16.sp)
        }

        // Button to navigate back
        Button(onClick = { navigateBack() }) {
            Text(text = "Back to Previous Screen")
        }
    }
}

@Composable
private fun ColumnScope.HistoryConversations(
    onChatClicked: (String) -> Unit,
    deleteConversation: (String) -> Unit,
    onConversation: (ConversationModel) -> Unit,
    currentConversationState: String,
    conversationState: List<ConversationModel>
) {
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<ConversationModel?>(null) }



    LazyColumn(
        Modifier
            .fillMaxWidth()
            .weight(1f, false),
    ) {
        items(conversationState.size) { index ->
            RecycleChatItem(
                text = conversationState[index].title,
                icon = Icons.Filled.Message,
                selected = conversationState[index].id == currentConversationState,
                onChatClicked = {
                    onChatClicked(conversationState[index].id)

                    scope.launch {
                        onConversation(conversationState[index])
                    }
                },
                onDeleteClicked = {
                    conversationToDelete = conversationState[index]
                    showDeleteDialog = true

                }
            )
            if (showDeleteDialog && conversationToDelete != null) {
                DeleteConfirmationDialog(
                    onConfirm = {
                        scope.launch {
                            deleteConversation(conversationState[index].id)
                        }
                        showDeleteDialog = false
                        conversationToDelete = null
                    },
                    onDismiss = {
                        showDeleteDialog = false
                        conversationToDelete = null
                    }
                )
            }
        }
    }


}

@Composable
private fun DrawerItemHeader(text: String) {
    Box(
        modifier = Modifier
            .heightIn(min = 52.dp)
            .padding(horizontal = 28.dp),
        contentAlignment = CenterStart
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChatItem(
    text: String,
    icon: ImageVector = Icons.Filled.Edit,
    selected: Boolean,
    onChatClicked: () -> Unit
) {
    val background = if (selected) {
        Modifier.background(MaterialTheme.colorScheme.tertiaryContainer)
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(CircleShape)
            .then(background)
            .clickable(onClick = onChatClicked),
        verticalAlignment = CenterVertically
    ) {
        val iconTint = if (selected) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        Icon(
            icon,
            tint = iconTint,
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                .size(25.dp),
            contentDescription = null,
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(start = 12.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


@Composable
private fun RecycleChatItem(
    text: String,
    icon: ImageVector = Icons.Filled.Edit,
    selected: Boolean,
    onChatClicked: () -> Unit,
    onDeleteClicked: () -> Unit,

) {
    val background = if (selected) {
        Modifier.background(MaterialTheme.colorScheme.surfaceTint)
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
            .clip(CircleShape)
            .then(background)
            .clickable(onClick = onChatClicked),
        verticalAlignment = CenterVertically
    ) {
        val iconTint = if (selected) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        Icon(
            icon,
            tint = iconTint,
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                .size(25.dp),
            contentDescription = null,
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier
                .padding(start = 12.dp)
                .fillMaxWidth(0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(0.9f, true))
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Delete",
            tint = if (selected) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier
                .padding(
                    end = 12.dp
                )
                .clickable { onDeleteClicked() }
        )

    }
}

@Composable
fun DividerItem(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(
                "Confirm Deletion",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                "Are you sure you want to delete this conversation?",
                color = MaterialTheme.colorScheme.surface,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("No")
            }
        }
    )
}
@Composable
fun PreviewAppDrawerIn() {
    val navController = rememberNavController() // Add NavController to Preview
    var isDarkTheme by remember { mutableStateOf(false) }
    AppDrawerIn(
        onChatClicked = {},
        onNewChatClicked = {},
        onIconClicked = {},
        conversationViewModel = {},
        deleteConversation = {},
        conversationState = mutableListOf(),
        currentConversationState = String(),
        onConversation = { _: ConversationModel -> },
        navController = navController, // Pass NavController to AppDrawerIn
        onSettingsClicked = {},
        isDarkTheme = isDarkTheme,
        onThemeChanged = { newTheme ->
            isDarkTheme = newTheme
            // Here you would typically update the app's theme
        }
    )
}
