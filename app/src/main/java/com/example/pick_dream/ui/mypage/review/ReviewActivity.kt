package com.example.pick_dream.ui.mypage.review

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        val recyclerView = findViewById<RecyclerView>(R.id.reviewRecyclerView)
        val adapter = ReviewAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("User").document(uid).get()
            .addOnSuccessListener { document ->
                val studentId = document.getString("studentId")
                if (studentId.isNullOrBlank()) {
                    Log.e("ReviewActivity", "Student ID not found")
                    return@addOnSuccessListener
                }

                db.collection("Reviews")
                    .whereEqualTo("userID", studentId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)  // 최신순으로 정렬
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val reviewList = querySnapshot.toObjects(Review::class.java)
                        adapter.submitList(reviewList)
                    }
                    .addOnFailureListener {
                        Log.e("ReviewActivity", "리뷰 로딩 실패", it)
                    }
            }
            .addOnFailureListener {
                Log.e("ReviewActivity", "학번 조회 실패", it)
            }
    }
}