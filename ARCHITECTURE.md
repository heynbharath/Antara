# System Architecture & Layered Protocol Stack

Antara utilizes a highly decoupled, modular 6-layer architecture. Rather than tying system behavior to a specific protocol or platform API, we define strict interfaces between layers. This ensures that hardware protocols, routing strategies, or encryption schemes can be updated independently without breaking downstream components.

```
+-----------------------------------------------------------------+
|                       1. Application Layer                      |
|           (Jetpack Compose UI, Notification Engine, SDK)         |
+-----------------------------------------------------------------+
                                 |  [Database Queries / Broadcasts]
                                 v
+-----------------------------------------------------------------+
|                       2. Messaging Layer                        |
|            (Conflict-Free Replicated Data Types, CRDTs)         |
+-----------------------------------------------------------------+
                                 |  [State Vectors / Change Sets]
                                 v
+-----------------------------------------------------------------+
|                        3. Routing Layer                         |
|           (Epidemic Routing, Gossip Protocols, DTN Buffer)       |
+-----------------------------------------------------------------+
                                 |  [Packets & Handshake Buffers]
                                 v
+-----------------------------------------------------------------+
|                       4. Transport Layer                        |
|        (Adaptive Connection Manager, Frame Serialization)       |
+-----------------------------------------------------------------+
                                 |  [Raw Data Frame Slices]
                                 v
+-----------------------------------------------------------------+
|                       5. Discovery Layer                        |
|          (Dynamic Service Advertisements, Identity Tokens)      |
+-----------------------------------------------------------------+
                                 |  [Beacon Frames & Scan Matches]
                                 v
+-----------------------------------------------------------------+
|                       6. Hardware Layer                         |
|             (BLE, Wi-Fi Aware/NAN, Wi-Fi Direct, LAN)           |
+-----------------------------------------------------------------+
```

---

## The 6-Layer Stack Specifications

### 1. Application Layer
*   **Responsibility:** Provides the user-facing interfaces, drafts messages, triggers localized alerts, and runs system notifications.
*   **Downstream Interface:** Communicates with the Messaging Layer by posting messages to a local thread queue or querying current thread states.

### 2. Messaging Layer
*   **Responsibility:** Maintains state consistency across a decentralized cluster of devices. It uses Conflict-Free Replicated Data Types (CRDTs) to represent chat histories and thread structures.
*   **Core Primitives:**
    *   *Merkle Directed Acyclic Graphs (DAGs)* to track causal history.
    *   *Vector Clocks* to establish causal ordering of events.
    *   *CRDT Sets* to resolve concurrent edits or deletes.
*   **Downstream Interface:** Serializes state differences into sync requests and passes them to the Routing Layer.

### 3. Routing Layer
*   **Responsibility:** Resolves paths and propagates data across multi-hop topologies. It manages a local Delay-Tolerant Networking (DTN) buffer to hold packets when next-hops are unreachable.
*   **Core Primitives:**
    *   *Neighbor Table:* Maintains a list of immediately accessible peers, their signal strength (RSSI), battery levels, and latency metrics.
    *   *Routing Heuristics:* Decides whether to flood, gossip, or execute a targeted route search (e.g., AODV-style) based on packet metadata.
*   **Downstream Interface:** Sends wrapped routing envelopes (packets with headers specifying source/destination hashes and hop limits) to the Transport Layer.

### 4. Transport Layer
*   **Responsibility:** Negotiates link-layer connections, handles framing/serialization, and balances throughput and power consumption across multiple radios.
*   **Core Primitives:**
    *   *Adaptive Transport Manager:* Actively switches between Bluetooth Low Energy (BLE) and Wi-Fi Direct depending on message sizes and signal metrics.
    *   *Frame Handler:* Breaks down large messages into standard transport frames and checks integrity using checksums.
*   **Downstream Interface:** Calls target sockets or write characteristics exposed by active connections in the Hardware Layer.

### 5. Discovery Layer
*   **Responsibility:** Continuously announces local presence and scans for nearby Antara nodes without drawing excessive battery power.
*   **Core Primitives:**
    *   *Cryptographic Advertisements:* Generates rotating ephemeral tokens to represent device presence while hiding identities from passive eavesdroppers.
    *   *Scan Scheduler:* Implements randomized, duty-cycled scan schedules to prevent radio collision and conserve power.
*   **Downstream Interface:** Interacts directly with the platform's BLE Advertising and Wi-Fi Scanning system APIs.

### 6. Hardware Layer
*   **Responsibility:** The physical radios on the smartphone (Bluetooth Low Energy, Bluetooth Classic, Wi-Fi Direct, Wi-Fi Aware/NAN, local Hotspots).
*   **Downstream Interface:** Platform-specific APIs (Android Core Bluetooth, iOS CoreBluetooth, Wi-Fi P2P APIs).

---

## End-to-End Data Flow (Message Transmission Example)

```
[User Taps Send]
       |
       v
Application Layer: Creates message object (Text: "Hi", Sender: Alice, Recipient: Bob).
       |
       v
Messaging Layer: Wraps message in a CRDT update node, adds Alice's vector clock, appends to the Merkle DAG.
       |
       v
Routing Layer: Creates a packet envelope. Consults Neighbor Table. Finds no path to Bob.
               Stores packet in the DTN queue.
       |
       v (Device moves; neighbor table detects Bob's proxy node Charlie)
       |
Routing Layer: Evaluates Charlie as a valid relay node. Serializes packet and hands to Transport Layer.
       |
       v
Transport Layer: Evaluates connection to Charlie. Initiates Wi-Fi Direct handshake (as payload is >50KB).
       |
       v
Discovery Layer: Resolves cryptographic tokens to ensure Charlie is a trusted network participant.
       |
       v
Hardware Layer: Writes payload bytes across the physical Wi-Fi Direct radio link.
```
