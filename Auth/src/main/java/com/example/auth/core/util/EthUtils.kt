package com.example.auth.core.util

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.*
import org.web3j.utils.Numeric
import java.security.Security
import java.security.SecureRandom

object EthUtils {

    init {
        // Initialize BouncyCastle provider
        setupBouncyCastle()
    }

    private fun setupBouncyCastle() {
        try {
            // Remove existing BC provider if present
            Security.removeProvider("BC")
            // Add BouncyCastle provider
            Security.addProvider(BouncyCastleProvider())

            // Verify the provider is available
            val provider = Security.getProvider("BC")
            if (provider == null) {
                throw RuntimeException("BouncyCastle provider not available")
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to setup BouncyCastle provider", e)
        }
    }

    data class WalletData(
        val address: String,
        val privateKey: String,
        val publicKey: String
    )

    fun generateNewWallet(): WalletData {
        try {
            // Ensure BouncyCastle is set up
            setupBouncyCastle()

            // Generate a new EC key pair using Web3j
            val ecKeyPair = Keys.createEcKeyPair()

            // Get the credentials
            val credentials = Credentials.create(ecKeyPair)

            // Extract wallet data
            val address = credentials.address
            val privateKey = Numeric.toHexStringWithPrefix(ecKeyPair.privateKey)
            val publicKey = Numeric.toHexStringWithPrefix(ecKeyPair.publicKey)

            return WalletData(
                address = address,
                privateKey = privateKey,
                publicKey = publicKey
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate wallet: ${e.message}", e)
        }
    }

    fun signMessage(message: String, privateKeyHex: String): String {
        try {
            // Ensure BouncyCastle is set up
            setupBouncyCastle()

            // Remove '0x' prefix if present
            val cleanPrivateKey = privateKeyHex.removePrefix("0x")

            // Create credentials from private key
            val credentials = Credentials.create(cleanPrivateKey)

            // Sign the message
            val signatureData = Sign.signPrefixedMessage(message.toByteArray(), credentials.ecKeyPair)

            // Convert signature to hex string
            val r = Numeric.toHexString(signatureData.r)
            val s = Numeric.toHexString(signatureData.s)
            val v = signatureData.v.toString()

            // Concatenate r, s, v
            return "${r}${s.removePrefix("0x")}${v.padStart(2, '0')}"

        } catch (e: Exception) {
            throw RuntimeException("Failed to sign message: ${e.message}", e)
        }
    }

    fun verifySignature(message: String, signature: String, expectedAddress: String): Boolean {
        try {
            // Ensure BouncyCastle is set up
            setupBouncyCastle()

            // Parse signature
            val r = signature.substring(0, 64)
            val s = signature.substring(64, 128)
            val v = signature.substring(128, 130).toInt(16).toByte()

            val signatureData = Sign.SignatureData(
                v,
                Numeric.hexStringToByteArray("0x$r"),
                Numeric.hexStringToByteArray("0x$s")
            )

            // Recover public key from signature
            val publicKey = Sign.signedPrefixedMessageToKey(message.toByteArray(), signatureData)

            // Get address from public key
            val recoveredAddress = "0x" + Keys.getAddress(publicKey)

            return recoveredAddress.equals(expectedAddress, ignoreCase = true)

        } catch (e: Exception) {
            return false
        }
    }
}