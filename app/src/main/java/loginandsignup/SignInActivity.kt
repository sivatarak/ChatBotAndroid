package loginandsignup

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var progressOverlay: View

    private val viewModel: ConversationViewModel by viewModels()

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

    private suspend fun login(username: String, password: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)

        if (isLoggedIn) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val loginRequest = Data1(
                        userId = "acb76b9c-f859-449c-b3e3-136982dae973",
                        sessionId = "acb76b9c-f859-449c-b3e3-136982dae973",
                        hierarchyId = "xyac",
                        role = "R1"
                    )

                    val response = RetrofitInstance.apiService.getAgents(loginRequest).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        println("Response Body: $responseBody")

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
                        val firstAgent = agentsMap.keys.first()
                        val firstAgentName: String? = agentsMap.values.firstOrNull()?.name
                        SessionManager.selectedAgentId = firstAgentName ?: ""
                        val reqAgentId = SessionManager.agents.entries.find { it.value.name == firstAgentName }?.key
                        val initResponseCode = SignInUtils.initialInstances(reqAgentId!!)
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
                    } else {
                        val errorBody = response.errorBody()?.string()
                        println("Error Response: $errorBody")
                        withContext(Dispatchers.Main) {
                            handleLoginFailure()
                        }
                    }
                } catch (e: Exception) {
                    println("Exception: ${e.message}")
                    withContext(Dispatchers.Main) {
                        handleLoginFailure()
                    }
                }
            }
        } else {
            with(sharedPreferences.edit()) {
                putBoolean("is_logged_in", true)
                apply()
            }

            handleLoginSuccess()
        }
    }

    private fun handleLoginSuccess() {
        progressOverlay.visibility = View.GONE
        val agents = SessionManager.agents
        println("Agents: $agents")

        openMainActivity()
    }

    private fun handleLoginFailure() {

        val agents = mapOf(
            "A1" to Agent(name = "Resume", description = "The Resume Intelligence System..."),
            "A2" to Agent(name = "Law", description = "The Law Intelligence System...")
        )
        SessionManager.agents = agents
        progressOverlay.visibility = View.GONE
        openMainActivity()
        //Toast.makeText(this, "Network error occurred. Please try again.", Toast.LENGTH_SHORT).show()
    }

    object SignInUtils {
        suspend fun initialInstances(agentId: String): Int {
            return withContext(Dispatchers.IO) {
                try {
                    val data = Data1(
                        userId = "acb76b9c-f859-449c-b3e3-136982dae973",
                        sessionId = "acb76b9c-f859-449c-b3e3-136982dae973",
                        hierarchyId = "",
                        role = "R1",
                        agentId = agentId
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
                                 //   viewModel.setInitializationFailed(true)
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
