package org.lukhnos.portmobile.lang;

import java.util.HashMap;
import java.util.Map;

public abstract class ClassValue<T> {
    boolean defaultCached = false;
    T defaultCachedMember = null;
    Map<Class<?>, T> cache = new HashMap<>();

    protected ClassValue() {}

    protected abstract T computeValue(Class<?> type);

    public T get(Class<?> type) {
        // Note: this does not care about synchronization.
        if (cache.containsKey(type)) {
            return cache.get(type);
        }

        T value = computeValue(type);
        cache.put(type, value);
        return value;
    }

    public void remove(Class<?> type) {
        // Note: this does not care about synchronization.
        cache.remove(type);
    }
}
