package loginandsignup

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.ComponentActivity
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.lifecycle.lifecycleScope
import com.chatgptlite.wanted.MainActivity
import com.chatgptlite.wanted.constants.Agent
import com.chatgptlite.wanted.constants.QuestionReqData
import com.chatgptlite.wanted.constants.RetrofitInstance
import com.chatgptlite.wanted.constants.SessionManager
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.json.JSONObject

@Composable
fun AgentListScreen(viewModel: ConversationViewModel = viewModel(), isDarkTheme: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val allAgents = remember { SessionManager.agents.values.toList() }
    var filteredAgents by remember { mutableStateOf(allAgents) }

    LaunchedEffect(searchQuery.text) {
        filteredAgents = if (searchQuery.text.isEmpty()) {
            allAgents
        } else {
            allAgents.filter { it.name.contains(searchQuery.text, ignoreCase = true) }
        }
    }

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).padding(top = 16.dp)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onDismiss = onDismiss
            )
            Spacer(modifier = Modifier.height(30.dp))
            AgentList(filteredAgents, viewModel, isDarkTheme)
        }
    }
}

@Composable
fun SearchBar(query: TextFieldValue, onQueryChange: (TextFieldValue) -> Unit, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.extraLarge
            )
            .height(53.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            placeholder = { Text("Search Agents") },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}


@Composable
fun AgentList(agents: List<Agent>, viewModel: ConversationViewModel , isDarkTheme: Boolean) {
    val context = LocalContext.current
    LazyColumn {
        items(agents) { agent ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                //elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AgentItem(agent) {
                    onClickAgent(agent.name, viewModel, context ,isDarkTheme)
                }
            }
        }
    }
}

@Composable
fun AgentItem(agent: Agent, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer, // Set the background color for the button
            contentColor = MaterialTheme.colorScheme.surface  // Set the content color (text color) for the button
        )
    ) {
        Text(agent.name)
    }
}

private fun onClickAgent(agentId: String, viewModel: ConversationViewModel, context: Context,isDarkTheme: Boolean) {
    val reqAgentId = SessionManager.agents.entries.find { it.value.name == agentId }?.key
    getQuestionCards(agentId)
    (context as? ComponentActivity)?.lifecycleScope?.launch {
        startNewConversation(agentId, viewModel, context,isDarkTheme)

        val initResponseCode = SignInActivity.SignInUtils.initialInstances(reqAgentId ?: "")
        if (initResponseCode == 200) {
            startNewConversation(agentId, viewModel, context,isDarkTheme)
        } else {
            showInitializationFailedToast(context)
            startNewConversation(agentId, viewModel, context,isDarkTheme)
            viewModel.setInitializationFailed(true)
            println("Initialization failed with code: $initResponseCode")
        }
    }
}

 fun getQuestionCards(agentId: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val reqAgentId = SessionManager.agents.entries.find { it.value.name == agentId }?.key
            if (reqAgentId != null) {
                println("Requesting question cards for agentId: $reqAgentId")
                val data = QuestionReqData(
                    sessionId = SessionManager.sessionId,
                    agentId = reqAgentId
                )
                val response = RetrofitInstance.apiService.getQuestionCards(data).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    println("Response Body: $responseBody")
                    val jsonResponse = JSONObject(responseBody)
                    val questionsJsonArray = jsonResponse.getJSONArray("questions")
                    val questions = mutableListOf<String>()
                    for (i in 0 until questionsJsonArray.length()) {
                        questions.add(questionsJsonArray.getString(i))
                    }
                    println("Question cards are: $questions")
                    SessionManager.questions = questions
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("Response was not successful. Error body: $errorBody")
                }
            } else {
                println("Agent not found with name: $agentId")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("An error occurred: ${e.message}")
        }
    }
}

private fun showInitializationFailedToast(context: Context) {
    Toast.makeText(context, "Initialization failed", Toast.LENGTH_SHORT).show()
}

private suspend fun startNewConversation(agentId: String, viewModel: ConversationViewModel, context: Context,isDarkTheme: Boolean) {
    Log.d("AgentListScreen", "Starting new conversation with agentId: $agentId")
    SessionManager.selectedAgentId = agentId
    delay(100)
    openMainActivity(agentId, context,isDarkTheme)
}

private fun openMainActivity(agentId: String, context: Context,isDarkTheme: Boolean) {
    Log.d("AgentListScreen", "Opening MainActivity with agent ID: $agentId")
    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra("agentId", agentId)
    intent.putExtra("showAgent", true)
    intent.putExtra("isDarkTheme", isDarkTheme)
    context.startActivity(intent)
    (context as? ComponentActivity)?.finish()
}
