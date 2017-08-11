package com.chh.ap.cs.util.cache.redis;

import com.chh.ap.cs.util.SerializeUtil;
import com.chh.ap.cs.util.cache.Cache;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
public class RedisCache<K, V> implements Cache<K, V> {

    private JedisFactory jedisFactory;

    private String name;

    private long maxLifetime;

    protected int dbIndex = 0;

    public RedisCache(JedisFactory jedisFactory) {
        this.jedisFactory = jedisFactory;
    }




    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    
    public boolean containsKey(Object key) {
        byte[] bs = serializeKey(key);
        if (bs == null) {
            return false;
        }
        Jedis jedis = jedisFactory.getJedis();
        jedis.select(dbIndex);
        Set<byte[]> keys = jedis.keys(bs);
        jedisFactory.returnBackJedis(jedis);
        return keys.isEmpty();
    }

    
    public boolean containsValue(Object value) {
        return false;
    }

    @SuppressWarnings("unchecked")
    public V get(Object key) {
        byte[] bs = serializeKey(key);
        if (bs == null) {
            return null;
        }
        Jedis jedis = jedisFactory.getJedis();
        jedis.select(dbIndex);
        Object object = SerializeUtil.unserialize(jedis.get(bs));
        jedisFactory.returnBackJedis(jedis);
        return (V) object;
    }

    
    public V put(K key, V value) {
        byte[] bs = serializeKey(key);
        if (bs == null) {
            return null;
        }
        byte[] values = SerializeUtil.serialize(value);
        Jedis jedis = jedisFactory.getJedis();
        jedis.select(dbIndex);
        if (maxLifetime > 0) {
            jedis.setex(bs, (int) (maxLifetime / 1000), values);
        } else {
            jedis.set(bs, values);
        }
        jedisFactory.returnBackJedis(jedis);
        return value;
    }

    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        byte[] bs = serializeKey(key);
        if (bs == null) {
            return null;
        }
        Jedis jedis = jedisFactory.getJedis();
        jedis.select(dbIndex);
        Object object = SerializeUtil.unserialize(jedis.get(bs));
        jedis.del(bs);
        jedisFactory.returnBackJedis(jedis);
        return (V) object;
    }

    
    public void putAll(Map<? extends K, ? extends V> m) {
        // TODO Auto-generated method stub

    }

    
    public void clear() {
        // TODO Auto-generated method stub

    }

    
    public Set<K> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public Collection<V> values() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public Set<Entry<K, V>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public long getMaxCacheSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    
    public void setMaxCacheSize(int maxSize) {
        // TODO Auto-generated method stub

    }

    
    public long getMaxLifetime() {
        return maxLifetime;
    }

    
    public void setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    
    public int getCacheSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    
    public long getCacheHits() {
        // TODO Auto-generated method stub
        return 0;
    }

    
    public long getCacheMisses() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getType() {
        return Cache.CACHE_TYPE_REDIS;
    }

    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 序列化KEY值
     * 
     * @param key
     * @return
     */
    protected byte[] serializeKey(Object key) {
        if (key == null) {
            return null;
        }
        if (key instanceof String) {
            String str = name + ":" + (String) key;
            return str.getBytes();
        }
        if (key instanceof Number || key instanceof Boolean) {
            String str = name + ":" + key.toString();
            return str.getBytes();
        }
        byte[] bs = SerializeUtil.serialize(key);
        return bs;
    }

    public JedisFactory getJedisFactory() {
        return jedisFactory;
    }

    public void setJedisFactory(JedisFactory jedisFactory) {
        this.jedisFactory = jedisFactory;
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }
}
