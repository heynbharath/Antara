# Privacy Model & Metadata Protection

Privacy is a core architectural pillar of Antara. In a peer-to-peer network where message delivery relies on community devices routing payloads, protecting user identity, message metadata, and physical location requires rigorous architectural insulation.

---

## 1. Zero-Metadata Routing Envelopes

In centralized chat systems, the server knows who is speaking to whom, when, and how often. Even if message payloads are end-to-end encrypted, this metadata is highly revealing.

Antara removes this centralized point of collection. To prevent intermediate relay nodes from compiling a communication map, we enforce **zero-metadata envelopes**:

*   **Destination Obfuscation:** The outer routing envelope does not contain the readable names of the sender or receiver. It contains the 256-bit NodeID hashes.
*   **Packet Padding:** To prevent eavesdroppers from guessing the content type (e.g., text vs. media files) by analyzing payload lengths, the transport layer pads all routing packets to fixed-size chunks (e.g., standard 1024-byte frames).
*   **Link-Level Token Rotation:** Senders and receivers negotiate rotating connection tokens for each hop. A physical device forwarding a packet only knows the immediate hop it received the bytes from and the next hop it is forwarding the bytes to. It cannot reconstruct the origin node or the final target node of a multi-hop path.

---

## 2. Location Privacy & Beacon Obfuscation

Because BLE and Wi-Fi advertising require broadcasting identifiers, passive radio sniffers (e.g., static nodes positioned around a campus) could track a user's physical movements by mapping their MAC address or discovery beacons.

### Mitigations:
1.  **OS-Level MAC Rotation:** Both Android and iOS randomize hardware Bluetooth MAC addresses dynamically at the operating system level (typically every 15 minutes).
2.  **Discovery Beacon Rotation:** Antara coordinates its discovery beacons with this cycle. The cryptographic discovery tokens update in sync with the MAC address rotation.
3.  **No GPS Tracking:** Antara does not access or transmit raw GPS coordinates. Spatial relationships are computed relatively using signal parameters (RSSI and round-trip time measurements) to estimate hop distance without mapping physical global locations.

---

## 3. Local Storage Security: SQLCipher

Because data belongs entirely to the local device (Local-First principle), a compromised or stolen phone represents a physical threat to local chat history.

*   **Database Encryption:** All messaging logs, contact details, public keys, and routing caches are stored in a local SQLite database compiled with **SQLCipher (AES-256-CBC encryption)**.
*   **Key Derivation:** The database encryption key is derived using PBKDF2 from a local passcode.
*   **Keychain/Keystore Binding:** The master decryption key is stored securely within the hardware-backed keystore (Android Keystore System or iOS Keychain Services). The database cannot be decrypted if it is copied off the physical device storage.

---

## 4. Ephemeral Message Retention

To limit exposure if a device is physically compromised, Antara implements automated message cleanup:
*   **Default Time-To-Live (TTL):** Messages are configured with a default local retention period (e.g., 30 days). Once the TTL expires, the SQLCipher database executes a secure overwrite step to delete the record.
*   **Relay Payload Isolation:** Intermediate relays that store messages in transit (DTN store-and-forward buffer) are restricted from accessing the payload, and payloads are deleted immediately upon receiving the target node's `ACK` frame.
