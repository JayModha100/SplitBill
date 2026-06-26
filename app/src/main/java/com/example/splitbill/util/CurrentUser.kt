package com.example.splitbill.util

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

object CurrentUser {
    private const val PREFS_NAME = "splitbill_prefs"
    private const val KEY_DEV_UID = "dev_uid"

    fun uid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}
