package com.example.healthassistant;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class to handle AES encryption and decryption.
 * Uses AES with CBC mode and PKCS5 padding.
 * NOTE: In a production environment, never hard-code keys—use secure key management like Android Keystore.
 */
public class EncryptionUtils {

    // 16-byte secret key (must be 16, 24, or 32 bytes for AES)
    private static final String key = "1234567890123456";

    // 16-byte initialization vector (IV) for CBC mode
    private static final String initVector = "encryptionIntVec";

    /**
     * Encrypts a plain text string using AES/CBC/PKCS5Padding.
     *
     * @param value the plain text to encrypt
     * @return the Base64-encoded encrypted string, or null if an error occurs
     */
    public static String encrypt(String value) {
        try {
            // Initialize IV and SecretKeySpec with UTF-8 encoding
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            // Create AES cipher instance
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

            // Initialize cipher in encryption mode
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            // Perform encryption and encode with Base64
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeToString(encrypted, Base64.DEFAULT);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypts a Base64-encoded AES-encrypted string back into plain text.
     *
     * @param encrypted the Base64-encoded encrypted string
     * @return the decrypted plain text, or null if decryption fails
     */
    public static String decrypt(String encrypted) {
        try {
            // Initialize IV and SecretKeySpec with UTF-8 encoding
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            // Create AES cipher instance
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

            // Initialize cipher in decryption mode
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            // Decode Base64 input and perform decryption


            byte[] original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
            return new String(original);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}