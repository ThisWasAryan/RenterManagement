package com.rms.app.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppHelper {

    fun sendRentReminder(
        context: Context,
        phoneNumber: String,
        tenantName: String,
        amount: Double,
        month: String
    ) {
        val message = buildString {
            append("Hi $tenantName,\n\n")
            append("This is a friendly reminder that your rent of ")
            append(CurrencyUtils.formatAmountCompact(amount))
            append(" for $month is due.\n\n")
            append("Please make the payment at your earliest convenience.\n\n")
            append("Thank you! 🏠")
        }
        openWhatsApp(context, phoneNumber, message)
    }

    fun sendPaymentConfirmation(
        context: Context,
        phoneNumber: String,
        tenantName: String,
        amount: Double,
        month: String
    ) {
        val message = buildString {
            append("Hi $tenantName,\n\n")
            append("Your rent payment of ")
            append(CurrencyUtils.formatAmountCompact(amount))
            append(" for $month has been received.\n\n")
            append("Thank you! ✅")
        }
        openWhatsApp(context, phoneNumber, message)
    }

    fun sendOverdueReminder(
        context: Context,
        phoneNumber: String,
        tenantName: String,
        amount: Double,
        pendingBalance: Double
    ) {
        val message = buildString {
            append("Hi $tenantName,\n\n")
            append("Your rent payment is overdue. ")
            append("Pending balance: ")
            append(CurrencyUtils.formatAmountCompact(pendingBalance))
            append("\n\nPlease clear the dues at the earliest.\n\n")
            append("Thank you! 🏠")
        }
        openWhatsApp(context, phoneNumber, message)
    }

    fun sendCustomMessage(context: Context, phoneNumber: String, message: String) {
        openWhatsApp(context, phoneNumber, message)
    }

    private fun openWhatsApp(context: Context, phoneNumber: String, message: String) {
        val formattedNumber = formatPhoneNumber(phoneNumber)
        val url = "https://api.whatsapp.com/send?phone=$formattedNumber&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser if WhatsApp is not installed
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        }
    }

    private fun formatPhoneNumber(phone: String): String {
        val cleaned = phone.replace("[^\\d+]".toRegex(), "")
        return if (cleaned.startsWith("+")) cleaned
        else if (cleaned.startsWith("91")) "+$cleaned"
        else "+91$cleaned"
    }
}
