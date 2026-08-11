# Product Requirements Document (PRD): Campus MVP

## 1. Executive Summary & Goal
The objective of the first Antara deployment is to enable a fully functional communication network across a university campus of **5,000+ students** without relying on internet connectivity, cellular networks, or centralized Wi-Fi networks.

The campus MVP acts as our proof-of-concept for the **Adaptive Transport Layer** and **Dynamic Routing Engine**.

---

## 2. Core User Stories & Features

### A. Zero-Config Onboarding
*   **User Story:** As a student, I want to download and open the app, input my name, and instantly see my classmates without registering an email, scanning phone numbers, or configuring Bluetooth.
*   **Requirements:**
    *   Dynamic local Ed25519 identity key generation on first launch.
    *   No input validation except display username (stored locally).
    *   Automated discovery engine scans and populates the "Nearby" list immediately.

### B. 1:1 Secure Messaging
*   **User Story:** As a student, I want to send an encrypted text message to a classmate sitting three lecture halls away.
*   **Requirements:**
    *   Double Ratchet end-to-end encryption.
    *   Unicast route discovery (AODV) searches the mesh for intermediary relay hops.
    *   Message queues in the DTN buffer if no active route is resolved, and transmits automatically when a node moves between the zones.

### C. Classroom / Department Announcements (Broadcasts)
*   **User Story:** As a Class Representative (CR) or lecturer, I want to broadcast a schedule change to everyone in the Department building without internet.
*   **Requirements:**
    *   **Cryptographic Role Verification:** Only authenticated node keys (e.g., verified by scanning the lecturer's QR code) can write to the Broadcast channel.
    *   **Flood-Routing Heuristic:** The message is flooded across all devices in range. Devices cache the message ID to prevent infinite packet loops.

### D. Emergency Alert Vector
*   **User Story:** As a campus administrator, I want to broadcast an emergency evacuation notice that reaches every single smartphone on campus instantly.
*   **Requirements:**
    *   Overrides standard transport queues. Evicts low-priority chat buffers.
    *   Uses maximum radio transmit power.
    *   Floods dynamically across both BLE and Wi-Fi networks concurrently.

---

## 3. UI Status Indicators (Sent vs. Delivered)

Antara rejects traditional "connecting..." banners. Senders see the absolute lifecycle of a message:

```
[Draft Message] -> User Taps Send
       |
       +--> [Status: Queued] Message is in local DTN storage (Waiting for hops).
       |
       +--> [Status: Sent] Message has left the device and has been successfully
       |                   forwarded to the first intermediate relay node.
       |
       +--> [Status: Delivered] Cryptographic ACK returned from recipient.
```

If a packet is in a `Sent` state, the sender knows it is actively flowing through the campus mesh, even if the recipient is currently on the other side of the campus.

---

## 4. Key Performance Indicators (KPIs) & Success Targets

To validate the architecture during the campus pilot:

*   **Mesh Density Coefficient:** The minimum number of active devices required to maintain a 90% message delivery rate across a 500-meter radius (Target: \(\ge 15\) active nodes).
*   **Delivery Latency:** Average hops and transit time for a 1:1 message across the campus library to the engineering block (Target: \(\le 4\) hops, \(\le 90\) seconds).
*   **Battery Overhead:** The rate of battery consumption for background scanning operations (Target: \(\le 1.8\%\) battery drain per hour on modern iOS/Android devices).
*   **DTN Delivery Success:** Percentage of queued messages successfully delivered within 4 hours via store-and-forward proxy nodes (Target: \(\ge 95\%\)).
