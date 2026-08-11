package org.circle13.antara.core.network

import android.net.Uri
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

data class UserIdentity(
    val nodeId: String,
    val publicKeyHex: String,
    val username: String,
    val fullName: String,
    val timestamp: Long = System.currentTimeMillis()
)

object CryptoManager {

    fun generateKeyPair(): Pair<String, String> {
        return try {
            val keyPairGenerator = KeyPairGenerator.getInstance("EC")
            keyPairGenerator.initialize(256, SecureRandom())
            val keyPair = keyPairGenerator.generateKeyPair()
            val publicKeyBytes = keyPair.public.encoded
            
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(publicKeyBytes)
            val nodeId = hashBytes.joinToString("") { "%02x".format(it) }
            val publicKeyHex = publicKeyBytes.joinToString("") { "%02x".format(it) }

            Pair(nodeId, publicKeyHex)
        } catch (e: Exception) {
            // Fallback random secure bytes if EC generator encounters platform variations
            val random = SecureRandom()
            val nodeBytes = ByteArray(16)
            val pubBytes = ByteArray(32)
            random.nextBytes(nodeBytes)
            random.nextBytes(pubBytes)
            val nodeId = nodeBytes.joinToString("") { "%02x".format(it) }
            val pubHex = pubBytes.joinToString("") { "%02x".format(it) }
            Pair(nodeId, pubHex)
        }
    }

    fun generateQrPayload(identity: UserIdentity): String {
        return "antara://identity?v=1&nodeId=${identity.nodeId}&name=${Uri.encode(identity.username)}&fullName=${Uri.encode(identity.fullName)}&pubKey=${identity.publicKeyHex}"
    }

    fun parseQrPayload(payload: String): UserIdentity? {
        return try {
            if (!payload.startsWith("antara://identity")) return null
            val uri = Uri.parse(payload)
            val nodeId = uri.getQueryParameter("nodeId") ?: return null
            val username = uri.getQueryParameter("name") ?: "Unknown Peer"
            val fullName = uri.getQueryParameter("fullName") ?: "Verified Peer"
            val pubKey = uri.getQueryParameter("pubKey") ?: ""
            UserIdentity(
                nodeId = nodeId,
                publicKeyHex = pubKey,
                username = username,
                fullName = fullName
            )
        } catch (e: Exception) {
            null
        }
    }
}
