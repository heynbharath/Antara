# Security Model & Threat Assessment

Operating a decentralized, serverless communication stack requires a highly defensive security posture. Unlike centralized applications that delegate authentication, rate-limiting, and validation to cloud servers, Antara must assume that:
1.  **Intermediate routing nodes are malicious.**
2.  **Peers can spoof identities or packet timestamps.**
3.  **An adversary can deploy a large number of virtual nodes to flood the network.**

---

## 1. Threat Modeling & Mitigations

| Threat | Description | Protocol Mitigation |
| :--- | :--- | :--- |
| **Man-in-the-Middle (MITM)** | An intermediary relay node intercepts, reads, or alters transit packets. | E2EE via Noise XK Handshake and Double Ratchet. Packet payloads are unreadable by relays. Ed25519 signatures prevent payload tampering. |
| **Replay Attacks** | An attacker captures valid encrypted packets and broadcasts them repeatedly to flood the recipient or consume network resources. | Every packet contains a monotonically increasing sequence counter and a high-resolution timestamp. Packets with expired timestamps (>5 min deviation) or duplicate sequence IDs are discarded at the transport layer. |
| **Sybil Attacks** | A single physical adversary creates thousands of virtual identity hashes to swamp routing tables or intercept gossip paths. | Hashcash-based proof-of-work (PoW) is required to register new neighbor vectors. Additionally, we enforce a localized Trust Score metric. |
| **Traffic Analysis** | An observer monitors packet sizes and directional flows to map the social graph of active communicators. | Packet padding (forcing fixed-size packet envelopes) and rotating ephemeral tokens are used during discovery. |
| **Relay Resource Starvation** | A malicious peer floods the mesh with large junk files, exhausting local storage (DTN buffers) on intermediate relays. | Local storage isolation and strict packet lifecycle quotas based on node trust levels. |

---

## 2. Sybil Mitigation: Hashcash Proof-of-Work

To prevent malicious actors from generating millions of arbitrary node IDs, registering a new neighbor identity or requesting a route discovery broadcast requires a lightweight **Proof-of-Work (PoW)** challenge:

*   When Node A seeks to register its route table vector on Node B, Node B issues a random 256-bit challenge salt.
*   Node A must compute a nonce such that:
    
    $$\text{SHA-256}(\text{NodeID}_{\text{A}} \mathbin{\Vert} \text{ChallengeSalt} \mathbin{\Vert} \text{Nonce}) \text{ starts with } D \text{ leading zeros.}$$
    
*   The difficulty \(D\) is adjusted dynamically based on local node density and connection requests (default: 16 leading zero bits, which takes less than 100ms on a modern smartphone, preventing bulk automated spoofing).

---

## 3. Dynamic Trust Scores & Packet Throttling

Intermediate relay performance is monitored by neighboring nodes. Senders compute a localized **Trust Score** for each connected peer:

1.  **Metric Tracking:** Every time Node A forwards a packet to Node B, it expects a cryptographic acknowledgment (`ACK`) forwarded back.
2.  **Score Formula:** 
    
    $$\text{TrustScore} = \frac{\text{AckedPackets}}{\text{ForwardedPackets}}$$
    
3.  **Behavior Penalty:** Senders select relays with higher trust scores. If a peer's Trust Score drops below `0.4` (indicating they are dropping packets or acting as a black-hole routing node), they are blocked from receiving high-priority traffic and their routing tables are ignored.

---

## 4. Message Authenticity & Key Verification

To ensure that the Alice you are chatting with is the actual physical person:
*   **Out-of-Band Verification:** Identity keys can be verified in-person by scanning a QR code containing the SHA-256 fingerprint of the user's permanent static key.
*   **Trust on First Use (TOFU):** If no out-of-band verification occurs, the app saves the static public key upon the first contact. If Bob's device identity key changes later, the app blocks the thread and alerts the user of a potential identity spoofing attempt.
