package com.rms.app.core.model.enums

enum class PaymentStatus(val displayName: String) {
    PAID("Paid"),
    PARTIAL("Partial"),
    UNPAID("Unpaid"),
    OVERDUE("Overdue")
}
