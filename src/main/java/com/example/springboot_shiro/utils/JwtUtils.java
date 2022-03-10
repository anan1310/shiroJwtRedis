package com.example.springboot_shiro.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.springboot_shiro.exception.CustomException;
import com.example.springboot_shiro.utils.common.Base64ConvertUtil;
import com.example.springboot_shiro.utils.common.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * 生成JWT的工具类
 *
 * @author anzhijie
 * @date 2022/03/04
 */
@PropertySource("classpath:config.properties")
@Component
public class JwtUtils {
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    /**
     * 过期时间改为从配置文件获取
     */
    private static String accessTokenExpireTime;

    /**
     * JWT认证加密私钥(Base64加密)
     */
    private static String encryptJWTKey;

    /**
     * 设置访问令牌到期时间
     * 使用@Value("${}")为了从配置文件中取值，使用set的方式将值获取到
     */
    @Value("${accessTokenExpireTime}")
    public void setAccessTokenExpireTime(String accessTokenExpireTime) {
        JwtUtils.accessTokenExpireTime = accessTokenExpireTime;
    }

    @Value("${encryptJWTKey}")
    public void setEncryptJWTKey(String encryptJWTKey) {
        JwtUtils.encryptJWTKey = encryptJWTKey;
    }

    /**
     * 校验令牌是否合法 （需要验签）
     *
     * @param token 令牌
     * @return boolean
     */
    public static boolean verify(String token) {
//        验签 HMACSHA256(base64UrlEncode(header) + "." +base64UrlEncode(payload),secret) 要和生成签名的前后一致，才可以验证成功
        try {
            String secret = getClaim(token, Constant.ACCOUNT) + Base64ConvertUtil.decode(encryptJWTKey);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token); //进行验证
            return true;
        } catch (UnsupportedEncodingException e) {
            logger.error("JWTToken认证解密出现UnsupportedEncodingException异常:{}", e.getMessage());
            throw new CustomException("JWTToken认证解密出现UnsupportedEncodingException异常:" + e.getMessage());
        }

    }

    /**
     * 获取token中的信息
     *
     * @param token 令牌
     * @param claim 信息
     * @return {@link String}
     */
    public static String getClaim(String token, String claim) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            // 只能输出String类型，如果是其他类型返回null
            return jwt.getClaim(claim).asString();
        } catch (JWTDecodeException e) {
            logger.error("解密Token中的公共信息出现JWTDecodeException异常:{}", e.getMessage());
            throw new CustomException("解密Token中的公共信息出现JWTDecodeException异常:" + e.getMessage());
        }
    }

    /**
     * 生成签名 由3部分构成
     * 1.header 算法
     * 2.payload 用户信息
     * 3.signature
     *
     * @param account          用户名称
     * @param currentTimeMillis 当前时间
     * @return {@link String}
     */
    public static String sign(String account, String currentTimeMillis) {

        try {

            //账号加JWT私钥加密（秘钥）
            String secret = account + Base64ConvertUtil.decode(encryptJWTKey);

            // 此处过期时间是以毫秒为单位，所以乘以1000
            Date date = new Date(System.currentTimeMillis() + Long.parseLong(accessTokenExpireTime) * 1000);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withClaim("username", account)
                    .withClaim("currentTimeMillis", currentTimeMillis)
                    .withExpiresAt(date)//指定令牌的过期时间
                    .sign(algorithm);
        } catch (UnsupportedEncodingException e) {
            logger.error("JWTToken加密出现UnsupportedEncodingException异常:{}", e.getMessage());
            throw new CustomException("JWTToken加密出现UnsupportedEncodingException异常:" + e.getMessage());
        }


    }

}
