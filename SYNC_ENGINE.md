# State Synchronization & CRDT Engine

In an offline-first mesh network, there is no centralized database server to act as the source of truth. Devices connect intermittently, edit thread states concurrently, and go offline for days. 

To resolve concurrent updates and guarantee eventual consistency, Antara uses a peer-to-peer state synchronization engine powered by **Conflict-Free Replicated Data Types (CRDTs)** and **Merkle Directed Acyclic Graphs (DAGs)**.

---

## 1. The Database as a Merkle DAG

Every chat thread in Antara is represented as a **Merkle Directed Acyclic Graph (DAG)**.
*   **Nodes in the DAG:** Each node represents an immutable action: a message sent, an edit operation, or a message deletion.
*   **Edges:** Each node contains cryptographic parent pointers (hashes) of the immediate predecessor messages it observed when it was created.
*   **Causal Ordering:** If Message B has a parent pointer to Message A, Message B causally succeeds Message A. If two messages do not have a causal link, they are concurrent.

```
Message A (Root)
    |
Message B
   /  \
Msg C  Msg D  (Concurrent edits; both have Message B as parent)
   \  /
Message E (Merge Node; resolves concurrency)
```

---

## 2. CRDT Logic: LWW-Element-Set
For thread membership and message state updates (e.g., editing text or deleting messages), Antara implements a **Last-Write-Wins Element-Set (LWW-Element-Set)** CRDT.

*   **Structure:** Each set maintains two internal logs: an `Add Set` and a `Remove Set`.
*   **Payload Format:** Each record contains:
    
    $$\text{Record} = (\text{ElementID}, \text{Value}, \text{Timestamp}, \text{VectorClock})$$
    
*   **State Reconciliation Rule:** 
    *   To check if a message exists, the engine checks if the message ID is in the `Add Set` and not in the `Remove Set`.
    *   If the message ID is in both sets, the operation with the higher timestamp (or vector clock value) wins.
    *   This ensures that even if two devices concurrent modify or delete the same message, they will arrive at the exact same database state once they synchronize, without requiring a central coordinator.

---

## 3. Dynamic Sync Loop & Diff Negotiation

When two nodes connect, they negotiate database differences using Merkle DAG synchronization to minimize data transfer:

```
Alice                                               Bob
  |                                                  |
  | --- Handshake Done ----------------------------> |
  |                                                  |
  | --- SyncRequest (DAG Heads & Bloom Filter) ----> | (Send current heads)
  |                                                  |
  |                                         [Evaluates Heads against own DAG]
  |                                         [Identifies missing sub-graphs]
  |                                                  |
  | <--- SyncResponse (Missing Messages payload) ----| (Sends delta updates)
  |                                                  |
  | [Integrates delta nodes]                         |
  | [Resolves CRDT conflicts]                        |
  |                                                  |
  | <--- SyncRequest (Updated DAG Heads) ------------|
```

### Steps:
1.  **SyncRequest Transmission:** Alice sends a `SyncRequest` containing the hashes of her current DAG heads (tips of the thread trees) and a Bloom filter of her message history.
2.  **Diff Identification:** Bob compares Alice's DAG heads to his own.
    *   *Scenario A (Identical):* No action needed. Sockets are kept in keep-alive state.
    *   *Scenario B (Alice is ahead):* Bob requests the missing delta logs.
    *   *Scenario C (Bob is ahead):* Bob transmits the missing nodes in a `SyncResponse`.
    *   *Scenario D (Diverged Heads):* Bob detects concurrent branches and compiles a merge plan, sending the differences to Alice to force convergence.
3.  **Local Integration:** Once Alice receives the delta payload, she inserts the nodes into her local SQLCipher database. The CRDT resolver executes to compute the updated UI thread state.
