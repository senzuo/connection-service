package com.chh.ap.cs.util.cache.redis;

import com.chh.ap.cs.util.SerializeUtil;
import redis.clients.jedis.Jedis;

/**
 * 
 * 
 * 
 * 
 * @date 2015年12月21日
 * @version 1.0.0
 * @author Niow
 * 
 * @param <K>
 * @param <V>
 */
public class RedisStackCache<K, V> extends RedisCache<K,V> {

    private JedisFactory jedisFactory;

    private String name;

    private long maxLifetime;

    public RedisStackCache(JedisFactory jedisFactory) {
        super(jedisFactory);
        this.jedisFactory = jedisFactory;
    }

    @Override
    public V get(Object key) {
        byte[] bs = serializeKey(key);
        if (bs == null) {
            return null;
        }
        Jedis jedis = jedisFactory.getJedis();
        Object object = SerializeUtil.unserialize(jedis.rpop(bs));
        jedisFactory.returnBackJedis(jedis);
        return (V) object;
    }

    @Override
    public V put(K key, V value) {
        byte[] bs = serializeKey(key);
        if (bs == null) {
            return null;
        }
        byte[] values = null;
        if (value instanceof byte[]) {
            values = (byte[])value;
        }else{
            values = SerializeUtil.serialize(value);
        }
        Jedis jedis = jedisFactory.getJedis();
        jedis.lpush(bs,values);
        jedisFactory.returnBackJedis(jedis);
        return value;
    }

}
