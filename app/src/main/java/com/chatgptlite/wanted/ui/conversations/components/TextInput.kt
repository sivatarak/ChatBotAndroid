package com.chatgptlite.wanted.ui.conversations.components

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel
import kotlinx.coroutines.launch

@Composable
fun TextInput(
    selectedQuestion: String,
    conversationViewModel: ConversationViewModel = hiltViewModel(),
    isDarkTheme: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val isLoading by conversationViewModel.isLoading.collectAsState()
    val isInitializationFailed by conversationViewModel.isInitializationFailed.collectAsState()
    Log.d("TextInput", "Using ViewModel instance in textinput.kt: ${conversationViewModel.getInstanceId()}")
    print(isInitializationFailed)
    LaunchedEffect(selectedQuestion) {
        if (selectedQuestion.isNotEmpty()) {
            coroutineScope.launch {
                conversationViewModel.clearSelectedAgentId()
                conversationViewModel.sendMessage(selectedQuestion)
                conversationViewModel.setShowAgent(false)
            }
        }
    }

    TextInputIn(
        selectedQuestion = "",
        sendMessage = { text ->
            coroutineScope.launch {
                conversationViewModel.clearSelectedAgentId()
                conversationViewModel.sendMessage(text)
            }
        },
        isLoading = isLoading,
        isInitializationFailed = isInitializationFailed, // Pass the new flag,
        isDarkTheme = isDarkTheme
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextInputIn(
    selectedQuestion: String,
    sendMessage: (String) -> Unit,
    isLoading: Boolean,
    isDarkTheme: Boolean,
    isInitializationFailed: Boolean, // New parameter
    conversationViewModel: ConversationViewModel = hiltViewModel()
) {
    var text by remember { mutableStateOf(TextFieldValue()) }
    var micActive by remember { mutableStateOf(false) }
    val context = LocalContext.current as Activity

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(selectedQuestion) {
        if (selectedQuestion.isEmpty()) {
            text = TextFieldValue("")
        }
    }

    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column {
            Divider(Modifier.height(0.2.dp))
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .padding(top = 6.dp, bottom = 10.dp)
            ) {
                Row {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Ask me anything", fontSize = 12.sp) },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        enabled = !isLoading && !isInitializationFailed // Disable based on the flag
                    )
                    IconButton(
                        onClick = {
                            val textClone = text.text.toString()
                            text = TextFieldValue("")
                            if (textClone.isNotEmpty()) {
                                sendMessage(textClone)
                                conversationViewModel.setShowAgent(false)
                            }
                        },
                        enabled = !isLoading && !isInitializationFailed // Disable based on the flag
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            "sendMessage",
                            modifier = Modifier.size(26.dp),
                            tint = if (isLoading || isInitializationFailed) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        )
                    }

                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewTextInput() {
    TextInputIn(
        selectedQuestion = "",
        sendMessage = {},
        isLoading = false,
        isInitializationFailed = false,
        isDarkTheme = true
    )
}