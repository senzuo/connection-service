package com.chh.ap.cs.util.aes;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * AES加解密器
 * <p>
 * Created by Niow on 2016/4/7.
 */
public class Encryptor {

    private static final Logger log = LoggerFactory.getLogger(Encryptor.class);

    private static final String CHARSET = "UTF-8";

    private String key;

    private byte[] MY_AES_KEY;

    public Encryptor() {

    }

    public Encryptor(String key) {
        if (key == null || key.trim().equals("")) {
            throw new IllegalArgumentException("AES加密KEY不能为空");
        }
        try {
            MY_AES_KEY = key.getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Key不支持UTF-8编码");
        }
    }

    public static String encrypt(String text, String aesKey) {
        try {
            byte[] aesKeyBytes = aesKey.getBytes(CHARSET);
            // 设置加密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
            IvParameterSpec iv = new IvParameterSpec(aesKeyBytes, 0, 16);
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
            log.error("AES加密出错", e);
        }
        return null;
    }

    /**
     * 对密文进行解密.
     *
     * @param text 需要解密的密文
     * @return 解密得到的明文
     * @throws AesException aes解密失败
     */
    public static String decrypt(String text, String aesKey) throws AesException {
        byte[] original;
        try {
            byte[] aesKeyBytes = aesKey.getBytes(CHARSET);
            // 设置解密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec key_spec = new SecretKeySpec(aesKeyBytes, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(aesKeyBytes, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, key_spec, iv);

            // 使用BASE64对密文进行解码
            byte[] encrypted = Base64.decodeBase64(text);
            // 解密
            original = cipher.doFinal(encrypted);
            // 去除补位字符
            byte[] bytes = PKCS7Encoder.decode(original);
            return new String(bytes, CHARSET);
        } catch (Exception e) {
            log.error("AES解密出错", e);
        }
        return null;
    }

    public String encrypt(String text) {
        return encrypt(text, key);
    }

    /**
     * 对密文进行解密.
     *
     * @param text 需要解密的密文
     * @return 解密得到的明文
     * @throws AesException aes解密失败
     */
    public String decrypt(String text) throws AesException {
        return decrypt(text, key);
    }
    

	/**
	 * 使用 aeskey对   对密文进行解密
	 * @param aesToken
	 * @param encryptData
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] aesToken, byte[] encryptData) throws Exception{
		SecretKeySpec skeySpec = new SecretKeySpec(aesToken, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
		byte[] decrypted = cipher.doFinal(encryptData);
		return decrypted;
	}
	
	    /**
	     * 方法名称：getAesKey
	     * @return
	     * @throws Exception
	     */
	    public static  byte[] getAesKey( byte[] key) throws Exception{
	        KeyGenerator keygen =  KeyGenerator.getInstance("AES");
	        SecureRandom random=SecureRandom.getInstance("SHA1PRNG");
	        random.setSeed(key);
	        keygen.init(128,random); //设置 为 128
	        SecretKey deskey = keygen.generateKey();
	        return deskey.getEncoded();
	    }
	    

		/**
		 * 使用aeskey 进行加密
		 * @param key
		 * @param data
		 * @return
		 * @throws Exception
		 */
		public static byte[] encrypt(byte[] key, byte[] data) throws Exception{
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
			byte[] encrypted = cipher.doFinal(data);
			return encrypted;
		}
		

    /**
     * 根据接收十六进制字符串转转换成RSA公钥
     * 传入数据：十六进制字符串
     * @param getPublicKey
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKey(byte[] hex) throws Exception {
        KeySpec keySpec = new X509EncodedKeySpec(hex);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = factory.generatePublic(keySpec);
        return publicKey;
    }
    

        /**
         * 使用rsa密钥 加密后的aes byte[] 数据
         * @param publicKey
         * @param data
         * @return
         * @throws Exception
         */
        public static byte[] encrypt(PublicKey publicKey, byte[] data) throws Exception{
            if(publicKey == null || data == null || data.length<0){
                return null;
            }
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        }
}
