package com.example.splitbill.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.Locale

object UpiLauncher {
    fun launchPayment(
        context: Context,
        payeeVpa: String,
        payeeName: String,
        amount: Double,
        note: String
    ): Boolean {
        val amountFormatted = String.format(Locale.US, "%.2f", amount)
        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", payeeVpa)
            .appendQueryParameter("pn", payeeName)
            .appendQueryParameter("am", amountFormatted)
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", note)
            .build()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
        }

        val chooser = Intent.createChooser(intent, "Pay with...")
        return try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooser)
                true
            } else {
                Toast.makeText(context, "No UPI app found", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to launch UPI", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
