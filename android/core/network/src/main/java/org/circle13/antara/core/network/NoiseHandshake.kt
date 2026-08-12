package org.circle13.antara.core.network

import java.security.SecureRandom
import java.security.KeyPairGenerator
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class NoiseHandshake {

    data class KeyPair(val publicKey: ByteArray, val privateKey: ByteArray)
    data class DerivedKeys(val rxKey: ByteArray, val txKey: ByteArray)

    private val secureRandom = SecureRandom()

    fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("EC")
        keyGen.initialize(256, secureRandom)
        val pair = keyGen.generateKeyPair()
        return KeyPair(pair.public.encoded, pair.private.encoded)
    }

    fun computeDH(localPrivateBytes: ByteArray, remotePublicBytes: ByteArray): ByteArray {
        val keyFactory = KeyFactory.getInstance("EC")
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(localPrivateBytes))
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(remotePublicBytes))
        
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(publicKey, true)
        
        return keyAgreement.generateSecret()
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
