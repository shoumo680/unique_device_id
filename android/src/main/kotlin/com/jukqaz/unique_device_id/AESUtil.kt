package com.jukqaz.unique_device_id

import android.util.Base64
import java.nio.charset.Charset
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESUtil(val key: String) {
    private val iv: String = key.substring(
        0,
        16
    )
    private val keySpec: Key

    init {
        val keyBytes = ByteArray(16)
        val b = key.toByteArray(charset("UTF-8"))
        var len = b.size
        if (len > keyBytes.size) {
            len = keyBytes.size
        }
        System.arraycopy(b, 0, keyBytes, 0, len)
        val keySpec = SecretKeySpec(keyBytes, "AES")
        this.keySpec = keySpec
    }

    fun encode(str: String): String {
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(
            Cipher.ENCRYPT_MODE,
            keySpec,
            IvParameterSpec(iv.toByteArray())
        )
        val encrypted = c.doFinal(str.toByteArray(charset("UTF-8")))
        return Base64.encodeToString(encrypted, 0)
    }

    fun decode(str: String): String {
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(
            Cipher.DECRYPT_MODE,
            keySpec,
            IvParameterSpec(iv.toByteArray(charset("UTF-8")))
        )
        val byteStr = Base64.decode(str.toByteArray(), 0)
        return String(c.doFinal(byteStr), Charset.defaultCharset())
    }

}