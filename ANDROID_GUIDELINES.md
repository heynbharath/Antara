# Android Architecture & Development Guidelines

This document outlines the architecture, coding standards, and platform implementation patterns for the Antara Android client. We follow a modular **Clean Architecture** pattern combined with **Jetpack Compose** for UI presentation.

---

## 1. Multi-Module Project Structure

To decouple compile boundaries and optimize build performance, the Android project is split into separate Gradle modules:

```
                  +--------------------------------+
                  |            :app                |
                  +--------------------------------+
                                   |
                   +---------------+---------------+
                   |                               |
                   v                               v
+--------------------------------+ +--------------------------------+
|       :feature:chat            | |     :feature:dashboard         |
+--------------------------------+ +--------------------------------+
                   |                               |
                   +---------------+---------------+
                                   |
                                   v
+-------------------------------------------------------------------+
|                           :core:model                             |
+-------------------------------------------------------------------+
                                   |
                   +---------------+---------------+
                   |                               |
                   v                               v
+--------------------------------+ +--------------------------------+
|          :core:network         | |          :core:database        |
|  (P2P, BLE, NAN, Wi-Fi Direct) | |    (SQLCipher Room database)   |
+--------------------------------+ +--------------------------------+
```

### Module Descriptions:
*   `:app`: The main entry point. Houses the dependency injection bindings (Hilt/Koin) and base `Application` class.
*   `:feature:*`: UI components and ViewModels (using Jetpack Compose).
*   `:core:model`: Shared data classes (Protobuf-generated entities).
*   `:core:network`: Houses the physical radio drivers (BLE GATT services, Wi-Fi P2P groups, Wi-Fi Aware state machines) and routing engine.
*   `:core:database`: The Room database engine layer encrypted with SQLCipher.

---

## 2. Background Execution & Services Model

Offline mesh networking requires continuous background operation. Because modern Android (API 26+) enforces strict battery saving restrictions, Antara uses a dual background execution approach:

### A. Core Network Daemon: Foreground Service
To prevent the OS from killing the BLE and Wi-Fi radio scanners while the app is in the background, we run a **Foreground Service** (`AntaraDaemonService`).
*   **Notification:** Displays a low-priority notification ("Antara network active").
*   **Wakelocks:** Acquires a partial wake lock ONLY when an active Wi-Fi Direct synchronization is in progress. The lock is immediately released once the sync completes.
*   **Lifecycle:** Binds to the system lifecycle. It monitors Bluetooth and Wi-Fi system states and triggers reconnection alerts.

### B. Deferred Execution: WorkManager
For heavy, non-time-critical processing (e.g., pruning old messages, auditing database integrity, generating offline search indexes), we schedule tasks using `WorkManager` with the constraints:
*   `Constraints.Builder().setRequiresBatteryNotLow(true).setRequiresDeviceIdle(true).build()`

---

## 3. Database Layer (Room + SQLCipher)

All database entities are cached in local tables via Google's Room persistence library, backed by SQLCipher:

```kotlin
@Database(
    entities = [MessageEntity::class, ThreadEntity::class, NeighborEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AntaraRoomDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun neighborDao(): NeighborDao
}

// Hilt/Dagger injection configuration
@Provides
@Singleton
fun provideDatabase(
    @ApplicationContext context: Context,
    passcodeProvider: DatabasePasscodeProvider
): AntaraRoomDatabase {
    // Generate decryption key from keystore passcode
    val factory = SupportOpenHelperFactory(passcodeProvider.getPasscode())
    return Room.databaseBuilder(
        context,
        AntaraRoomDatabase::class.java,
        "antara_secure.db"
    ).openHelperFactory(factory) // SQLCipher encryption factory
     .build()
}
```

---

## 4. BLE Connection Flow

To manage BLE connection stability in background threads:
1.  **Coroutines for Flow Control:** Use Kotlin Coroutines and asynchronous flows (`StateFlow`) to process BLE GATT updates sequentially, preventing GATT error code `133` caused by overlapping commands.
2.  **Thread Concurrency:** Move all socket and network packet processing out of the main thread using `Dispatchers.IO`.
3.  **Scope Lifecycle:** Ensure BLE discovery scanner callbacks are securely cancelled within `onDestroy` of the background daemon service to prevent memory leaks.
