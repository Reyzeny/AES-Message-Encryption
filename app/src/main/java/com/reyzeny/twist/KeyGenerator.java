package com.reyzeny.twist;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.github.javafaker.Faker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class KeyGenerator {
    public static String generateKeyWord() {
        Faker faker = new Faker();

        String stringKey = faker.name().name().toLowerCase();
        System.out.println("string key is " + stringKey);
        return stringKey;
    }

    public static SecretKey makeKey(String mKey) {
        byte[] key;
        MessageDigest sha = null;

        try {
            key = mKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
