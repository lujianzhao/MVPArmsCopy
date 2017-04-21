package com.jess.arms.common.data.cipher;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author: lujianzhao
 * @date: 21/04/2017 10:14
 * @Description:
 */
public class AESCipher extends com.jess.arms.common.data.cipher.Cipher {

    private static final String CHARSET = "UTF-8";

    /**
     * 初始化向量，必须和IOS一致
     */
    private static final   String IV_STRING = "!@#$!@#$%^&**&^%";

    private String mKey;

    public AESCipher(String key) {
        this.mKey = key;
    }

    @Override
    public byte[] decrypt(byte[] res) {
        try {
            byte[] keyBytes = mKey.getBytes(CHARSET);
            return cipherOperation(res, keyBytes, Cipher.DECRYPT_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public byte[] encrypt(byte[] res) {
        try {
            byte[] keyBytes = mKey.getBytes(CHARSET);
            return cipherOperation(res, keyBytes, Cipher.ENCRYPT_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }


    private byte[] cipherOperation(byte[] contentBytes, byte[] keyBytes, int mode) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        byte[] initParam = IV_STRING.getBytes(CHARSET);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(mode, secretKey, ivParameterSpec);

        return cipher.doFinal(contentBytes);
    }


}
