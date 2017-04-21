package com.jess.arms.common.data.cipher;


import android.util.Base64;

/**
 * @author lujianzhao
 * @date 14-7-31
 */
public class Base64Cipher {
    private Cipher cipher;

    public Base64Cipher() {
    }

    public Base64Cipher(Cipher cipher) {
        this.cipher = cipher;
    }

    /**
     * 解密
     * @param res
     * @return
     */
    public String decrypt(String res) {
        byte[] decryptedBytes = Base64.decode(res, Base64.DEFAULT);
        if(cipher != null){
            decryptedBytes = cipher.decrypt(decryptedBytes);
        }
        return new String(decryptedBytes);
    }

    /**
     * 加密
     * @param res
     * @return
     */
    public String encrypt(String res) {
        byte[] encryptedBytes = res.getBytes();
        if(cipher != null){
            encryptedBytes = cipher.encrypt(encryptedBytes);
        }
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }
}
