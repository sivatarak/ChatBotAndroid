package loginandsignup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chatgptlite.wanted.R
import com.chatgptlite.wanted.constants.Agent

class AgentAdapter(
    private var agentList: List<Agent>,
    private val clickListener: AgentClickListener
) : RecyclerView.Adapter<AgentAdapter.AgentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agent, parent, false)
        return AgentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AgentViewHolder, position: Int) {
        holder.bind(agentList[position], clickListener)
    }

    override fun getItemCount(): Int {
        return agentList.size
    }

    fun updateList(newList: List<Agent>) {
        agentList = newList
        notifyDataSetChanged()
    }

    interface AgentClickListener {
        fun onClickAgent(agentId: String)
    }

    class AgentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnAgentId: Button = itemView.findViewById(R.id.btnAgentId)
        private val tvAdditionalText: TextView = itemView.findViewById(R.id.tvAdditionalText)
        private val btnReadMore: Button = itemView.findViewById(R.id.btnReadMore)

        fun bind(agent: Agent, clickListener: AgentClickListener) {
            btnAgentId.text = agent.name
            val descriptionAvailable = agent.description.isNotEmpty()

            if (descriptionAvailable) {
                tvAdditionalText.text = if (agent.isExpanded) agent.description else agent.description.take(50) + "..."
                btnReadMore.text = if (agent.isExpanded) "Read Less" else "Read More"
                btnReadMore.visibility = View.VISIBLE
            } else {
                tvAdditionalText.text = ""
                btnReadMore.visibility = View.GONE
            }

            btnAgentId.setOnClickListener {
                clickListener.onClickAgent(agent.name)
            }

            btnReadMore.setOnClickListener {
                agent.isExpanded = !agent.isExpanded
                tvAdditionalText.text = if (agent.isExpanded) agent.description else agent.description.take(80) + "..."
                btnReadMore.text = if (agent.isExpanded) "Read Less" else "Read More"
            }
        }
    }
}
