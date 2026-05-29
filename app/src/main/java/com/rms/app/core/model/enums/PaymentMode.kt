package com.rms.app.core.model.enums

enum class PaymentMode(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    BANK_TRANSFER("Bank Transfer"),
    CHEQUE("Cheque"),
    OTHER("Other")
}
