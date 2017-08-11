package com.chh.ap.cs.util.aes;





import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by admin on 2015/6/2.
 */
public class SecurityUtils {
    private static final String CHARSET = "UTF-8";

    public final static String MY_MES_KEY_STRING = "11111111111111111111111111111111";

    public static String encrypt(String text) {
        try {
            byte[] MY_AES_KEY = MY_MES_KEY_STRING.getBytes(CHARSET);
            // 设置加密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(MY_AES_KEY, "AES");
            IvParameterSpec iv = new IvParameterSpec(MY_AES_KEY, 0, 16);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            ByteGroup byteCollector = new ByteGroup();
            byteCollector.addBytes(text.getBytes(CHARSET));
            // ... + pad: 使用自定义的填充方式对明文进行补位填充
            byte[] padBytes = PKCS7Encoder.encode(byteCollector.size());
            byteCollector.addBytes(padBytes);

            byte[] finalbyte = byteCollector.toBytes();
            // 加密
            byte[] encrypted = cipher.doFinal(finalbyte);
            // 使用BASE64对加密后的字符串进行编码
            String base64Encrypted = Base64.encodeBase64String(encrypted);
            return base64Encrypted;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 对密文进行解密.
     *
     * @param text 需要解密的密文
     * @return 解密得到的明文
     * @throws AesException aes解密失败
     */
    public static String decrypt(String text) throws AesException {
        byte[] original;
        try {
            byte[] MY_AES_KEY = MY_MES_KEY_STRING.getBytes(CHARSET);
            // 设置解密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec key_spec = new SecretKeySpec(MY_AES_KEY, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(MY_AES_KEY, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, key_spec, iv);

            // 使用BASE64对密文进行解码
            byte[] encrypted = Base64.decodeBase64(text);
            // 解密
            original = cipher.doFinal(encrypted);
            // 去除补位字符
            byte[] bytes = PKCS7Encoder.decode(original);
            return new String(bytes, CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeBase64(byte[] b) {
        return Base64.encodeBase64String(b);
    }

    public static byte[] decodeBase64(String base64String) {
        return Base64.decodeBase64(base64String);
    }

//    public static void main(String[] args) throws AesException {
//        String encrypt = encrypt("你打发斯蒂芬撒旦法阿斯蒂芬阿斯蒂芬阿萨德发生的发生的发妹啊");
//        System.out.println(encrypt);
//        System.out.println(decrypt(encrypt));
//    }
}
