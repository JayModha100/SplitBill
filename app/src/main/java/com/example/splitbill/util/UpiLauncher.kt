package com.example.splitbill.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.Locale

/**
 * Builds and launches a UPI "pay" deep link (upi://pay) so the user can settle
 * a debt in their preferred UPI app (GPay, PhonePe, Paytm, etc.) with the
 * recipient, amount and note pre-filled.
 *
 * Spec reference: NPCI UPI Linking Specification (pa/pn/am/cu/tn params).
 */
object UpiLauncher {

    /**
     * @param payeeVpa     recipient's UPI id (e.g. "jay@okhdfcbank")
     * @param payeeName    recipient's display name
     * @param amount       amount in rupees
     * @param note         transaction note shown in the UPI app
     * @return true if a UPI app was launched, false if none could handle it.
     */
    fun launchPayment(
        context: Context,
        payeeVpa: String,
        payeeName: String,
        amount: Double,
        note: String
    ): Boolean {
        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", payeeVpa)
            .appendQueryParameter("pn", payeeName)
            .appendQueryParameter("am", String.format(Locale.US, "%.2f", amount))
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", note)
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri)
        val chooser = Intent.createChooser(intent, "Pay with")

        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
            true
        } else {
            Toast.makeText(
                context,
                "No UPI app found. Install GPay, PhonePe or Paytm to pay.",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }
}
