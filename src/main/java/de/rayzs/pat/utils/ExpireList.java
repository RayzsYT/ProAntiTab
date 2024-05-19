package de.rayzs.pat.utils;

import com.google.common.cache.*;
import java.util.concurrent.TimeUnit;

public class ExpireList<T> {

    private final Cache<Object, Object> cache;

    public ExpireList(long expireTime, TimeUnit timeUnit) {
        cache = CacheBuilder.newBuilder().concurrencyLevel(2).expireAfterWrite(expireTime, timeUnit).build();
    }

    public ExpireList(ExpireList<T> expireList) {
        cache = expireList.getCache();
    }

    public boolean add(T t) {
        if(contains(t)) return false;
        cache.put(t, (byte) 0);
        return true;
    }

    public void addIgnoreIfContains(T t) {
        cache.put(t, (byte) 0);
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
}
