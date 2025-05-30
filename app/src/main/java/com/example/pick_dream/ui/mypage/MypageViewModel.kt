package com.example.pick_dream.ui.mypage

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

        if (uid == null) {
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("User").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val user = FirebaseAuth.getInstance().currentUser

                doc.toObject(User::class.java)?.let {
                    _userData.value = it
                }
            }
            .addOnFailureListener { e ->
            }
    }
}
