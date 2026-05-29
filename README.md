# Rent Management System (RMS)

Rent Management System (RMS) is a comprehensive, offline-first Android application built natively in Kotlin. It is designed to modernize and simplify landlord operations by replacing traditional paper notebooks with a fast, structured, and technically robust mobile solution.

## Project Overview & Purpose
Managing rental properties traditionally involves scattered spreadsheets, physical receipts, manual electricity calculations, and untracked WhatsApp messages. The purpose of RMS is to provide a single source of truth for all landlord activities. It handles everything from property organization and document filing to smart payment tracking and automated WhatsApp reminders.

## Core Philosophy
- **Offline-First:** No reliance on cloud synchronicity. Your data lives on your device, ensuring instantaneous load times, total privacy, and absolute operational reliability even without an internet connection.
- **Strict Data Consistency:** Deeply interconnected relational database architectures ensure that payments, properties, rooms, electricity, and documents are seamlessly interlinked.
- **Frictionless UX:** Tailored specifically for fast data entry and immediate readability, avoiding excessive configuration steps in favor of smart, automated defaults.

---

## Key Features

### 🏢 Room & Property Management
- Hierarchical property and room structuring.
- Strict room occupancy validation preventing double-booking of active tenants.
- Flexible room setups with configurable deposits, base rents, and specific room details (floor, notes).

### 💸 Smart Rent-Cycle Logic & Payment Tracking
- Intelligent move-in date anchor logic calculates precise billing cycles.
- Dynamic dashboard displaying aggregated "Amount Pending" (combining outstanding rent and electricity) and "Amount Collected".
- Support for partial payments and 1-month advance pre-payments.
- Robust tracking of multiple payment methods (Cash, UPI, Bank Transfer, Cheque, Other).

### ⚡ Integrated Electricity Billing
- Track per-room electricity meter readings.
- Automatic consumption calculation based on configurable per-unit rates.
- Electricity dues are directly integrated into the global pending dues system.

### 📁 Meaningful Document Management
- Categorized, expandable filing cabinet UX for ID documents (Aadhaar, PAN, Passport, DL), Rental Agreements, and Receipts.
- Direct association of documents to specific tenants for immediate retrieval.
- Integrated file and gallery picker for quick uploads.

### 💬 WhatsApp Reminder System
- One-click template-based WhatsApp reminders.
- Dynamic message population including tenant name, specific pending amounts, due cycles, and electricity details.

---

## Screens & Workflows Overview
1. **Home/Dashboard:** Real-time financial summary and immediate actionable list of tenants with pending dues. Smart routing handles exactly what the tenant owes (Rent, Electricity, or Both).
2. **Properties:** A structured view of properties and their contained rooms.
3. **Tenants:** The core directory containing active and past tenants, enabling robust editing, deep payment histories, and document access.
4. **Documents:** A structured categorization of all global uploads, allowing landlords to quickly find any specific lease or ID.
5. **Settings:** Operational configurations, custom room creation, WhatsApp template editing, and database management.

---

## Technology Stack & Architecture

### Stack
- **Kotlin:** 100% Native Android development.
- **Jetpack Compose:** Modern, declarative UI toolkit.
- **Room Database:** SQLite abstraction for robust, relational offline storage.
- **Hilt (Dagger):** Dependency Injection framework ensuring scalable and modular architecture.
- **Coroutines & Flow:** Asynchronous data streaming and reactive UI updates.

### Architecture Overview
RMS follows the **MVVM (Model-View-ViewModel)** architectural pattern. 
- **Data Layer:** Utilizes Room DAOs and Repositories to handle complex SQL joins (e.g., `TenantWithRooms`, `TenantWithPayments`).
- **Domain/ViewModel Layer:** Manages strict validation rules (like occupancy blocks, smart pre-payment calculation, and electricity amount aggregation).
- **UI Layer:** Compose functions reactively observe `StateFlow` structures, ensuring the UI is always perfectly synced with the underlying SQLite database.

---

## Version Evolution

The project has matured significantly across major iterations to reach its current stable state:

* **V1:** Initial UI foundation. Established the basic Compose architecture and early tenant management capabilities.
* **V2:** Workflow completion. Introduced relational payment systems, the electricity calculation logic, raw document uploads, and the WhatsApp integration system.
* **V3:** Operational polish and stability. Implemented smart payment workflows, heavily organized the document filing system, enforced room/property relationship consistency, introduced a reactive dashboard, and finalized production-level landlord workflows.
* **v3.1** It is a major refinement and stabilization update focused on operational correctness, workflow polish, dashboard accuracy, and long-term landlord usability.

---

## Build & Installation Instructions

### Android Version Requirements
- **Minimum SDK:** API 26 (Android 8.0 Oreo)
- **Target SDK:** API 34 (Android 14)

### Building from Source
1. Clone the repository:
   ```bash
   git clone https://github.com/ThisWasAryan/RentManagementSystem.git
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and ensure the latest SDK build tools are installed.
4. Build the project using:
   ```bash
   ./gradlew assembleDebug
   ```

### APK Installation
For landlords looking to use the app immediately without building from source:
1. Navigate to the **Releases** section on this GitHub repository.
2. Download the `RMS-v3.1-stable.apk` file.
3. Transfer the file to your Android device and install it (ensure "Install from Unknown Sources" is enabled in your device settings).

---

## Future Roadmap
- Cloud Backup & Restore via Google Drive (Opt-in).
- Deep financial analytics, charts, and yearly income/expense reporting.
- PDF Receipt Generation for tenants.


---
*Made with ❤️ by Aryan Raj*
