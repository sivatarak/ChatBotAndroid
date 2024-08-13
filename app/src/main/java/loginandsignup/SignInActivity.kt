package loginandsignup

import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatgptlite.wanted.MainActivity
import com.chatgptlite.wanted.R
import com.chatgptlite.wanted.constants.Agent
import com.chatgptlite.wanted.constants.Data1
import com.chatgptlite.wanted.constants.RetrofitInstance
import com.chatgptlite.wanted.constants.SessionManager
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import android.provider.Settings
import android.content.Context
import android.view.contentcapture.ContentCaptureSessionId
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.chatgptlite.wanted.constants.LoginRequest
import com.chatgptlite.wanted.constants.LoginResponse
import com.chatgptlite.wanted.constants.SessionManager.sessionId
import com.chatgptlite.wanted.ui.common.AgentsScreen
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy
import java.util.UUID


@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var progressOverlay: View

    private val viewModel: ConversationViewModel by viewModels()
//    @Composable
//    fun MyComposableFunction(viewModel: ConversationViewModel) {
//        val showAgent by viewModel.isShowAgent.collectAsState()
//
//        // Use showAgent here
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize the UI components
        emailEditText = findViewById(R.id.emailEt)
        passwordEditText = findViewById(R.id.passET)
        progressOverlay = findViewById(R.id.progress_overlay)

        findViewById<View>(R.id.button).setOnClickListener {
            val email = emailEditText.text.toString()
            val pass = passwordEditText.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                // Show the progress overlay
                progressOverlay.visibility = View.VISIBLE

                // Launch a coroutine from the Main dispatcher
                CoroutineScope(Dispatchers.Main).launch {
                    login(email, pass)
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun getDeviceHash(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }

    fun generateSessionId(): String {
        return UUID.randomUUID().toString()
    }

    private fun setEditTextsEnabled(enabled: Boolean) {
        emailEditText.isEnabled = enabled
        passwordEditText.isEnabled = enabled
    }

    private fun showProgressOverlay() {
        progressOverlay.visibility = View.VISIBLE
        setEditTextsEnabled(false)
    }

    private fun hideProgressOverlay() {
        progressOverlay.visibility = View.GONE
        setEditTextsEnabled(true)
    }

    private val json = Json {
        ignoreUnknownKeys = true // This will ignore unknown keys during deserialization
    }
    private suspend fun login(username: String, password: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false) // Changed default to false
        showProgressOverlay()
        val deviceHash = getDeviceHash(this)
        val sessionId = generateSessionId()

        if (isLoggedIn) { // Changed condition to !isLoggedIn
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Login request
                    val loginRequest = LoginRequest(
                        username = username,
                        password = password,
                        deviceHash = deviceHash,
                        sessionId = sessionId
                    )

                    val loginResponse = RetrofitInstance.apiService.login(loginRequest).execute()
                    if (loginResponse.isSuccessful) {
                        val responseBody = loginResponse.body()?.string()
                        val jsonObject = JsonParser.parseString(responseBody).asJsonObject
                        val statusCode = jsonObject.get("status_code")?.asInt

                        if (statusCode == 404) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@SignInActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                progressOverlay.visibility = View.GONE
                            }
                            return@launch
                        }


                        val loginResponseBody: LoginResponse = json.decodeFromString(responseBody ?: "")

                        print(loginResponseBody)
                        if (loginResponseBody != null) {
                            // Store login response in SessionManager
                            SessionManager.setLoginResponse(loginResponseBody)

                            val roleMap = loginResponseBody.role.user
                            val hierarchyId = roleMap.keys.firstOrNull() ?: ""  // Extract "hierarchy Id"
                            val role = roleMap[hierarchyId] ?: ""  // Extract "Role"
                            val userId = loginResponseBody.userId
                            val sessionId = sessionId
                            SessionManager.sessionId = sessionId
                            // Get agents request
                            val agentRequest = Data1(
                                userId = userId,
                                sessionId = sessionId,
                                hierarchyId = hierarchyId ,
                                role = role ,
                                org = loginResponseBody.org,
                                position = loginResponseBody.position
                            )
                            Log.d(ContentValues.TAG, "the agentRequest${agentRequest},${sessionId}")

                            val response = RetrofitInstance.apiService.getAgents(agentRequest).execute()
                            if (response.isSuccessful) {
                                val responseBody = response.body()?.string()
                                println("Response Body: $responseBody")
                                Log.d(ContentValues.TAG, "Response Body: $responseBody")
                                val jsonResponse = JSONObject(responseBody)
                                val agentIdsObject = jsonResponse.getJSONObject("agentIds")

                                val agentsMap = mutableMapOf<String, Agent>()
                                val keys = agentIdsObject.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    val agentData = agentIdsObject.getJSONObject(key)
                                    val agent = Agent(
                                        name = agentData.optString("Name"),
                                        description = agentData.optString("Description")
                                    )
                                    agentsMap[key] = agent
                                }

                                SessionManager.agents = agentsMap
                                val firstAgentName = agentsMap.values.firstOrNull()?.name
                                SessionManager.selectedAgentId = firstAgentName ?: ""
                                val reqAgentId = SessionManager.agents.entries.find { it.value.name == firstAgentName }?.key

                                reqAgentId?.let {
                                    val initResponseCode = SignInUtils.initialInstances(it)
                                    if (initResponseCode == 200) {
                                        withContext(Dispatchers.Main) {
                                            handleLoginSuccess()
                                        }
                                    } else {
                                        println("Initialization failed with code: $initResponseCode")
                                        withContext(Dispatchers.Main) {
                                            handleLoginFailure()
                                        }
                                    }
                                } ?: withContext(Dispatchers.Main) {
                                    handleLoginFailure()
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                println("Error Response: $errorBody")
                                withContext(Dispatchers.Main) {
                                    handleLoginFailure()
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                handleLoginFailure()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            handleLoginFailure()
                        }
                    }
                } catch (e: Exception) {
                    println("Exception: ${e.message}")
                    withContext(Dispatchers.Main) {
                        //handleLoginFailure()
                        Toast.makeText(this@SignInActivity, "Network error occurred, Try again", Toast.LENGTH_SHORT).show()
                        progressOverlay.visibility = View.GONE
                    }
                }finally {
                    // Hide progress overlay
                    withContext(Dispatchers.Main) {
                        hideProgressOverlay()
                       // progressOverlay.visibility = View.GONE
                    }
                }
            }
        } else {
            // User is already logged in
            handleLoginSuccess()
        }

        // Set logged in status to true
        with(sharedPreferences.edit()) {
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    private fun handleLoginSuccess() {
        progressOverlay.visibility = View.GONE
        val agents = SessionManager.agents
        println("Agents: $agents")

        openMainActivity()
    }

    private fun handleLoginFailure() {

//        val agents = mapOf(
//            "A1" to Agent(name = "Resume", description = "The Resume Intelligence System..."),
//            "A2" to Agent(name = "Law", description = "The Law Intelligence System...")
//        )
//        SessionManager.agents = agents
//        progressOverlay.visibility = View.GONE
//        openMainActivity()

        Toast.makeText(this, "Invalid input arguments", Toast.LENGTH_SHORT).show()
        progressOverlay.visibility = View.GONE
    }

    object SignInUtils {
        suspend fun initialInstances(agentId: String): Int {
            return withContext(Dispatchers.IO) {
                try {
                    val loginResponse = SessionManager.getLoginResponse()
                    //val sessionId = SessionManager.getSessionId()

                    if (loginResponse == null || sessionId == null) {
                        println("Login response or session ID is missing.")
                        return@withContext 500
                    }

                    val roleMap = loginResponse.role.user
                    val hierarchyId = roleMap.keys.firstOrNull() ?: ""  // Extract "hierarchy Id"
                    val role = roleMap[hierarchyId] ?: ""  // Extract "Role"

                    val data = Data1(
                        userId = loginResponse.userId,
                        sessionId = sessionId,
                        hierarchyId = hierarchyId,
                        role = role,
                        agentId = agentId,
                        org = loginResponse.org,
                        position = loginResponse.position
                    )

                    val response = RetrofitInstance.apiService.initializeInstances(data).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        println(responseBody)

                        if (responseBody != null) {
                            val jsonObject = JsonParser.parseString(responseBody).asJsonObject
                            val statusCode = jsonObject.get("status_code")?.asInt

                            if (statusCode == 200) {

                                200
                            } else {
                                val errorDetail = jsonObject.get("detail")?.asString

                                println("Response was not successful. Error detail: $errorDetail")
                                if (statusCode == 500) {
                                    // viewModel.setInitializationFailed(true)
                                }
                                statusCode ?: response.code()
                            }
                        } else {
                            println("Response was not successful. Empty body.")
                            response.code()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        println("Response was not successful. Error body: $errorBody")
                        response.code()
                    }
                } catch (e: Exception) {
                    println("Exception: ${e.message}")
                    500
                }
            }
        }
    }




    private fun openMainActivity() {
        progressOverlay.visibility = View.GONE
        val intent = Intent(this@SignInActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
