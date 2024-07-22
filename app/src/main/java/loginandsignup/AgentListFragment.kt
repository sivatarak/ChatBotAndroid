package loginandsignup

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatgptlite.wanted.MainActivity
import com.chatgptlite.wanted.R
import com.chatgptlite.wanted.constants.Agent
import com.chatgptlite.wanted.constants.Data1
import com.chatgptlite.wanted.constants.QuestionReqData
import com.chatgptlite.wanted.constants.RetrofitInstance
import com.chatgptlite.wanted.constants.SessionManager
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel_Factory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

@AndroidEntryPoint
class AgentListFragment : AppCompatActivity(), AgentAdapter.AgentClickListener {

    private lateinit var agentAdapter: AgentAdapter
    private lateinit var allAgents: List<Agent>  // Updated to use List<Agent>
  //  private val viewModel: ConversationViewModel.

    private val viewModel: ConversationViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_agent_list)
        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        Log.d("AgentListFragment", "Using ViewModel instance: ${viewModel.getInstanceId()}")
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        // Convert SessionManager.agents Map to a List<Agent>
        allAgents = SessionManager.agents.values.toList()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAgents)
        recyclerView.layoutManager = LinearLayoutManager(this)
        agentAdapter = AgentAdapter(allAgents, this)
        recyclerView.adapter = agentAdapter
    }

    private fun setupSearchView() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.isIconified = false
        searchView.clearFocus()

        val searchText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchText.setTextColor(Color.WHITE)
        searchText.setHintTextColor(Color.GRAY)
        searchText.hint = "Search Agents"

        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setImageDrawable(ColorDrawable(Color.TRANSPARENT))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = filterAgents(newText)
                agentAdapter.updateList(filteredList)
                return true
            }
        })
    }

    fun getQuestionCards(agentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Find the agentId using the agentName
                val reqAgentId = SessionManager.agents.entries.find { it.value.name == agentId }?.key

                // Check if agentId is found
                if (reqAgentId != null) {
                    println("Requesting question cards for agentId: $reqAgentId")

                    // Create a QuestionReqData instance with the provided data
                    val data = QuestionReqData(
                        sessionId = "acb76b9c-f859-449c-b3e3-136982dae973",
                        agentId = reqAgentId
                    )

                    // Call the API service with the loginRequest
                    val response = RetrofitInstance.apiService.getQuestionCards(data).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        println("Response Body: $responseBody")

                        // Parse the JSON response
                        val jsonResponse = JSONObject(responseBody)

                        // Extract the array of questions from the JSON response
                        val questionsJsonArray = jsonResponse.getJSONArray("questions")

                        // Convert the JSON array to a list of strings
                        val questions = mutableListOf<String>()
                        for (i in 0 until questionsJsonArray.length()) {
                            questions.add(questionsJsonArray.getString(i))
                        }

                        // Print the questions
                        println("Question cards are: $questions")

                        // Update SessionManager's questions
                        SessionManager.questions = questions
                    } else {
                        // Handle unsuccessful response
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


    override fun onClickAgent(agentId: String) {
        val reqAgentId = SessionManager.agents.entries.find { it.value.name == agentId }?.key
        getQuestionCards(agentId)
        lifecycleScope.launch {
            startNewConversation(agentId)
            // Call the singleton method
            val initResponseCode = SignInActivity.SignInUtils.initialInstances(reqAgentId ?: "")
            if (initResponseCode == 200) {
                startNewConversation(agentId)
            } else {
                showInitializationFailedToast()
                startNewConversation(agentId)
                viewModel.setInitializationFailed(true) // Correctly set the flag
                println("Initialization failed with code: $initResponseCode")
            }
        }
    }
    private fun showInitializationFailedToast() {
        val toast = Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private suspend fun startNewConversation(agentId: String) {
        Log.d(TAG, "Starting new conversation with agentId: $agentId")
        SessionManager.selectedAgentId = agentId
        delay(100)

        // After the new conversation is started, navigate to MainActivity
        openMainActivity(agentId)
    }

    private fun openMainActivity(agentId: String) {
        Log.d(TAG, "Opening MainActivity with agent ID: $agentId")
        val intent = Intent(this@AgentListFragment, MainActivity::class.java)
        intent.putExtra("agentId", agentId)
        intent.putExtra("showAgent", true)
        startActivity(intent)
        finish()
    }

    private fun filterAgents(query: String?): List<Agent> {
        if (query.isNullOrEmpty()) {
            return allAgents
        }
        val lowerCaseQuery = query.toLowerCase(Locale.ROOT)
        return allAgents.filter { it.name.toLowerCase(Locale.ROOT).contains(lowerCaseQuery) }
    }


}
