# Product & Engineering Principles

The Antara architecture is governed by eight immutable principles. These principles serve as the testing gate for all code changes, protocol designs, and product decisions.

---

## 1. Offline First
*   **The Rule:** The internet is a supplementary transport layer, not a core requirement.
*   **Engineering Impact:** All databases must reside on-device. The application must launch, allow reading and drafting of messages, manage peer discoveries, and perform cryptographic verifications without reaching any external server. 

## 2. Infrastructure Independent
*   **The Rule:** The network must form, operate, and heal without relying on central routing hardware, domain name servers, or cloud servers.
*   **Engineering Impact:** There are no centralized auth servers or coordination servers. Dynamic naming, identity verification, and message routing are handled peer-to-peer using cryptographic keys and mesh-native routing tables.

## 3. Zero Configuration
*   **The Rule:** No manual bluetooth pairing, no inputting IP addresses, and no complex setup screens.
*   **Engineering Impact:** The network layer automatically handles handshake negotiations, transport channel selection, and route finding in the background upon application startup. The user simply opens the application and immediately sees nearby peers and groups.

## 4. Local First
*   **The Rule:** Data belongs entirely to the device that created it.
*   **Engineering Impact:** Storage is local-first. State synchronization occurs directly between peer devices. Remote cloud backups are end-to-end encrypted and are treated as secondary storage targets, never as the source of truth.

## 5. Privacy by Default
*   **The Rule:** The network layer must not leak metadata, location data, or message contents to external or intermediate routing nodes.
*   **Engineering Impact:** All messages are end-to-end encrypted. Transient routing packets use ephemeral destination hashes that change periodically to prevent passive observers from mapping node movements or traffic patterns.

## 6. Battery Conscious
*   **The Rule:** Continuous background radio operation must not drain device battery.
*   **Engineering Impact:** The network stack manages radio duty cycles dynamically. Devices adjust their advertising and scanning intervals based on velocity, battery percentage, active charging state, and local node density.

## 7. Self Healing
*   **The Rule:** The loss of any individual router or relay node must not bring down the network.
*   **Engineering Impact:** Routing tables are dynamically updated. If a link breaks, the transport layer automatically searches for alternate multi-hop paths or falls back to store-and-forward methods.

## 8. Delay Tolerant
*   **The Rule:** Network partition is the expected state. Messages are stored, carried, and forwarded.
*   **Engineering Impact:** Based on Delay-Tolerant Networking (DTN) principles, packets are stored locally until an appropriate next-hop contact is discovered. If a sender and receiver are never in the same place at the same time, the message is physically carried across the partition by moving intermediary nodes.
