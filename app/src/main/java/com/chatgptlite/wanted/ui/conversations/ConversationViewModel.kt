@file:Suppress("UNCHECKED_CAST")

package com.chatgptlite.wanted.ui.conversations

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.io.IOException
import java.net.SocketTimeoutException
import androidx.lifecycle.ViewModel
import com.chatgptlite.wanted.constants.ApiRequest
import com.chatgptlite.wanted.constants.Data
import com.chatgptlite.wanted.constants.RetrofitInstance
import com.chatgptlite.wanted.constants.SessionManager
import com.chatgptlite.wanted.data.remote.ConversationRepository
import com.chatgptlite.wanted.data.remote.MessageRepository
import com.chatgptlite.wanted.data.remote.OpenAIRepositoryImpl
import com.chatgptlite.wanted.models.*
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/**
 * Used to communicate between screens.
 */

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val conversationRepo: ConversationRepository,

    private val messageRepo: MessageRepository,
    private val openAIRepo: OpenAIRepositoryImpl,
) : ViewModel() {
    val _currentConversation: MutableStateFlow<String> =
        MutableStateFlow(Date().time.toString())

    private val instanceId = UUID.randomUUID().toString()

    init {
        Log.d("ConversationViewModel", "Created new instance with ID: $instanceId")
    }

    fun getInstanceId(): String = instanceId

    private val _conversations: MutableStateFlow<MutableList<ConversationModel>> = MutableStateFlow(
        mutableListOf()
    )
    private val _messages: MutableStateFlow<HashMap<String, MutableList<MessageModel>>> =
        MutableStateFlow(HashMap())
    private val _isFetching: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _isFabExpanded = MutableStateFlow(false)

    val currentConversationState: StateFlow<String> = _currentConversation.asStateFlow()
    val conversationsState: StateFlow<MutableList<ConversationModel>> = _conversations.asStateFlow()
    val messagesState: StateFlow<HashMap<String, MutableList<MessageModel>>> =
        _messages.asStateFlow()
    public val _isLoading = MutableStateFlow(false)
    public val isLoading: StateFlow<Boolean> get() = _isLoading
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()


    private val _isShowAgent = MutableStateFlow(false)
    val isShowAgent: StateFlow<Boolean> get() = _isShowAgent

    private val _isInitializationFailed = MutableStateFlow(false)
    val isInitializationFailed: StateFlow<Boolean> = _isInitializationFailed.asStateFlow()


    private val _progressLoad = MutableStateFlow(false)
    val progressLoad: StateFlow<Boolean> = _progressLoad

    fun setLoading(isLoading: Boolean) {
        _progressLoad.value = isLoading
    }


    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }


    fun setInitializationFailed(isFailed: Boolean) {
        _isInitializationFailed.value = isFailed
    }
    fun clearConversation() {
        _currentConversation.value = ""
    }
    // Function to update _isShowAgent
    fun setShowAgent(showAgent: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _isShowAgent.value = showAgent
            }
        }
    }
    val isFabExpanded: StateFlow<Boolean> get() = _isFabExpanded
    private val _selectedAgentId = MutableStateFlow("")

    val selectedAgentId: StateFlow<String> get() = _selectedAgentId

    private val _isNewChat = MutableStateFlow(false)
    val isNewChat: StateFlow<Boolean> get() = _isNewChat


    fun updateIsNewChat(launchNewChat: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _isNewChat.value = launchNewChat
            }
        }
    }

    fun updateSelectedAgentId(agentId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _selectedAgentId.value = agentId
            }
        }
    }
    fun clearSelectedAgentId() {

            _selectedAgentId.value = ""
            Log.d(TAG, "Selected agent ID cleared in ViewModel")

    }

    private var stopReceivingResults = false


    suspend fun initialize() {
        _isFetching.value = true
        _conversations.value = conversationRepo.fetchConversations()
        print(_conversations.value)

        if (_conversations.value.isNotEmpty()) {
           // _currentConversation.value = _conversations.value.first().id
            fetchMessages()
        }
        _isFetching.value = false
    }

    suspend fun onConversation(conversation: ConversationModel) {
        _isFetching.value = true
        _currentConversation.value = conversation.id
        fetchMessages()
        _isFetching.value = false
    }





    suspend fun sendMessage(message: String, onLoadingChange: (Boolean) -> Unit = {}) {
        _isLoading.value = true
        stopReceivingResults = false
        if (getMessagesByConversation(_currentConversation.value).isEmpty()) {
            createConversationRemote(message)
        }
        val flow: Flow<String> = openAIRepo.textCompletionsWithStream(
            TextCompletionsParam(
                promptText = getPrompt(_currentConversation.value),
                messagesTurbo = getMessagesParamsTurbo(_currentConversation.value)
            )
        )

        val newMessageModel = MessageModel(
            question = message,
            answer = "",
            conversationId = _currentConversation.value,
        )

        val currentListMessage = getMessagesByConversation(_currentConversation.value).toMutableList()
        currentListMessage.add(0, newMessageModel)
        setMessages(currentListMessage)

        val reqAgentId: String = SessionManager.agents.entries.find { it.value.name == SessionManager.selectedAgentId }?.key ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.sendMessage(
                    ApiRequest(
                        sessionId = SessionManager.sessionId,
                        agentId = reqAgentId,
                        data = Data(
                            query = message,
                            uniqueId = _currentConversation.value,
                        )
                    )
                ).execute()

                withContext(Dispatchers.Main) {
                    onLoadingChange(false)

                    if (response.isSuccessful) {
                        _isLoading.value = false
                        val responseBody = response.body()?.string()?.trim()
                        responseBody?.let { responseBodyStr ->
                            val jsonElement = JsonParser.parseString(responseBodyStr)
                            if (jsonElement.isJsonObject) {
                                val jsonObject = jsonElement.asJsonObject
                                val result = jsonObject.getAsJsonPrimitive("result")?.asString
                                if (result == null) {
                                    messageRepo.createMessage(newMessageModel.copy(answer = "input details missing"))
                                    updateLocalAnswer("input details missing")
                                } else {
                                    updateLocalAnswer(result)
                                    setFabExpanded(true)
                                    messageRepo.createMessage(newMessageModel.copy(answer = result))
                                }
                            } else {
                                messageRepo.createMessage(newMessageModel.copy(answer = "input details missing"))
                                updateLocalAnswer("input details missing")
                            }
                        } ?: run {
                            messageRepo.createMessage(newMessageModel.copy(answer = "input details missing"))
                            updateLocalAnswer("input details missing")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        println("Response was not successful. Error body: $errorBody")
                        _isLoading.value = false
                        val errorMessage = errorBody ?: "Unknown error"
                        if (response.code() == 500) {
                            messageRepo.createMessage(newMessageModel.copy(answer = "input details missing"))
                            updateLocalAnswer("input details missing")
                        } else {
                            messageRepo.createMessage(newMessageModel.copy(answer = "oops something error"))
                            updateLocalAnswer("oops something error")
                        }
                    }
                    setFabExpanded(false)
                }
            } catch (e: SocketTimeoutException) {
                withContext(Dispatchers.Main) {
                    println("Timeout occurred: ${e.message}")
                    _isLoading.value = false
                    messageRepo.createMessage(newMessageModel.copy(answer = "oops something error"))
                    updateLocalAnswer("oops something error")
                    onLoadingChange(false)
                    setFabExpanded(false)
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    println("Network error occurred: ${e.message}")
                    _isLoading.value = false
                    messageRepo.createMessage(newMessageModel.copy(answer = "oops something error"))
                    updateLocalAnswer("oops something error")
                    onLoadingChange(false)
                    setFabExpanded(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println("An error occurred: ${e.message}")
                    _isLoading.value = false
                    messageRepo.createMessage(newMessageModel.copy(answer = "oops something error"))
                    updateLocalAnswer("oops something error")
                    onLoadingChange(false)
                    setFabExpanded(false)
                }
            }
        }
    }




    private fun createConversationRemote(title: String) {
        val newConversation: ConversationModel = ConversationModel(
            id = _currentConversation.value,
            title = title,
            createdAt = Date(),
        )
        conversationRepo.newConversation(newConversation)

        val conversations = _conversations.value.toMutableList()
        conversations.add(0, newConversation)

        _conversations.value = conversations
    }

  fun newConversation() {
        val conversationId: String = Date().time.toString()
        _currentConversation.value = conversationId  // Assuming _currentConversation is a MutableLiveData<String> or similar
        println(_currentConversation.value)
       // setShowAgent(true)
        println(conversationId)
       Log.d(TAG, "New conversation started with ID 1: $conversationId")

    }

    fun getMessagesByConversation(conversationId: String): MutableList<MessageModel> {
        setShowAgent(false)
        if (_messages.value[conversationId] == null) return mutableListOf()

        val messagesMap: HashMap<String, MutableList<MessageModel>> =
            _messages.value.clone() as HashMap<String, MutableList<MessageModel>>
        print(messagesMap[conversationId])
        Log.d(TAG, "New conversation started with ID 2: ${messagesMap[conversationId]}")
        return messagesMap[conversationId]!!

    }

    private fun getPrompt(conversationId: String): String {
        if (_messages.value[conversationId] == null) return ""

        val messagesMap: HashMap<String, MutableList<MessageModel>> =
            _messages.value.clone() as HashMap<String, MutableList<MessageModel>>

        var response: String = ""

        for (message in messagesMap[conversationId]!!.reversed()) {
            response += """Human:${message.question.trim()}
                |Bot:${
                if (message.answer == "oops something error") ""
                else message.answer.trim()
            }""".trimMargin()
        }

        return response
    }

    private fun getMessagesParamsTurbo(conversationId: String): List<MessageTurbo> {
        if (_messages.value[conversationId] == null) return listOf()

        val messagesMap: HashMap<String, MutableList<MessageModel>> =
            _messages.value.clone() as HashMap<String, MutableList<MessageModel>>

        val response: MutableList<MessageTurbo> = mutableListOf(
            MessageTurbo(
                role = TurboRole.system, content = "Markdown style if exists code"
            )
        )

        for (message in messagesMap[conversationId]!!.reversed()) {
            response.add(MessageTurbo(content = message.question))

            if (message.answer != "Let me thinking...") {

                response.add(MessageTurbo(content = message.answer, role = TurboRole.user))
            }
        }

        return response.toList()
    }

    suspend fun deleteConversation(conversationId: String) {
        // Delete remote
        conversationRepo.deleteConversation(conversationId)

        // Delete local
        val conversations: MutableList<ConversationModel> = _conversations.value.toMutableList()
        val conversationToRemove = conversations.find { it.id == conversationId }

        if (conversationToRemove != null) {
            conversations.remove(conversationToRemove)
            _conversations.value = conversations
        }
    }

    private suspend fun fetchMessages() {
        if (_currentConversation.value.isEmpty() ||
            _messages.value[_currentConversation.value] != null) return

        val flow: Flow<List<MessageModel>> = messageRepo.fetchMessages(_currentConversation.value)
        Log.d(TAG, "New conversation started with ID 3: ${flow}")

        flow.collectLatest {
            setMessages(it.toMutableList())
            if(_isNewChat.value){
                newConversation()
            }
        }
    }

    private fun updateLocalAnswer(answer: String) {
        val currentListMessage: MutableList<MessageModel> =
            getMessagesByConversation(_currentConversation.value).toMutableList()
        if (currentListMessage.isNotEmpty() ) {
            currentListMessage[0] = currentListMessage[0].copy(answer = answer)
            setMessages(currentListMessage)
        }
    }

    public fun setMessages(messages: MutableList<MessageModel>) {
        val messagesMap: HashMap<String, MutableList<MessageModel>> =
            _messages.value.clone() as HashMap<String, MutableList<MessageModel>>
        println(_currentConversation.value)
        messagesMap[_currentConversation.value] = messages
        print(messages)
        Log.d(TAG, "New conversation started with ID 4: ${messages}")
        _messages.value = messagesMap
    }

    fun stopReceivingResults() {
        stopReceivingResults = true
    }
    private fun setFabExpanded(expanded: Boolean) {
        _isFabExpanded.value = expanded
    }





}