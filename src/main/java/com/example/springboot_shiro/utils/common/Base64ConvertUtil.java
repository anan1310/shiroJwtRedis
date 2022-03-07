package com.example.springboot_shiro.utils.common;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * base64工具（用于编码和解码，这个过程是可逆的）
 */
public class Base64ConvertUtil {
    private Base64ConvertUtil() {}
    /**
     * 编码
     *
     * @param str str
     * @return {@link String}
     * @throws UnsupportedEncodingException 不支持编码异常
     */
    public static String encode(String str) throws UnsupportedEncodingException {
        byte[] encodeBytes = Base64.getEncoder().encode(str.getBytes("utf-8"));
        return new String(encodeBytes);
    }


    /**
     * 解码
     *
     * @param str str
     * @return {@link String}
     * @throws UnsupportedEncodingException 不支持编码异常
     */
    public static String decode(String str) throws UnsupportedEncodingException {
        byte[] decodeBytes = Base64.getDecoder().decode(str.getBytes("utf-8"));
        return new String(decodeBytes);
    }


}
