package com.chh.ap.cs.util.cache.redis;

import com.chh.ap.cs.util.SerializeUtil;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * 
 * 
 * @date 2016年11月23日
 * @version 1.0.0
 * @author 
 * 
 * @param <K> K 值由 redis key 和 field 组成 ; list[0]=key list[1]=field;
 * @param <V> filed value 值
 */
public class RedisHashCache<K extends List, V> extends RedisCache<K,V> {

    private JedisFactory jedisFactory;

    private String name;

    private long maxLifetime;

    public RedisHashCache(JedisFactory jedisFactory) {
        super(jedisFactory);
        this.jedisFactory = jedisFactory;
    }
    
    public Map getAllFields(Object key) {
        byte[] bs = serializeKey(key);
        if (bs == null) {
            return null;
        }
        Jedis jedis = jedisFactory.getJedis();
        //获取的类型为一个hash类型的Value
        Map<byte[],byte[]> fmap = jedis.hgetAll(bs);
        
        jedisFactory.returnBackJedis(jedis);
        Map<Object, Object> map = new HashMap<Object, Object>();
        Object fk = null ; 
        Object fv = null ; 
        for(Map.Entry<byte[],byte[]> entry : fmap.entrySet()){ 
        	fk = SerializeUtil.unserialize(entry.getKey());
        	fv = SerializeUtil.unserialize(entry.getValue());
            map.put(fk, fv);
        }
        return map;
    }
    
    @Override
    public V put(K keyField, V value) {
    	if(!(keyField instanceof List) ){
	   		 return null;
	   	}
	   	List kf = (List)keyField;
	   	if(kf == null || kf.size() != 2){
	   		return null;
	   	}
	   	//KEY
	    byte[] bs = serializeKey(kf.get(0));
	    if (bs == null) {
	       return null;
	    }
	    //Field
	    byte[] fk = null;
	    if (kf.get(1) instanceof byte[]) {
	    	fk = (byte[])kf.get(1);
	    }else{
	    	fk = SerializeUtil.serialize(kf.get(1));
	    }
	    //field value
	    byte[] fv = null;
        if (value instanceof byte[]) {
        	fv = (byte[])value;
        }else{
        	fv = SerializeUtil.serialize(value);
        }
	    
	    Jedis jedis = jedisFactory.getJedis();
	    jedis.hset(bs, fk, fv);
	    jedisFactory.returnBackJedis(jedis);
	    return value;
       
    }
    
    @Override
    public V get(Object keyField) {
    	if(!(keyField instanceof List) ){
	   		 return null;
	   	}
	   	List kf = (List)keyField;
	   	if(kf == null || kf.size() != 2){
	   		return null;
	   	}
    	
        byte[] bs = serializeKey(kf.get(0));
        if (bs == null) {
            return null;
        }
        byte[] fk = null;
        if (kf.get(1) instanceof byte[]) {
        	fk = (byte[])kf.get(1);
        }else{
        	fk = SerializeUtil.serialize(kf.get(1));
        }
        Jedis jedis = jedisFactory.getJedis();
        Object object = SerializeUtil.unserialize(jedis.hget(bs, fk));
        
        jedisFactory.returnBackJedis(jedis);
        return (V)object;
    }
    
    @Override
    public V remove(Object keyField) {
    	if(!(keyField instanceof List) ){
	   		 return null;
	   	}
	   	List kf = (List)keyField;
	   	if(kf == null || kf.size() != 2){
	   		return null;
	   	}
	   	//KEY
	    byte[] bs = serializeKey(kf.get(0));
	    if (bs == null) {
	       return null;
	    }
	    //Field
	    byte[] fk = null;
	    if (kf.get(1) instanceof byte[]) {
	    	fk = (byte[])kf.get(1);
	    }else{
	    	fk = SerializeUtil.serialize(kf.get(1));
	    }
        
        Jedis jedis = jedisFactory.getJedis();
        jedis.select(dbIndex);
        Object object = SerializeUtil.unserialize(jedis.hget(bs,fk));
        jedis.hdel(bs, fk);
        jedisFactory.returnBackJedis(jedis);
        return (V)object;
    }
    

}
