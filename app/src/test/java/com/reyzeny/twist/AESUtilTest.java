package com.reyzeny.twist;

import org.junit.Test;

import javax.crypto.SecretKey;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class AESUtilTest {
    @Test
    public void testEncryptionWithMessageAndKey() {
        String key = KeyGenerator.generateKeyWord();
        String encryptedMessage = AESUtil.encryptMessage("Hello Pelumi", key);
        System.out.println("encrypted message is " + encryptedMessage);
        String decryptedMessage = AESUtil.decryptMessage(encryptedMessage, key);
        assertEquals("Hello Pelumi", decryptedMessage);
    }
}
