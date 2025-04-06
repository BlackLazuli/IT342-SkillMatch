    package com.example.skillmatch

    import android.annotation.SuppressLint
    import android.content.Intent
    import android.os.Bundle
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageView
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity

    class Login : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)

            val usernameEditText: EditText = findViewById(R.id.usernameInput)
            val passwordEditText: EditText = findViewById(R.id.passwordInput)
            val loginButton: Button = findViewById(R.id.loginbtn)
            val googleLogin: ImageView = findViewById(R.id.googlebtn)
            val signUpText: TextView = findViewById(R.id.signUpText)

            loginButton.setOnClickListener {
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    // Navigate to the main activity after login
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                }
            }

            googleLogin.setOnClickListener {
                Toast.makeText(this, "Google Login Clicked", Toast.LENGTH_SHORT).show()
                // Implement Google Sign-In functionality
            }

            signUpText.setOnClickListener {
                val intent = Intent(this, ChooseRole::class.java)
                startActivity(intent)
            }
        }
    }
