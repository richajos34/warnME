package com.berkeley.irms.warnme.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemCache<T> {

    private final Map<String, T> cacheMap = new ConcurrentHashMap<>();

    public T get(String key) {
        return cacheMap.get(key);
    }

    public void put(String key, T value) {
        cacheMap.put(key, value);
    }

    public void clear() {
        cacheMap.clear();
    }
}
