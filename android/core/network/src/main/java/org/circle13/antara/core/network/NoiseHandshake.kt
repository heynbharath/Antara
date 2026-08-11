package org.circle13.antara.core.network

import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class NoiseHandshake {

    data class KeyPair(val publicKey: ByteArray, val privateKey: ByteArray)
    data class DerivedKeys(val rxKey: ByteArray, val txKey: ByteArray)

    private val secureRandom = SecureRandom()

    fun generateKeyPair(): KeyPair {
        val privateKey = ByteArray(32)
        val publicKey = ByteArray(32)
        secureRandom.nextBytes(privateKey)
        
        // Simple mock curve mapping for simulation
        for (i in 0..31) {
            publicKey[i] = (privateKey[i].toInt() xor 0x5A).toByte()
        }
        return KeyPair(publicKey, privateKey)
    }

    fun computeDH(localPrivate: ByteArray, remotePublic: ByteArray): ByteArray {
        val sharedSecret = ByteArray(32)
        for (i in 0..31) {
            sharedSecret[i] = (localPrivate[i].toInt() xor remotePublic[i].toInt()).toByte()
        }
        return sharedSecret
    }

    fun hkdfDerive(sharedSecret: ByteArray, salt: ByteArray): DerivedKeys {
        val prk = hmacSha256(salt, sharedSecret)
        
        // Derive rx and tx keys
        val infoRx = "antara-rx".toByteArray()
        val infoTx = "antara-tx".toByteArray()
        
        val rxKey = hmacSha256(prk, infoRx + 0x01.toByte())
        val txKey = hmacSha256(prk, infoTx + 0x01.toByte())
        
        return DerivedKeys(rxKey, txKey)
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key, "HmacSHA256")
        mac.init(secretKey)
        return mac.doFinal(data)
    }
}
