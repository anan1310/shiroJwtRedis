package com.example.springboot_shiro.shiro.cache;
import com.example.springboot_shiro.utils.ApplicationContextUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


import java.util.Collection;
import java.util.Set;


public class RedisCache<K, V> implements Cache<K, V> {

    public RedisCache(String cashName) {
        this.cashName = cashName;
    }

    public RedisCache() {
    }

    private  String cashName;

    @Override
    public V get(K k) throws CacheException {
//        return (V) getRedisTemplate().opsForValue().get(k.toString());
        return (V) getRedisTemplate().opsForHash().get(this.cashName,k.toString());
    }


    @Override
    public V put(K k, V v) throws CacheException {
//        getRedisTemplate().opsForValue().set(k.toString(),v);
        getRedisTemplate().opsForHash().put(this.cashName,k.toString(),v);
        return null;
    }

    @Override
    public V remove(K k) throws CacheException {
        return (V)getRedisTemplate().opsForHash().delete(this.cashName,k.toString());
    }

    @Override
    public void clear() throws CacheException {
        getRedisTemplate().delete(this.cashName);

    }

    @Override
    public int size() {
        return getRedisTemplate().opsForHash().size(this.cashName).intValue();
    }

    @Override
    public Set<K> keys() {
        return getRedisTemplate().opsForHash().keys(this.cashName);
    }

    @Override
    public Collection<V> values() {
        return getRedisTemplate().opsForHash().values(this.cashName);
    }
//通过context获取到template
    private RedisTemplate getRedisTemplate(){
        RedisTemplate redisTemplate = (RedisTemplate) ApplicationContextUtils.getBean("redisTemplate");
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }

}
