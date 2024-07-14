package loginandsignup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chatgptlite.wanted.R
import com.codingstuff.loginandsignup.MainActivity
import android.widget.Button

class SettingsFragment : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings)

        // Find the sign-out button by its ID
        val signOutButton: Button = findViewById(R.id.sign_out_button)

        // Set click listener for sign-out button
        signOutButton.setOnClickListener {
            // Intent to launch MainActivity
            val intent = Intent(this, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Optional: Call finish() if you want to close the SettingsFragment activity
        }
    }
}
