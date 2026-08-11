# Literature Review & P2P Protocols Research

Antara builds on decades of computer science research in mobile ad-hoc networks (MANETs), delay-tolerant networking (DTN), and decentralized cryptography. This document compiles our academic foundations and provides a comparative analysis of existing projects.

---

## 1. Comparative Analysis Matrix

Antara occupies a unique space, combining high-throughput local ad-hoc connections with modern mobile-optimized background transport.

| System | Primary Transport | Routing Architecture | Multi-Hop Support | Target Platform | Main Limitation |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Meshtastic** | LoRa (868/915 MHz) | Flood Mesh | Yes (up to 7 hops) | Custom Hardware | Extremely low bandwidth (~kbps); requires external hardware radio module. |
| **Briar** | BLE, Wi-Fi, Internet | Direct Sync (1-hop) | No multi-hop routing | Android (limited iOS) | No multi-hop message routing; requires direct physical proximity. |
| **FireChat** | BLE, Multipeer Conn | Proprietary mesh | Yes | iOS, Android | Proprietary protocol, abandoned, lacked end-to-end encryption. |
| **Serval Mesh** | Wi-Fi (Ad-Hoc / AP) | BATMAN-advanced | Yes | Rooted Android | Required root permissions to configure Wi-Fi ad-hoc profiles. |
| **Antara** | BLE, Wi-Fi Direct, NAN | Hybrid (Gossip/DTN) | **Yes (Dynamic)** | **Android & iOS** | Platform-level background execution constraints (mitigated). |

---

## 2. Academic Bibliography & Foundations

Our protocol stack draws key concepts from several foundational papers in networking and distributed systems:

### A. Delay-Tolerant Networking (DTN)
*   **Concepts Used:** Store-and-Forward architectures, Epidemic routing, Spray & Wait heuristic.
*   **Key Literature:** 
    *   Vahdat, A., & Becker, D. (2000). *Epidemic Routing for Partially Connected Ad Hoc Networks*. Duke University.
    *   Spyropoulos, T., Psounis, K., & Raghavendra, C. S. (2005). *Spray and wait: an efficient alternative to epidemic routing in delay tolerant networks*. ACM SIGCOMM.

### B. Gossip and Epidemic Dissemination
*   **Concepts Used:** Probabilistic data replication, Bloom-filter reconciliation, and bandwidth minimization in dense environments.
*   **Key Literature:**
    *   Demers, A., et al. (1987). *Epidemic algorithms for replicated database maintenance*. ACM Symposium on Principles of Distributed Computing.
    *   Karp, B., & Kung, H. T. (2000). *GPSR: Greedy perimeter stateless routing for wireless networks*. ACM MobiCom.

### C. Conflict-Free Replicated Data Types (CRDTs)
*   **Concepts Used:** State-based and operation-based eventual consistency without primary nodes.
*   **Key Literature:**
    *   Shapiro, M., Preguiça, N., Baquero, C., & Zawirski, M. (2011). *Conflict-Free Replicated Data Types*. Symposium on Self-Stabilizing Systems.

### D. End-to-End Cryptography and Noise
*   **Concepts Used:** Noise XK Pattern handshake, Double Ratchet key iteration.
*   **Key Literature:**
    *   Perrin, T. (2018). *The Noise Protocol Specification*.
    *   Marlinspike, M., & Perrin, T. (2016). *The Double Ratchet Algorithm*. Signal.org.
