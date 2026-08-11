import XCTest
import CryptoKit
@testable import AntaraCrypto

final class NoiseHandshakeTests: XCTestCase {
    
    func testNoiseHandshakeDerivation() throws {
        let noise = NoiseHandshake()
        
        // 1. Generate local and remote key pairs
        let aliceKeys = noise.generateKeyPair()
        let bobKeys = noise.generateKeyPair()
        
        XCTAssertNotNil(aliceKeys.publicKeyData)
        XCTAssertNotNil(bobKeys.publicKeyData)
        
        // 2. Perform Alice's and Bob's DH negotiations
        let aliceSharedSecret = try noise.computeSharedSecret(
            localPrivate: aliceKeys.privateKey,
            remotePublicRepresentation: bobKeys.publicKeyData
        )
        
        let bobSharedSecret = try noise.computeSharedSecret(
            localPrivate: bobKeys.privateKey,
            remotePublicRepresentation: aliceKeys.publicKeyData
        )
        
        // Compute raw shared representations to assert equality
        let aliceSecretData = aliceSharedSecret.withUnsafeBytes { Data($0) }
        let bobSecretData = bobSharedSecret.withUnsafeBytes { Data($0) }
        XCTAssertEqual(aliceSecretData, bobSecretData)
        
        // 3. Derive read/write keys using HKDF-SHA256
        let salt = "antara-salt-v1".data(using: .utf8)!
        let aliceDerived = noise.hkdfDerive(sharedSecret: aliceSharedSecret, salt: salt)
        let bobDerived = noise.hkdfDerive(sharedSecret: bobSharedSecret, salt: salt)
        
        // Alice rx key matches Bob tx key, and Alice tx key matches Bob rx key
        let aliceRxData = aliceDerived.rxKey.withUnsafeBytes { Data($0) }
        let aliceTxData = aliceDerived.txKey.withUnsafeBytes { Data($0) }
        let bobRxData = bobDerived.rxKey.withUnsafeBytes { Data($0) }
        let bobTxData = bobDerived.txKey.withUnsafeBytes { Data($0) }
        
        XCTAssertEqual(aliceRxData, bobTxData)
        XCTAssertEqual(aliceTxData, bobRxData)
    }
}
