/**
 * $RCSfile$ $Revision: 3144 $ $Date: 2005-12-01 14:20:11 -0300 (Thu, 01 Dec
 * 2005) $
 * 
 * Copyright (C) 2004-2008 Jive Software. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.chh.ap.cs.util.cache;


import com.chh.ap.cs.util.cache.redis.JedisFactory;
import com.chh.ap.cs.util.cache.redis.RedisCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates Cache objects. The returned caches will either be local or clustered
 * depending on the clustering enabled setting and a user's license.
 * 
 * <p>
 * When clustered caching is turned on, cache usage statistics for all caches
 * that have been created are periodically published to the clustered cache
 * named "opt-$cacheStats".
 * </p>
 * 
 */
@SuppressWarnings("rawtypes")
public class CacheFactory {

//    private static final Logger log = LoggerFactory.getLogger(CacheFactory.class);

    public static String LOCAL_CACHE_PROPERTY_NAME = "cache.clustering.local.class";
    public static String CLUSTERED_CACHE_PROPERTY_NAME = "cache.clustering.clustered.class";

    /**
     * Storage for all caches that get created.
     */
    private static Map<String, Cache> caches = new ConcurrentHashMap<String, Cache>();
    private static List<String> localOnly = Collections.synchronizedList(new ArrayList<String>());

    public static final int DEFAULT_MAX_CACHE_SIZE = 1024 * 256;
    public static final long DEFAULT_MAX_CACHE_LIFETIME = 6 * 3600 * 1000;

    private JedisFactory jedisFactory;

    public CacheFactory() {
    }

    /**
     * Returns an array of all caches in the system.
     * 
     * @return an array of all caches in the system.
     */
    public static Cache[] getAllCaches() {
        List<Cache> values = new ArrayList<Cache>();
        for (Cache cache : caches.values()) {
            values.add(cache);
        }
        return values.toArray(new Cache[values.size()]);
    }

    /**
     * Returns the named cache, creating it as necessary.
     * 
     * @param name
     *            the name of the cache to create.
     * @return the named cache, creating it as necessary.
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends Cache> T createCache(String name) {
        T cache = (T) caches.get(name);
        if (cache != null) {
            return cache;
        }
        cache = (T) new DefaultCache(name, DEFAULT_MAX_CACHE_SIZE, DEFAULT_MAX_CACHE_LIFETIME);
        return cache;
    }

    /**
     * Returns the named cache, creating it as necessary.
     * 
     * @param name
     *            the name of the cache to create.
     * @param cacheType
     *            the type of the cache,
     *            {@link Cache} fields
     *            start which CACHE_TYPE_
     * @return the named cache, creating it as necessary.
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends Cache> T createCache(String name, int cacheType) {
        T cache = (T) caches.get(name);
        if (cache != null) {
            return cache;
        }
        switch (cacheType) {
        case Cache.CACHE_TYPE_DEFAULT: {
            cache = (T) new DefaultCache(name, DEFAULT_MAX_CACHE_SIZE, DEFAULT_MAX_CACHE_LIFETIME);
            break;
        }
        case Cache.CACHE_TYPE_REDIS: {
            cache = (T) new RedisCache(jedisFactory);
            cache.setName(name);
            break;
        }
        default: {
            cache = (T) new DefaultCache(name, DEFAULT_MAX_CACHE_SIZE, DEFAULT_MAX_CACHE_LIFETIME);
            break;
        }
        }
        return cache;
    }

    /**
     * Destroys the cache for the cache name specified.
     * 
     * @param name
     *            the name of the cache to destroy.
     */
    public static synchronized void destroyCache(String name) {
        Cache cache = caches.remove(name);
        if (cache != null) {
            if (localOnly.contains(name)) {
                localOnly.remove(name);
            } else {
            }
        }
    }

    public synchronized static void clearCaches() {
        for (String cacheName : caches.keySet()) {
            Cache cache = caches.get(cacheName);
            cache.clear();
        }
    }


}