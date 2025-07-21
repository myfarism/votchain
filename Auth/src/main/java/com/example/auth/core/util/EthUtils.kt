package com.example.auth.core.util

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Hash
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.nio.charset.Charset
import java.security.Security
import kotlin.text.Charsets

object EthUtils {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    fun generateNewWallet(): Wallet {
        val ecKeyPair = Keys.createEcKeyPair()
        return Wallet(
            privateKey = Numeric.toHexStringNoPrefix(ecKeyPair.privateKey),
            publicKey = Numeric.toHexStringNoPrefix(ecKeyPair.publicKey),
            address = Keys.toChecksumAddress(Keys.getAddress(ecKeyPair))
        )
    }

    fun signMessage(message: String, privateKey: String): String {
        val privateKeyBytes = Numeric.hexStringToByteArray(privateKey)
        val ecKeyPair = ECKeyPair.create(privateKeyBytes)

        // Convert message to bytes and hash it
        val messageHash = Hash.sha3(message.toByteArray(Charsets.UTF_8))

        // Sign the message hash
        val signatureData = Sign.signMessage(messageHash, ecKeyPair, false)

        // Format the signature components
        val r = Numeric.toHexStringNoPrefix(signatureData.r)
        val s = Numeric.toHexStringNoPrefix(signatureData.s)
        val v = signatureData.v.toString()

        return "0x$r${s.padStart(64, '0')}$v"
    }
}

data class Wallet(
    val privateKey: String,
    val publicKey: String,
    val address: String
)