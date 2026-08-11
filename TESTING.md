# Verification & Mesh Testing Specification

Testing a decentralized routing and transport layer on physical devices is difficult due to variable radio environments and spatial configurations. To ensure protocol stability, Antara uses a three-tiered testing strategy: **Local Unit Testing**, **Virtualized Mesh Emulation**, and **Automated Hardware Battery Benchmarking**.

---

## 1. Virtualized Mesh Simulation (Shadow Network)

To validate routing algorithms (Gossip, Epidemic, Spray & Wait) at scale, we use a virtualized network simulator configured to run in Docker containers:

*   **Node Emulation:** Every simulated node runs the compiled Kotlin Routing and Sync engine inside a lightweight container.
*   **Virtual Radio Media:** We use a centralized network channel controller that drops packets or delays bytes between container sockets to simulate spatial distancing, radio occlusion, and packet loss.
*   **Topology Graph Maps:** We load real campus building maps and simulated coordinate paths to verify how nodes dynamically discover peers, forward routing tables, and establish multi-hop paths.

```
+-----------------------------------------------------------+
|                 Mesh Topology Simulator                   |
|                                                           |
|  +--------------------+             +------------------+  |
|  | Node Container A   | <---RSSI---> | Node Container B |  |
|  | (Mock BLE & Wi-Fi) |  Symmetric   | (Relay Node)     |  |
|  +--------------------+  Link Delay  +------------------+  |
|            |                                |             |
|            +---------------+----------------+             |
|                            |                              |
|                            v                              |
|                  +--------------------+                   |
|                  | Node Container C   |                   |
|                  +--------------------+                   |
+-----------------------------------------------------------+
```

---

## 2. P2P Chaos Engineering Scenarios

To prove our **Self-Healing** and **Delay-Tolerant** design principles, the integration suite triggers automated chaos runs:

1.  **Sudden Link Eviction:** While a database synchronization session is actively transferring payload data, we force kill the target node container. The sender must cleanly place the remaining frame packets back into the DTN queue and log the partial transfer without corrupting the Merkle DAG.
2.  **Sybil Flooding Simulation:** A simulated malicious node sends 10,000 route requests per second using spoofed IDs. The system must verify that the target nodes scale their Hashcash difficulty parameters and throttle connection pools without dropping valid peer frames.
3.  **Dynamic Route Partition:** A chain of nodes `A -> B -> C -> D` is established. During active routing from `A` to `D`, Node `B` is disabled. The test verifies that Node `A` successfully recalculates its route or uses Node `C` as an alternate relay within 5 seconds.

---

## 3. Battery Drain Gating & Benchmarks

Because continuous scanning is power-intensive, we enforce strict battery consumption limits:

*   **Test Rig:** Automated Android and iOS test devices are connected to digital power meters (e.g., Monsoon Power Monitor).
*   **Run Iterations:** The devices execute background discovery and routing cycles for 12 hours.
*   **Gating Thresholds:**
    
    $$\text{Target Background Drain} \le 1.8\% \text{ battery capacity per hour.}$$
    
    If an architectural change to discovery scanning frequencies causes background consumption to exceed this threshold, the PR is automatically blocked from merge.

---

## 4. Platform Testing Command Matrices

### Running Android Unit Tests
```bash
# Run JVM unit tests for routing and database modules
./gradlew testDebugUnitTest --continue

# Run local instrumented tests on connected devices
./gradlew connectedAndroidTest
```

### Running iOS Test Suite via CLI
```bash
# Execute local unit and logic tests inside Simulator
xcodebuild test \
  -scheme Antara \
  -destination 'platform=iOS Simulator,name=iPhone 15,OS=17.2'
```
