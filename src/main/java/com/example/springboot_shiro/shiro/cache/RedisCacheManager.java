package com.example.springboot_shiro.shiro.cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
//redis缓冲管理器
public class RedisCacheManager  implements CacheManager {
    @Override
    public <K, V> Cache<K, V> getCache(String s) throws CacheException {

        return new RedisCache(s);
    }
}
