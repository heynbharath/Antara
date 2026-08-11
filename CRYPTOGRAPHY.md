# Cryptographic Protocol & Identity Specification

In an offline, serverless environment, we must assume that the transport medium is entirely untrusted. Packets will traverse arbitrary stranger devices during multi-hop routing. Thus, security cannot be bolted on at the application layer—it must be baked into the packet design.

Antara ensures **End-to-End Encryption (E2EE)**, **Forward Secrecy**, and **Cryptographic Identity Verification** using modern cryptographic standards.

---

## 1. Cryptographic Primitives

Antara restricts its cryptography to a set of highly optimized, side-channel-resistant primitives:

*   **Key Agreement (Diffie-Hellman):** X25519 (Curve25519) for ECDH key negotiations.
*   **Signatures:** Ed25519 for identity verification and packet signing.
*   **Symmetric Encryption:** AES-256-GCM or ChaCha20-Poly1305 for authenticated encryption (AEAD) of payloads.
*   **Hash Function:** SHA-256 or BLAKE2b for generating content hashes and identity digests.
*   **Key Derivation:** HKDF-SHA256 (Hash-based Key Derivation Function) for extracting and expanding cryptographic keys.

---

## 2. Peer Identity Model

Because there is no central authority to assign user IDs, Antara identity is entirely self-sovereign:

*   **Identity Key Pair:** A permanent Ed25519 keypair (\(IK_{\text{pub}}, IK_{\text{priv}}\)) generated locally upon application setup.
*   **Node Address:** The SHA-256 hash of the public Identity Key:
    
    $$\text{NodeID} = \text{SHA-256}(IK_{\text{pub}})$$
    
    This 256-bit hash is used as the node's identifier in neighbor tables, hop tables, and routing envelopes.

---

## 3. End-to-End Session Handshake: Noise Protocol Framework

To establish a secure connection between two nodes, Antara implements the **Noise XK Handshake Pattern** from the Noise Protocol Framework. This pattern assumes the initiator (\(A\)) knows the responder's (\(B\)) static public key beforehand (which is exchanged during initial proximity discovery).

```
Alice (Initiator)                                  Bob (Responder)
   |                                                      |
   | --- e, es, s, ss ----------------------------------> | (Handshake Message 1)
   |                                                      |
   | <---------------------------------- e, ee ----------- | (Handshake Message 2)
   |                                                      |
[Computes symmetric session keys]                 [Computes symmetric session keys]
```

### Handshake Sequence:
1.  **Ephemeral Key Generation:** Both Alice and Bob generate temporary, one-time key pairs (\(E_A, E_B\)) for the session.
2.  **Diffie-Hellman Exchanges:**
    *   `es`: DH exchange between Alice's ephemeral key and Bob's static key.
    *   `s`: Alice transmits her static identity public key encrypted with the current session state.
    *   `ss`: DH exchange between Alice's static key and Bob's static key.
    *   `ee`: DH exchange between Alice's ephemeral key and Bob's ephemeral key.
3.  **Session Transition:** The handshake secrets are run through HKDF-SHA256 to produce a pair of unidirectional **Symmetric Cipher States** (one for transmission, one for reception). The ephemeral keys are immediately destroyed.

---

## 4. Message Lifecycle: The Double Ratchet Algorithm

Once a Noise session is established, Antara implements the **Double Ratchet** (formerly Axolotl) algorithm to secure ongoing message streams. This guarantees:
*   **Forward Secrecy:** A compromised session key does not expose past messages.
*   **Break-in Recovery:** An adversary who intercepts a temporary key cannot decrypt future messages once a new DH ratchet completes.

### The Double Ratchet Chain:
*   **KDF Chain Ratchet:** For every message sent, the sending chain KDF generates a new message key. This is a fast, symmetric key derivation.
*   **DH Ratchet:** When a node receives a response containing a new ephemeral key, it executes a Diffie-Hellman computation to advance the root key chain, creating a new sending/receiving chain.

---

## 5. Zero-Knowledge Private Discovery

To prevent third-party tracking, Antara does not broadcast static node IDs during discovery. Instead, it uses **Cryptographic Discovery Tokens**:

1.  Each node generates a rotating daily key:
    
    $$DK = \text{HKDF}(IK_{\text{priv}}, \text{CurrentDayStamp})$$

2.  When scanning, Node A broadcasts an ephemeral beacon token computed as:
    
    $$\text{BeaconToken} = \text{HMAC-SHA256}(DK, \text{EpochInterval})$$
    
    Where `EpochInterval` rotates every 15 minutes.

3.  Only contacts who have previously exchanged static identity keys with Alice can reconstruct the expected tokens and recognize her presence. To an outside observer or intermediate router, the beacon token is indistinguishable from random noise.
