package com.example.pick_dream.ui.mypage.review

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_review)

        // 뒤로가기 버튼 클릭 시 현재 액티비티 종료
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        val recyclerView = findViewById<RecyclerView>(R.id.reviewRecyclerView)
        val adapter = ReviewAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

//Firebase
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

// 1. uid로 학번(studentId) 가져오기
        db.collection("User").document(uid).get()
            .addOnSuccessListener { document ->
                val studentId = document.getString("studentId") ?: return@addOnSuccessListener
                val adapter = ReviewAdapter()
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = adapter

                // 2. 학번으로 후기 불러오기
                db.collection("Reviews")
                    .whereEqualTo("userID", studentId)  // 🔥 여기서 학번 사용!
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val reviewList = querySnapshot.documents.mapNotNull {
                            it.toObject(Review::class.java)
                        }
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