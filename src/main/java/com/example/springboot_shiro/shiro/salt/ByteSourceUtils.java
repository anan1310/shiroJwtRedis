package com.example.springboot_shiro.shiro.salt;

import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;

public class ByteSourceUtils {
    public static ByteSource bytes(byte[] bytes){
        return new MyByteSource(bytes);
    }
    public static ByteSource bytes(String arg0){
        return new MyByteSource(arg0.getBytes());
    }

}
