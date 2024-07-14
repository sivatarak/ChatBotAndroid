package com.chatgptlite.wanted.ui.conversations.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chatgptlite.wanted.constants.SessionManager
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel

@Composable
fun QuestionCardsRow(
    viewModel: ConversationViewModel,
    showAgent: Boolean,
    onQuestionSelected: (String) -> Unit, // Callback to handle question selection
    modifier: Modifier = Modifier
) {
    if (showAgent) {
        val questions = SessionManager.questions

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            items(questions) { question ->
                QuestionButton(question = question, onQuestionSelected = onQuestionSelected)
            }
        }
    }
}

@Composable
fun QuestionButton(question: String, onQuestionSelected: (String) -> Unit) {
    Button(
        onClick = { onQuestionSelected(question) }, // Handle button click to update text input
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .width(250.dp)
            .padding(horizontal = 2.dp, vertical = 4.dp)
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
