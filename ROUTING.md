# Routing Engine & Mesh Protocols

Antara's routing engine is its core differentiator. Unlike simple client-server messaging or basic flood-mesh systems that quickly saturate available radio bandwidth, Antara runs an **adaptive, multi-modal routing engine** designed to select the optimal relay path while conserving battery power.

---

## 1. Neighbor Table & Dynamic Metrics
Every node maintains a local cache called the `NeighborTable`. This table lists all nodes within immediate radio range (1-hop) and computes an ongoing **Link Quality Index (LQI)** using several metrics:

$$\text{LQI} = \alpha \cdot \text{RSSI}_{\text{scaled}} + \beta \cdot \text{Battery}_{\text{pct}} + \gamma \cdot \text{TrustScore} - \delta \cdot \text{QueueDepth}$$

Where:
*   `RSSI_scaled` (0.0 to 1.0): Represents signal attenuation.
*   `Battery_pct` (0.0 to 1.0): Protects dying nodes by avoiding using them as relays.
*   `TrustScore` (0.0 to 1.0): Measured dynamically based on successful delivery vs. packet drops.
*   `QueueDepth` (0.0 to 1.0): Represents congestion on the target node.
*   \(\alpha, \beta, \gamma, \delta\) are weight coefficients tuned via platform benchmarks.

---

## 2. Dynamic Hop Table
For multi-hop destinations, nodes exchange periodic low-footprint distance vectors. The `HopTable` maps target node cryptographic address hashes to:
1.  **Next Hop Hash:** The best physical peer to route through.
2.  **Total Hops:** The shortest known topological distance.
3.  **Expiry Time:** The epoch timestamp indicating when this route vector expires.

---

## 3. Hybrid Routing Heuristics

Antara does not use a single routing algorithm. Instead, it dynamically switches its routing heuristic depending on the **network density**, **destination state**, and **energy budget**:

```
                  +-----------------------------------+
                  |      Packet Enters Routing        |
                  +-----------------------------------+
                                    |
                  Is Destination a Direct Neighbor?
                       /                  \
                    (Yes)                 (No)
                     /                      \
        +-----------------------+     Is Route in HopTable?
        |  Send Direct (1-Hop)  |         /           \
        +-----------------------+      (Yes)          (No)
                                        /               \
                       +----------------------+    Is Network Dense?
                       | Unicast Route (AODV) |       /          \
                       +----------------------+    (Yes)         (No)
                                                    /              \
                                   +-------------------+    +-------------------+
                                   | Gossip Protocol   |    | Epidemic / DTN    |
                                   +-------------------+    +-------------------+
```

### A. Epidemic Routing & Spray-and-Wait (Sparse Network Mode)
In disconnected environments (e.g., deep wilderness, low-density regions), devices are isolated from each other. Antara falls back to **Delay-Tolerant Networking (DTN)**:
*   **Epidemic Flooding:** Senders distribute copies of the packet to every newly encountered node.
*   **Spray & Wait:** To prevent message explosion, the packet contains a `TicketCount` metadata field. When Node A encounters Node B, it forwards the packet but splits the ticket budget (e.g., Node A retains \(N/2\) tickets, Node B gets \(N/2\)). Once a node has only 1 ticket left, it is forbidden from forwarding the packet to anyone except the final recipient.

### B. Gossip Protocol (Dense Network Mode)
In dense environments (e.g., a university campus with thousands of active nodes), epidemic routing would cause immediate packet storms, saturating channels:
*   **Probabilistic Forwarding:** When a node receives a broadcast packet, it forwards it to adjacent peers with a dynamic probability \(P_{\text{forward}}\) (typically 0.65). 
*   **Bloom Filters:** Nodes periodically exchange a compressed Bloom filter representing their message cache, ensuring peers only transmit missing payloads.

### C. AODV & BATMAN Hybrid (Structured Mesh Mode)
When nodes are stationary or semi-stationary (e.g., students sitting in a lecture hall):
*   **Route Discovery:** The node broadcasts a `RouteRequest (RREQ)` packet.
*   **Route Reply:** The target returns a unicast `RouteReply (RREP)` backwards along the path. Senders then route packets down this specific sequence of hops.
*   **Proactive Healing (BATMAN-inspired):** Nodes broadcast periodic originator messages (`OGM`) to declare their existence, allowing nodes to build optimal path trees.

---

## 4. Delay-Tolerant Storage & Queue Eviction
If a packet cannot be delivered immediately, it enters the **DTN Buffer** (isolated folder in local database storage). 
*   **Priority Allocation:** Critical emergency alerts receive a permanent buffer allocation. Chat messages are allocated memory dynamically.
*   **Eviction Policy (FIFO-TTL Hybrid):** Packets are cleared from storage when:
    1.  The packet's Time-To-Live (TTL) counter reaches zero (default: 48 hours).
    2.  An cryptographic `ACK` frame for the packet is received from the target.
    3.  The local storage reaches its capacity limit (90% threshold), triggering eviction of the oldest, low-priority messages.
