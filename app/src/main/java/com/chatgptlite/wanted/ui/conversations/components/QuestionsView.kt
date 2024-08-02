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
    onQuestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
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
                QuestionButton(
                    question = question,
                    onQuestionSelected = onQuestionSelected,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
fun QuestionButton(
    question: String,
    onQuestionSelected: (String) -> Unit,
    isDarkTheme: Boolean
) {
    val backgroundColor = if (isDarkTheme) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val contentColor = if (isDarkTheme) {
        Color.Black
    } else {
        MaterialTheme.colorScheme.surface
    }

    Button(
        onClick = { onQuestionSelected(question) },
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .width(280.dp)
            .padding(horizontal = 2.dp, vertical = 4.dp)
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}