package com.rms.app.core.model.enums

enum class ReminderType(val displayName: String) {
    RENT_DUE("Rent Due"),
    OVERDUE("Overdue"),
    AGREEMENT_EXPIRY("Agreement Expiry"),
    CUSTOM("Custom")
}
