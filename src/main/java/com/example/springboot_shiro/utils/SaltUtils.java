package com.example.springboot_shiro.utils;

import java.util.Random;
//生成盐
//1.还有一种方法，截取UUID的一区间

public class SaltUtils {

    public static String getSalt(int n ){
        char[] chars ="QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm0123456789!@#$%^&*()".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            char aChar = chars[new Random().nextInt(chars.length)];
              stringBuilder.append(aChar);
        }
        return  stringBuilder.toString();
    }

    public static void main(String[] args) {
        String salt = getSalt(10);
        System.out.println(salt);
    }
}
