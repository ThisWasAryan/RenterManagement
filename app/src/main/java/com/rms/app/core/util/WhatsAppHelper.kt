package com.rms.app.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppHelper {

    fun formatAndSendMessage(
        context: Context,
        phoneNumber: String,
        template: String,
        args: Map<String, String>
    ) {
        var message = template
        args.forEach { (key, value) ->
            message = message.replace("{$key}", value)
        }
        openWhatsApp(context, phoneNumber, message)
    }

    // Fallbacks if templates are not set yet
    fun getDefaultRentReminderTemplate(): String = 
        "Hi {tenantName},\n\nThis is a friendly reminder that your rent of {amount} for {month} is due.\n\nPlease make the payment at your earliest convenience.\n\nThank you! \uD83C\uDFE0"
        
    fun getDefaultPaymentConfirmationTemplate(): String =
        "Hi {tenantName},\n\nYour rent payment of {amount} for {month} has been received.\n\nThank you! ✅"

    fun getDefaultOverdueReminderTemplate(): String =
        "Hi {tenantName},\n\nYour rent payment is overdue. Pending balance: {pendingBalance}\n\nPlease clear the dues at the earliest.\n\nThank you! \uD83C\uDFE0"

    fun getDefaultElectricityReminderTemplate(): String =
        "Hi {tenantName},\n\nYour electricity bill for {month} is {amount}.\n\nPrevious Reading: {previousReading}\nCurrent Reading: {currentReading}\nUnits Used: {units}\n\nPlease pay at your earliest convenience.\n\nThank you! ⚡"

    fun getDefaultCombinedReminderTemplate(): String =
        "Hi {tenantName},\n\nYour total dues for {month} are {totalAmount}.\n\nRent: {rentAmount}\nElectricity: {electricityAmount}\nPrevious Reading: {previousReading}\nCurrent Reading: {currentReading}\nUnits Used: {units}\n\nPlease make the payment.\n\nThank you! \uD83C\uDFE0⚡"

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
