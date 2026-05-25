# RMS — Rent Management System

RMS is an Android application for landlords and small property owners to manage tenants, rent operations, utility readings, reminders, and related records in one place.

## Current Release Status

Version 1 (V1) is stable and available in Releases. Core workflows are operational for day-to-day use.

## Key Capabilities

- Tenant and room management
- Payment history tracking
- Electricity reading management
- Reminder and follow-up workflows
- Document metadata management
- Offline-first local data storage

## Technology Stack

- Kotlin + Jetpack Compose
- MVVM architecture
- Room Database + DAO layer
- Hilt for dependency injection
- DataStore for preferences
- WorkManager for background reminder scheduling

## Known Limitations in V1

The following items are currently limited and are planned for V2 improvements:

- Recording rent payments is not fully functional in all flows.
- File attachment/upload flows (images and documents) are currently unavailable.
- One form-layout flow has UI alignment issues.
- Theme toggle (dark/light mode switching) is not working as expected.

## Planned for V2

- Reliable payment recording flow
- Media and document upload support
- UI/UX refinements in affected forms
- Fully functional theme mode switching
- Additional feature and stability enhancements

## Build and Run

1. Open the project in Android Studio.
2. Sync Gradle dependencies.
3. Build debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run on emulator or physical device (API 26+).

## Notes

This README intentionally focuses on user-relevant behavior and release-level information.
