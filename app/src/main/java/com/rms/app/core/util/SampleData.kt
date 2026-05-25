package com.rms.app.core.util

import com.rms.app.core.model.entities.*

/**
 * Provides sample/fake data for previews and initial testing.
 */
object SampleData {

    val sampleProperty = Property(
        id = 1,
        name = "My Property",
        address = "123 Main Street, Mumbai",
        type = "residential"
    )

    val sampleRooms = listOf(
        Room(id = 1, propertyId = 1, roomNumber = "A-101", floor = "1st", monthlyRent = 12000.0, securityDeposit = 24000.0, status = "occupied"),
        Room(id = 2, propertyId = 1, roomNumber = "A-102", floor = "1st", monthlyRent = 10000.0, securityDeposit = 20000.0, status = "occupied"),
        Room(id = 3, propertyId = 1, roomNumber = "B-201", floor = "2nd", monthlyRent = 15000.0, securityDeposit = 30000.0, status = "occupied"),
        Room(id = 4, propertyId = 1, roomNumber = "B-202", floor = "2nd", monthlyRent = 8000.0, securityDeposit = 16000.0, status = "available"),
        Room(id = 5, propertyId = 1, roomNumber = "C-301", floor = "3rd", monthlyRent = 18000.0, securityDeposit = 36000.0, status = "available"),
    )

    val sampleTenants = listOf(
        Tenant(
            id = 1, roomId = 1, name = "Rahul Sharma", phone = "9876543210",
            whatsappNumber = "9876543210", email = "rahul@email.com",
            moveInDate = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000,
            advanceDeposit = 24000.0, isActive = true, notes = "Reliable tenant"
        ),
        Tenant(
            id = 2, roomId = 2, name = "Priya Patel", phone = "9876543211",
            whatsappNumber = "9876543211", email = "priya@email.com",
            moveInDate = System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000,
            advanceDeposit = 20000.0, isActive = true
        ),
        Tenant(
            id = 3, roomId = 3, name = "Amit Kumar", phone = "9876543212",
            whatsappNumber = "9876543212",
            moveInDate = System.currentTimeMillis() - 45L * 24 * 60 * 60 * 1000,
            advanceDeposit = 30000.0, isActive = true
        ),
    )

    val samplePayments = listOf(
        Payment(id = 1, tenantId = 1, amount = 12000.0, type = "RENT", mode = "UPI", paymentDate = System.currentTimeMillis() - 23L * 24 * 60 * 60 * 1000, forMonth = 5, forYear = 2026),
        Payment(id = 2, tenantId = 1, amount = 12000.0, type = "RENT", mode = "CASH", paymentDate = System.currentTimeMillis() - 53L * 24 * 60 * 60 * 1000, forMonth = 4, forYear = 2026),
        Payment(id = 3, tenantId = 2, amount = 10000.0, type = "RENT", mode = "BANK_TRANSFER", paymentDate = System.currentTimeMillis() - 51L * 24 * 60 * 60 * 1000, forMonth = 4, forYear = 2026),
    )

    val sampleElectricityReadings = listOf(
        ElectricityReading(id = 1, tenantId = 1, previousReading = 1200.0, currentReading = 1250.0, unitsConsumed = 50.0, ratePerUnit = 8.0, totalAmount = 400.0, readingDate = System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000, forMonth = 5, forYear = 2026, isPaid = true),
        ElectricityReading(id = 2, tenantId = 2, previousReading = 930.0, currentReading = 980.0, unitsConsumed = 50.0, ratePerUnit = 8.0, totalAmount = 400.0, readingDate = System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000, forMonth = 5, forYear = 2026, isPaid = false),
    )
}
