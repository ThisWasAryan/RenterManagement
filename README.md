# Rent Management System (RMS)

Welcome to the **Rent Management System (RMS)**! RMS is a complete, offline-first digital ledger built for landlords, property managers, and small property owners. Say goodbye to scattered paper notebooks, lost receipts, and messy WhatsApp logs. RMS puts everything you need to manage your rental properties directly into your pocket.

---

## Download

The latest stable Android APK is available in the Releases section.

**Download:** RMS-v3-stable.apk *(Available in GitHub Releases)*

---

## Features at a Glance

RMS is built to handle all the practical, day-to-day operations of managing tenants:

### Tenant & Room Management
- **Add Tenants Easily:** Store essential details like Move-In Date, Aadhaar/PAN, Security Deposit, and Contact Information.
- **Room Assignments:** Manage multiple rooms/properties and effortlessly assign tenants to them.
- **Deactivation:** Safely deactivate a tenant when they move out without losing their historical records.

### Automated Rent Logic
- **Smart Billing Cycles:** Rent is smartly tracked based on the tenant's exact **Move-In Date** (not arbitrary calendar months). A tenant's cycle only becomes "Due" after a full month has passed.
- **Precise Balances:** The system accurately tracks exactly how much rent is pending, preventing you from manually calculating months and partial payments.
- **Advance Payments:** Tenants can pay rent in advance, and the system correctly allocates it to the upcoming cycle while strictly preventing accidental duplicate advance entries.

### ⚡ Electricity & Utility Tracking
- **Meter Readings:** Log previous and current meter readings to automatically calculate electricity consumed.
- **Photo Evidence:** Snap and attach photos of the electricity meter for undeniable proof.
- **Custom Rates:** Configure per-unit electricity rates for each tenant or property.

### One-Tap WhatsApp Reminders
- **Template Messaging:** Send fully formatted WhatsApp reminders (Rent Due, Electricity Bills, Payment Confirmations) directly from the app.
- **Dynamic Placeholders:** Messages automatically populate with the tenant's name, precise pending rent, exact units consumed, and current/previous meter readings. 

### Digital Document Vault
- **Attach Files:** Upload PDFs, documents, and photos directly from your phone.
- **Organized Storage:** Store Rental Agreements, ID Proofs, and more on a per-tenant basis for instant access anywhere.

### Beautiful & Accessible UI
- **Modern Design:** Built with modern Android standards featuring beautiful typography, easy-to-read cards, and intuitive navigation.
- **Dark Mode Support:** Fully supports Light, Dark, and System-default themes.

---

## Technical Details & Architecture

RMS is a native Android application built with modern architecture components and robust engineering practices. It relies on a local-first philosophy to ensure complete offline functionality and zero data privacy concerns.

### Tech Stack
- **UI Framework:** Kotlin + Jetpack Compose (Material 3)
- **Architecture Pattern:** MVVM (Model-View-ViewModel) + Clean Architecture principles
- **Local Database:** Room persistence library + DAOs (Data Access Objects)
- **Dependency Injection:** Hilt / Dagger
- **State Management:** StateFlow / SharedFlow / Coroutines for reactive, async programming
- **Preferences:** Jetpack DataStore (Preferences)
- **Image/Document Handling:** Coil (for image loading/caching) and Android `ContentResolver` (for local URI persistence)

### V3 Stable Improvements
Version 3 finalizes the core workflows to ensure the app is highly robust for real-world deployments:
- **Billing Engine Overhaul:** Reworked rent calculations from arbitrary calendar-month validations to true elapsed billing cycles using `DateUtils` utilities spanning off the tenant's `moveInDate`. 
- **Instant Reactive UI:** Upgraded Compose `LaunchedEffect` hooks and Flow combiners in ViewModels (like `HomeViewModel`) to instantly refresh summary metrics and lists without requiring a restart.
- **UI State Protections:** Implemented strict ViewModel rules to enforce correct UI states (e.g., locking out the "Record Payment" bottom sheet if advance cycles are fully paid, preventing overpayment).
- **Advanced Template System:** Enhanced the `WhatsAppHelper` integration to dynamically resolve deep variables (like `{previousReading}` and `{currentReading}`) before firing intents.

### Building from Source

To compile and build RMS from source:

1. Clone the repository and open the project in **Android Studio**.
2. Sync the Gradle dependencies.
3. To build a debug APK, run the following Gradle task via the terminal or Android Studio's Gradle panel:
   ```bash
   ./gradlew assembleDebug
   ```
4. Or, to generate a signed release APK:
   ```bash
   ./gradlew assembleRelease
   ```
5. Deploy to an emulator or physical device running Android API 26 or higher.

---

### Made with love and AI tools by @ThisWasAryan
