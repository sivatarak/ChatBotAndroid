package loginandsignup

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import com.chatgptlite.wanted.MainActivity
import com.chatgptlite.wanted.constants.Agent
import com.chatgptlite.wanted.constants.ApiRequest
import com.chatgptlite.wanted.constants.Data1
import com.chatgptlite.wanted.constants.RetrofitInstance
import com.chatgptlite.wanted.constants.SessionManager
import com.chatgptlite.wanted.constants.UserIdWrapper
import com.chatgptlite.wanted.databinding.ActivitySignInBinding
import com.chatgptlite.wanted.models.MessageModel
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import loginandsignup.SignInActivity.SignInUtils.initialInstances
import org.json.JSONArray
import org.json.JSONObject
@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val viewModel: ConversationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                login(email, pass)
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun login(username: String, password: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", true)

        if (isLoggedIn) {
            // User is already logged in, call agents service
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Create a LoginRequest instance with the provided data
                    val loginRequest = Data1(
                        userId = "acb76b9c-f859-449c-b3e3-136982dae973",
                        sessionId = "acb76b9c-f859-449c-b3e3-136982dae973",
                        hierarchyId = "xyac",
                        role = "R1"
                    )

                    // Call agents service with the loginRequest
                    val response = RetrofitInstance.apiService.getAgents(loginRequest).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        println("Response Body: $responseBody")

                        val jsonResponse = JSONObject(responseBody)
                        // Assuming the field is named "agentIds" in the actual response
                        val agentIdsObject = jsonResponse.getJSONObject("agentIds")

                        // Convert JSONObject to Map<String, Agent>
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
                        val firstAgent = agentsMap.keys.first()
                        println(firstAgent)
                        // Update SessionManager with the Map
                        SessionManager.agents = agentsMap
                        val firstAgentName: String? = agentsMap.values.firstOrNull()?.name
                        SessionManager.selectedAgentId = firstAgentName ?: ""
                        val reqAgentId = SessionManager.agents.entries.find { it.value.name == firstAgentName }?.key
                        // Initialize instances and handle response
                        val initResponseCode = initialInstances(reqAgentId!!)
                        if (initResponseCode == 200) {



                            handleLoginSuccess() // Common function for handling login success
                        } else {
                            println("Initialization failed with code: $initResponseCode")
                            handleLoginFailure() // Common function for handling login failure
                        }
                    } else {
                        // Handle unsuccessful response
                        val errorBody = response.errorBody()?.string()
                        println("Error Response: $errorBody")
                        handleLoginFailure() // Common function for handling login failure
                    }
                } catch (e: Exception) {
                    // Handle exception
                    println("Exception: ${e.message}")
                    handleLoginFailure() // Common function for handling login failure
                }
            }
        } else {
            // Mock a successful response (for demonstration, replace with actual login logic)
            val editor = sharedPreferences.edit()
            editor.putBoolean("is_logged_in", true)
            editor.apply()

            handleLoginSuccess() // Common function for handling login success
        }
    }



    private fun handleLoginSuccess() {
        // Assume agents are already stored in SessionManager
        val agents = SessionManager.agents
        println("Agents: $agents")

        // You can also set agentIds if needed
       // val agentIds = agents.mapValues { it.value.name }
        //SessionManager.agentIds = agentIds

        openMainActivity()
    }

    private fun handleLoginFailure() {
        // Mock data for demonstration
        val agents = mapOf(
            "A1" to Agent(name = "Resume", description = "The Resume Intelligence System..."),
            "A2" to Agent(name = "Law", description = "The Law Intelligence System...")
        )
        SessionManager.agents = agents

        // Also set agentIds if needed
        val agentIds = agents.mapValues { it.value.name }
       // SessionManager.agentIds = agentIds

        openMainActivity()
    }

    object SignInUtils {
        suspend fun initialInstances(agentId: String): Int {
            return withContext(Dispatchers.IO) {
                try {
                    val data = Data1(
                        userId = "acb76b9c-f859-449c-b3e3-136982dae973",
                        sessionId = "acb76b9c-f859-449c-b3e3-136982dae973",
                        hierarchyId = "xyac",
                        role = "R1",
                        agentId = agentId
                    )
                    val response = RetrofitInstance.apiService.initializeInstances(data).execute()
                    if (response.isSuccessful) {
                        return@withContext 200
                    } else {
                        return@withContext response.code()
                    }
                } catch (e: Exception) {
                    println("Exception: ${e.message}")
                    return@withContext 500
                }
            }
        }
    }


    private fun openMainActivity() {
        val intent = Intent(this@SignInActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }



}
