package com.chatgptlite.wanted.ui.conversations.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel
import com.chatgptlite.wanted.constants.SessionManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AgentIdView(viewModel: ConversationViewModel,  showAgent: Boolean ,isDarkTheme :Boolean) {
    val context = LocalContext.current as? Activity

    val agentId = SessionManager.selectedAgentId
    val additionalText: String = SessionManager.agents.entries
        .find { it.value.name == agentId }
        ?.value
        ?.description ?: ""

    val currentConversationId by viewModel.currentConversationState.collectAsState()
    val agentColor = if (isDarkTheme){
       Color.White
    }else{
       Color.Black
    }
    // Only render the content if agentId is not empty and currentConversationId is empty
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 30.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Text(
                text = agentId,
                style = MaterialTheme.typography.bodyMedium,
                color = agentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = additionalText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
}
