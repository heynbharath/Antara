# iOS Architecture & Development Guidelines

This document specifies the platform-level implementation patterns, architectural components, and system APIs for the Antara iOS client. The project utilizes **SwiftUI** for UI components and **Swift Concurrency (async/await, Actors)** to handle multi-threaded radio operations.

---

## 1. Swift Package Manager (SPM) Modular Layout

Rather than managing a complex Xcode project workspace with multiple subprojects, Antara is structured as a collection of localized Swift Packages inside the repository:

```
AntaraPackage/ (Local SPM Package)
├── Package.swift
├── Sources/
│   ├── AntaraCore/          (Shared Models, Protobuf Entities)
│   ├── AntaraCrypto/        (Noise XK, Double Ratchet, Keychain helpers)
│   ├── AntaraDatabase/      (SQLCipher & GRDB wrapper)
│   ├── AntaraNetwork/       (CoreBluetooth, Multipeer Connectivity drivers)
│   └── AntaraUI/            (SwiftUI components and Design System tokens)
```

---

## 2. Swift Concurrency & Thread Isolation

Mesh networking requires concurrent access to peripheral connection pools, routing structures, and local databases. To prevent data races and thread crashes:
*   **The Network Actor:** The `CoreBluetoothManager` is declared as a Swift `actor` to isolate its internal state (active `CBPeripheral` objects, write queues) from caller threads.
*   **MainActor UI:** SwiftUI ViewModels are decorated with `@MainActor` to ensure UI state updates execute exclusively on the main thread.
*   **Task Lifecycles:** Dynamic scanning loops use structured task groups that support cancelation propagation.

```swift
actor BluetoothTransportActor {
    private var activePeripherals: [UUID: CBPeripheral] = [:]
    
    func transmit(payload: Data, to peerId: UUID) async throws {
        guard let peripheral = activePeripherals[peerId] else {
            throw TransportError.peerNotConnected
        }
        // Write characteristic logic executes isolated on this actor's thread
        try await writeData(payload, to: peripheral)
    }
}
```

---

## 3. Background BLE Execution (CoreBluetooth)

Apple enforces strict controls on background applications. To maintain active background mesh networks on iOS:
1.  **Background Modes:** The `Info.plist` must declare the `UIBackgroundModes` containing `bluetooth-central` (scanning) and `bluetooth-peripheral` (advertising).
2.  **Explicit Service Scanning:** In background threads, `CBCentralManager.scanForPeripherals(withServices:...)` must be passed the explicit Antara Service UUID (e.g., `0xFD5A`). Passing `nil` (wildcard scanning) is blocked in background states.
3.  **State Preservation:** Initialize the `CBCentralManager` with a restoration identifier:
    
    ```swift
    let manager = CBCentralManager(
        delegate: self, 
        queue: bluetoothQueue, 
        options: [CBCentralManagerOptionRestoreIdentifierKey: "AntaraCentralRestorationKey"]
    )
    ```
    
    This ensures that if the system kills the app in the background, the OS will automatically restart it in the background if a matching BLE advertising packet is detected.

---

## 4. Apple Multipeer Connectivity (WiFi Direct Equivalent)

Since Apple devices do not support standard Wi-Fi Direct APIs, Antara uses Apple's **Multipeer Connectivity Framework** to bridge high-throughput payloads on iOS:
*   **MCNearbyServiceAdvertiser** & **MCNearbyServiceBrowser**: Used to dynamically scan and establish local Wi-Fi / Bluetooth peer connections.
*   **Service Type:** Limited to 15 characters, alphanumeric/dashes (e.g., `antara-sync`).
*   **Payload Elevation:** BLE triggers MC session handshakes when payload transfers exceed BLE capacity.
*   **Cross-Platform Constraints:** Because Multipeer Connectivity is proprietary to Apple, iOS-to-Android high-payload synchronization uses Wi-Fi Hotspots or Wi-Fi Aware.
