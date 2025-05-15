package com.example.urreciptionary.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object UserInfoManager {

    fun getUsername(callback: (String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: return callback(null)
        val ref = FirebaseDatabase.getInstance().getReference("users").child(user.uid).child("username")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java)
                callback(username)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserInfoManager", "Failed to fetch username", error.toException())
                callback(null)
            }
        })
    }

    fun getPhotoUrl(callback: (String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: return callback(null)
        val ref = FirebaseDatabase.getInstance().getReference("users").child(user.uid).child("photoUrl")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val photoUrl = snapshot.getValue(String::class.java)
                callback(photoUrl)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserInfoManager", "Failed to fetch photoUrl", error.toException())
                callback(null)
            }
        })
    }
}