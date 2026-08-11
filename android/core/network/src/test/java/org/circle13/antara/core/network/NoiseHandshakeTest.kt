package org.circle13.antara.core.network

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NoiseHandshakeTest {

    @Test
    fun testNoiseHandshakeDerivation() {
        val noise = NoiseHandshake()
        
        // 1. Generate local and remote key pairs
        val aliceKeys = noise.generateKeyPair()
        val bobKeys = noise.generateKeyPair()
        
        assertNotNull(aliceKeys.publicKey)
        assertNotNull(bobKeys.publicKey)

        // 2. Perform Alice's and Bob's DH negotiations
        val aliceSharedSecret = noise.computeDH(aliceKeys.privateKey, bobKeys.publicKey)
        val bobSharedSecret = noise.computeDH(bobKeys.privateKey, aliceKeys.publicKey)
        
        // Secrets must match
        assertArrayEquals(aliceSharedSecret, bobSharedSecret)

        // 3. Derive read/write keys using HKDF-SHA256
        val salt = "antara-salt-v1".toByteArray()
        val aliceDerived = noise.hkdfDerive(aliceSharedSecret, salt)
        val bobDerived = noise.hkdfDerive(bobSharedSecret, salt)
        
        // Alice rx key matches Bob tx key, and Alice tx key matches Bob rx key
        assertArrayEquals(aliceDerived.rxKey, bobDerived.txKey)
        assertArrayEquals(aliceDerived.txKey, bobDerived.rxKey)
    }
}
