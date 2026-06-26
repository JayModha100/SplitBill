package com.example.splitbill.util

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

object CurrentUser {
    fun uid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}
