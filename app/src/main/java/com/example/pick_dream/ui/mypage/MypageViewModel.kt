package com.example.pick_dream.ui.mypage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pick_dream.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPageViewModel : ViewModel() {
    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> get() = _userData

    fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("MyPageViewModel", "UID = $uid") // ① UID 출력

        if (uid == null) {
            Log.e("MyPageViewModel", "로그인 안 됨 (UID is null)")
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("User").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                Log.d("MyPageViewModel", "Firestore 문서 = ${doc.data}") // ② 문서 출력
                val user = FirebaseAuth.getInstance().currentUser
                Log.d("MyUID", "현재 UID = ${user?.uid}")

                doc.toObject(User::class.java)?.let {
                    Log.d("MyPageViewModel", "User 객체 = $it") // ③ 객체 출력
                    _userData.value = it
                }
            }
            .addOnFailureListener { e ->
                Log.e("MyPageViewModel", "Firestore 실패: ${e.message}", e) // ④ 실패 로그
            }
    }
}
