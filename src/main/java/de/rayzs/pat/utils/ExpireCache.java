package de.rayzs.pat.utils;

import java.util.concurrent.TimeUnit;
import com.google.common.cache.*;

public class ExpireCache<T, K> {

    private final Cache<Object, Object> cache;

    public ExpireCache(long expireTime, TimeUnit timeUnit) {
        cache = CacheBuilder.newBuilder().concurrencyLevel(2).expireAfterWrite(expireTime, timeUnit).build();
    }

    public ExpireCache(ExpireCache<T, K> expireList) {
        cache = expireList.getCache();
    }

    public boolean put(T t, K k) {
        if(contains(t)) return false;
        cache.put(t, k);
        return true;
    }

    public boolean remove(T t) {
        if(!contains(t)) return false;
        cache.invalidate(t);
        return true;
    }

    public K putAndGet(T t, K k) {
        put(t, k);
        return k;
    }

    public void clear() {
        cache.cleanUp();
    }

    public void putIgnoreIfContains(T t, K k) {
        cache.put(t, k);
    }

    public K get(T t) {
        Object result = cache.getIfPresent(t);;
        return result == null ? null : (K) result;
    }

    public K getOrDefault(T t, K k) {
        Object result = cache.getIfPresent(t);;
        return result == null ? k : (K) result;
    }

    public int getSize() {
        cache.cleanUp();
        return (int) cache.size();
    }

    public Cache<Object, Object> getCache() {
        return cache;
    }

    public boolean contains(T t) {
        return cache.getIfPresent(t) != null;
    }

    public boolean containsValue(K k) {
        return cache.asMap().containsValue(k);
    }
}