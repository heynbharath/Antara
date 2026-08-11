# Engineering & Product Roadmap

Antara's engineering path transitions from building a specialized campus messaging client to providing a general-purpose, decentralized offline-first transport SDK.

```mermaid
gantt
    title Antara Development Timeline
    dateFormat  YYYY-MM
    section Phase 1: Local Comms
    1:1 & Group Messaging, P2P Mesh  :active, des1, 2026-08, 3m
    section Phase 2: Campus Intelligence
    Broadcasting, Dept Channels, Alerts  : des2, 2026-11, 3m
    section Phase 3: Spatial Layer
    Proximity Discovery, Occupancy Mapping : des3, 2027-02, 3m
    section Phase 4: Edge Services
    File Sync, Voice, Local AI Summary : des4, 2027-05, 4m
    section Phase 5: Network SDK
    Multi-app SDK Integration, System Daemon : des5, 2027-09, 6m
```

---

## Phase 1: Local Communication (Core Transport & Mesh)
**Objective:** Establish secure, decentralized link-layer communication, discover nearby peers, and route messages across dynamic multi-hop pathways.

*   **P2P Transport Abstraction:** Create uniform drivers for Bluetooth Low Energy (BLE), Wi-Fi Direct, and Wi-Fi Aware (NAN).
*   **1:1 Messaging:** Cryptographically secure direct messaging with ephemeral public keys.
*   **Local Group Mesh:** Support multi-peer chatrooms using epidemic routing algorithms.
*   **Store-and-Forward Implementation:** Simple queueing system enabling devices to store and carry messages until recipient contact is made.

## Phase 2: Campus Intelligence (Scalable Information Distribution)
**Objective:** Scale the network to handle local public announcements, group divisions, and low-latency broadcast vectors.

*   **Classroom Broadcasts:** One-to-many communication channel permitting authorized devices (e.g., lecturers) to push messages.
*   **Class Representative (CR) Announcements:** Role-verified local channels to sync schedules.
*   **Lost-and-Found Ledger:** A distributed, tamper-resistant catalog of announcements.
*   **Emergency Alert Vector:** A high-priority flood-routing channel that overrides transport congestion to push emergency messages across all available nodes.

## Phase 3: Spatial Layer (Context-Aware Networks)
**Objective:** Layer geographical and architectural spatial intelligence onto the mesh network without sacrificing location privacy.

*   **Proximity-Based Discovery:** Allow discovery of study groups and interest circles based on hop distance and signal triangulation.
*   **Privacy-Preserving Occupancy Mapping:** Aggregate local beacon density to estimate how busy areas (libraries, dining halls) are, without tracking individual user identities.
*   **Indoor Wayfinding Beacons:** Implement offline, collaborative anchor-based location services.

## Phase 4: Edge Services (Rich Media & Local Computation)
**Objective:** Upgrade the data plane to support high-payload content and local resource processing.

*   **P2P File Transfer:** Support segmented, multi-source download systems for large media files (lecture slides, notes).
*   **Voice Notes Integration:** Highly compressed codec payloads optimized for constrained transport bounds.
*   **Local AI Summarization:** Run local SLM models (e.g., Gemini Nano) to summarize active threads or broadcast notifications.
*   **Offline Search Indexing:** A distributed inverted index mapping local group history across devices.

## Phase 5: Antara Network SDK (Infrastructure Transition)
**Objective:** Decouple the networking stack from the Antara client application and open it as general infrastructure.

*   **Transport Abstraction SDK:** Release library packages for Kotlin (Android) and Swift (iOS) to let third-party applications use Antara as their transport.
*   **Platform System Daemons:** Create background platform services that share a single mesh connection pool across multiple apps.
*   **Decentralized Plugin Engine:** Enable third-party developers to load custom edge services and data sync pipelines directly into the routing layer.
