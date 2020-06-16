package com.sky.csc

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

class SecretsHelper {
    static final password = Configuration.getEnvironmentVariable("CSC_E2E_TESTS_PASSWORD")

    static String decrypt(String encryptedString) {
        def cipher = initCipher(Cipher.DECRYPT_MODE, generateSecretKeyFromPassword())
        def decryptedBytes = cipher.doFinal(Base64.decoder.decode(encryptedString))
        return new String(decryptedBytes, "UTF-8")
    }

    static String encrypt(String plainString) {
        def cipher = initCipher(Cipher.ENCRYPT_MODE, generateSecretKeyFromPassword())
        def encryptedBytes = cipher.doFinal(plainString.getBytes("UTF-8"))
        return Base64.encoder.encodeToString(encryptedBytes)
    }

    static byte[] generateSecretKeyFromPassword() {
        def md5 = MessageDigest.getInstance("MD5")
        def md5Hash = md5.digest(password.getBytes());
        return Arrays.copyOf(md5Hash, 16);
    }

    static Cipher initCipher(int cipherMode, byte[] secretKey) {
        def keySpec = new SecretKeySpec(secretKey, "AES")
        def cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(cipherMode, keySpec)
        return cipher
    }
}
