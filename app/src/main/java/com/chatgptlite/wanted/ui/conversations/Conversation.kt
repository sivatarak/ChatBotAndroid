package com.chatgptlite.wanted.ui.conversations

import android.app.Activity
import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatgptlite.wanted.constants.conversationTestTag
import com.chatgptlite.wanted.models.MessageModel
import com.chatgptlite.wanted.ui.conversations.components.AgentIdView
import com.chatgptlite.wanted.ui.conversations.components.MessageCard
import com.chatgptlite.wanted.ui.conversations.components.QuestionCardsRow
import com.chatgptlite.wanted.ui.conversations.components.TextInput
import com.chatgptlite.wanted.ui.conversations.ui.theme.ChatGPTLiteTheme
import com.chatgptlite.wanted.ui.theme.BackGroundColor
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import com.chatgptlite.wanted.ui.theme.ConversationColor
import com.chatgptlite.wanted.ui.theme.GhostWhite

@Composable
fun Conversation(isDarkTheme: Boolean) {
    ChatGPTLiteTheme(darkTheme = isDarkTheme) {
        val color = if (isDarkTheme) {
            ConversationColor
        } else {
            GhostWhite
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = color
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .imePadding() // Padding for keyboard
            ) {
                val conversationViewModel: ConversationViewModel = hiltViewModel()
                val context = LocalContext.current as? Activity
                val showAgent by conversationViewModel.isShowAgent.collectAsState()
                var selectedQuestion by rememberSaveable { mutableStateOf("") }

                if (showAgent) {
                    AgentIdView(showAgent = showAgent, viewModel = conversationViewModel,isDarkTheme = isDarkTheme)
                }

                MessageList(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    conversationViewModel = conversationViewModel
                )

                if (showAgent) {
                    QuestionCardsRow(
                        isDarkTheme = isDarkTheme,
                        viewModel = conversationViewModel,
                        showAgent = true, // Set to true to show the question cards
                        onQuestionSelected = { question ->
                            selectedQuestion = question
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally) // Aligns the component at the bottom start of the parent
                            .padding(bottom = 16.dp) // Add padding around the component
                    )
                }

                TextInput(
                    selectedQuestion = selectedQuestion,
                    conversationViewModel = conversationViewModel,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    conversationViewModel: ConversationViewModel  = hiltViewModel(),
) {
    val listState = rememberLazyListState()
    val conversationId by conversationViewModel.currentConversationState.collectAsState()
    val messagesMap by conversationViewModel.messagesState.collectAsState()
    val isFabExpanded by conversationViewModel.isFabExpanded.collectAsState()

    val messages: List<MessageModel> =
        if (messagesMap[conversationId] == null) listOf() else messagesMap[conversationId]!!

    Box(modifier = modifier) {
        LazyColumn(
            contentPadding =
            WindowInsets.statusBars.add(WindowInsets(top = 90.dp)).asPaddingValues(),
            modifier = Modifier
                .testTag(conversationTestTag)
                .fillMaxSize(),
            reverseLayout = true,
            state = listState,
        ) {
            items(messages.size) { index ->
                Box(modifier = Modifier.padding(bottom = if (index == 0) 10.dp else 0.dp)) {
                    Column {
                        MessageCard(
                            message = messages[index],
                            isLast = index == messages.size - 1,
                            isHuman = true
                        )
                        MessageCard(message = messages[index])
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    ChatGPTLiteTheme {
        // Preview content
    }
}
