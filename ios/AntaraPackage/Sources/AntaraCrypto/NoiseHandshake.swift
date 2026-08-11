import Foundation
import CryptoKit

public final class NoiseHandshake {
    
    public struct KeyAgreementPair {
        public let privateKey: Curve25519.KeyAgreement.PrivateKey
        public let publicKeyData: Data
        
        public init() {
            let key = Curve25519.KeyAgreement.PrivateKey()
            self.privateKey = key
            self.publicKeyData = key.publicKey.rawRepresentation
        }
    }
    
    public struct DerivedSymmetricKeys {
        public let rxKey: SymmetricKey
        public let txKey: SymmetricKey
        
        public init(rxKey: SymmetricKey, txKey: SymmetricKey) {
            self.rxKey = rxKey
            self.txKey = txKey
        }
    }
    
    public init() {}
    
    public func generateKeyPair() -> KeyAgreementPair {
        return KeyAgreementPair()
    }
    
    public func computeSharedSecret(localPrivate: Curve25519.KeyAgreement.PrivateKey, remotePublicRepresentation: Data) throws -> SharedSecret {
        let remotePublic = try Curve25519.KeyAgreement.PublicKey(rawRepresentation: remotePublicRepresentation)
        return try localPrivate.sharedSecretFromKeyAgreement(with: remotePublic)
    }
    
    public func hkdfDerive(sharedSecret: SharedSecret, salt: Data) -> DerivedSymmetricKeys {
        // Derive rx and tx keys
        let infoRx = "antara-rx".data(using: .utf8)!
        let infoTx = "antara-tx".data(using: .utf8)!
        
        let rxKey = sharedSecret.hkdfDerivedSymmetricKey(
            using: SHA256.self,
            salt: salt,
            sharedInfo: infoRx,
            outputByteCount: 32
        )
        
        let txKey = sharedSecret.hkdfDerivedSymmetricKey(
            using: SHA256.self,
            salt: salt,
            sharedInfo: infoTx,
            outputByteCount: 32
        )
        
        return DerivedSymmetricKeys(rxKey: rxKey, txKey: txKey)
    }
}
