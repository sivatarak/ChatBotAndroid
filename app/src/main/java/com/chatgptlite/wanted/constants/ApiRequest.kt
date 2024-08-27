package com.chatgptlite.wanted.constants

import kotlinx.serialization.Serializable

data class ApiRequest(
    val sessionId: String,
    val agentId: String,
    val data: Data
)

data class Data(
    val query: String,
    val uniqueId: String,
)
data class UserIdWrapper(val userId: String)
data class Data1(
    val userId: String = "",
    val sessionId: String = "",
    val role: String = "",
    var hierarchyId: String="",
    var agentId: String ="",
    val org : String = "",
    val position : String = ""
)
data class QuestionReqData(
    val sessionId: String = "",
    var agentId: String =""
)
data class Agent(
    val name: String,
    val description: String,
    var isExpanded: Boolean = false
)

object SessionManager {
    var agents: Map<String, Agent> = mutableMapOf()
    var selectedAgentId: String = ""
    var questions: List<String> = listOf()  // Initialize as an empty list
    var sessionId: String = ""
    private var loginResponse: LoginResponse? = null  // Store the login response

    fun setLoginResponse(response: LoginResponse) {
        loginResponse = response
    }

    fun getLoginResponse(): LoginResponse? {
        return loginResponse
    }
}

data class LoginRequest(
    val username: String,
    val password: String,
    val deviceHash: String,
    val sessionId: String
)

@Serializable
data class LoginResponse(
    val userName: String,
    val email: String,
    val userId: String,
    val org: String,
    val position: String,
    val deviceHash: String,
    val role: Role,
    val someOptionalField: String? = null
)

@Serializable
data class Role(
    val user: Map<String, String> // Using a map to handle dynamic keys
)

object Config {
    const val API_BASE_URL = "http://172.25.1.237:5001/"
}

