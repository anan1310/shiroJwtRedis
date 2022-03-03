package com.example.springboot_shiro.config;

import com.example.springboot_shiro.shiro.cache.RedisCacheManager;
import com.example.springboot_shiro.shiro.realm.CusotomerRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

//用来整合shiro框架的相关配置类
/*
ShiroFilterFactoryBean依赖于DefaultSecurityManager
DefaultSecurityManager依赖于Realm
 */
@Configuration
public class ShiroConfig {
    //   1.创建ShiroFilterFactoryBean --> 负责拦截所有请求
    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultSecurityManager defaultSecurityManager) {
//        1.创建
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

//        给filter设置安全管理器 注入DefaultSecurityManager
        shiroFilterFactoryBean.setSecurityManager(defaultSecurityManager);
//        配置系统受限资源
//        配置系统公共资源（anon 公共资源）authc 请求这个资源需要认证和授权 map 应该是有序的
        Map<String, String> map = new LinkedHashMap<>();
        map.put("/user/login", "anon");  //需要注意顺序
        map.put("/user/register", "anon");
        map.put("/register.jsp", "anon");
        map.put("/user/getImage","anon");//验证码
        map.put("/**", "authc"); //拦截所有的请求


//        配置认证授和授权规则 配置那些资源是受限的 还是公用的
        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);


//        默认认证界面路径
        shiroFilterFactoryBean.setLoginUrl("/login.jsp"); //身份认证失败后，，跳转到登录页面


        return shiroFilterFactoryBean;
    }

    //   2.创建安全管理器
    @Bean
    public DefaultWebSecurityManager getDefaultWebSecurity(Realm realm) {

        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
//       给安全管理器设置realm
        defaultWebSecurityManager.setRealm(realm);
        return defaultWebSecurityManager;

    }

    //   3.创建自定义个realm
    @Bean(value = "realm")
//    @Primary
    public Realm getRealm() {

        CusotomerRealm cusotomerRealm = new CusotomerRealm();
//        修改凭证校验匹配器（MD5）
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("MD5");//设置算法
        hashedCredentialsMatcher.setHashIterations(1024); //hash散列次数
        cusotomerRealm.setCredentialsMatcher(hashedCredentialsMatcher);
        //      开启缓冲管理 EhCacheManager
        cusotomerRealm.setCachingEnabled(true); //开启全局缓冲
        cusotomerRealm.setAuthenticationCachingEnabled(true); //认证缓冲
        cusotomerRealm.setAuthorizationCachingEnabled(true); //授权缓冲
        cusotomerRealm.setCacheManager(new RedisCacheManager());  //我们之后使用的是redis作为缓冲去存取数库中的数据

        return cusotomerRealm;
    }




}
