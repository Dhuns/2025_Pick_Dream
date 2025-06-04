package com.example.pick_dream.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pick_dream.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.pick_dream.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.editTextId)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val forgotPasswordTextView = findViewById<TextView>(R.id.tvForgotPassword)

        loginButton.setOnClickListener {
            val id = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "학번과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dummyEmail = "${id}@example.com"

            auth.signInWithEmailAndPassword(dummyEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "로그인 실패: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        forgotPasswordTextView.setOnClickListener {
            val url = "https://kutis.kyonggi.ac.kr/webkutis/view/indexWeb.jsp"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "웹 페이지를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
