# RMS — Rent Management System (V2)

RMS is an Android application for landlords and small property owners to manage tenants, rent operations, utility readings, reminders, and related records in one place.

## Current Release Status

**Version 2 (V2)** is the current major release. This version addresses all critical limitations found in V1 and introduces several new features for a complete property management experience.

## V2 Improvements & Comparison

V2 represents a significant leap from the initial release, focusing on workflow reliability and feature completeness.

| Feature | Version 1 (V1) | Version 2 (V2) |
| :--- | :--- | :--- |
| **Rent Payments** | Recording not fully functional | **Fully functional** recording flow with support for partial payments, payment modes, and month selection. |
| **Document Management** | Metadata only; no file uploads | **Full support** for uploading images (Gallery/Camera) and PDF/Doc files for agreements and IDs. |
| **User Interface** | UI alignment issues in key forms | **Refined form layouts** with better grouping, sectioning (using Section Headers), and consistent Material 3 spacing. |
| **Theme Support** | Theme toggle not working | **Fully functional Dark/Light/System theme** switching via Settings. |
| **Utility Tracking** | Basic electricity reading | **Enhanced Electricity management** with rate configuration and meter photo attachments. |
| **Communications** | Manual reminders only | **WhatsApp Templates** for quick automated messaging to tenants for rent due or receipts. |

## Key Capabilities

- **Tenant & Property Management**: Track multiple properties and rooms with detailed tenant profiles including Aadhaar/PAN details.
- **Payment History**: Full history of rent payments with status tracking (Paid/Pending/Overdue).
- **Electricity Reading Management**: Record meter readings, calculate bills based on custom rates, and store proof images.
- **Document Vault**: Securely store digital copies of rental agreements, ID cards, and other essential documents.
- **Reminder & Follow-up**: Automated and manual reminders for rent due dates and agreement expiries.
- **Offline-First Storage**: All data is stored locally using Room Database, ensuring 100% privacy and offline availability.

## Technology Stack

- **Kotlin + Jetpack Compose**: Modern declarative UI framework.
- **MVVM Architecture**: Clean, scalable, and testable code structure.
- **Room Database + DAO**: Robust local data persistence.
- **Hilt**: Modern dependency injection.
- **DataStore**: For user preferences like Theme modes and default rates.
- **WorkManager**: Reliability for background reminder scheduling.
- **Coil**: Smooth image loading for document previews.

## Build and Run

1. Open the project in **Android Studio**.
2. Sync Gradle dependencies.
3. Build debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run on an emulator or physical device (API 26+).

## License

This project is for management of rental properties.
