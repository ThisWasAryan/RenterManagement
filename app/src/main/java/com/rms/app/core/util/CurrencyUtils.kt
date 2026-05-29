package com.rms.app.core.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val indianFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun formatAmount(amount: Double): String {
        return indianFormat.format(amount)
    }

    fun formatAmountCompact(amount: Double): String {
        return "₹${String.format(Locale.getDefault(), "%,.0f", amount)}"
    }

    fun formatAmountWithDecimals(amount: Double): String {
        return "₹${String.format(Locale.getDefault(), "%,.2f", amount)}"
    }
}
