package com.reyzeny.twist;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class AESUtil {
    public static String encryptMessage(String message, String encryptionKey) {
        try
        {

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, KeyGenerator.makeKey(encryptionKey));
            byte[] encryptedText = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return android.util.Base64.encodeToString(encryptedText, Base64.DEFAULT);
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }


    public static String decryptMessage(String message, String encryptionKey) {
        try
        {

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, KeyGenerator.makeKey(encryptionKey));
            byte[] decryptedText = cipher.doFinal(android.util.Base64.decode(message, Base64.DEFAULT));
            return new String(decryptedText);
        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
}
