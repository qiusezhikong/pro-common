package com.miismi.common.cache;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BeeCacheUtil {
    private static final Logger LOG = LoggerFactory.getLogger(BeeCacheUtil.class);
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, BeeCache>> classCaches = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<OnLoadListener, ConcurrentHashMap<String, BeeCache>> objCaches = new ConcurrentHashMap<>();

    /**
     * 添加数据加载逻辑
     */
    public static void add(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            CacheParam cacheParam = method.getAnnotation(CacheParam.class);
            if (cacheParam == null) {
                continue;
            }
            String key = cacheParam.key();
            TimeUnit timeUnit = cacheParam.timeUnit();
            long duration = cacheParam.duration();
            ExpirationTimeType expirationTimeType = cacheParam.expirationTimeType();

            if (null == key || key.trim().equals("")) {
                continue;
            }
            long timeInterval = timeUnit.toMillis(duration);
            if (timeInterval <= 10000L) {
                timeInterval = 10000L;
            }
            BeeCache beeBeeCache = new BeeCache.Builder(key)
                    .setTimeInterval(timeInterval)
                    .setExpirationTimeType(expirationTimeType)
                    .setClass(clazz).build();
            ConcurrentHashMap<String, BeeCache> newCaches = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, BeeCache> caches = classCaches.putIfAbsent(clazz, newCaches);
            if (caches == null) {
                caches = newCaches;
            }
            caches.putIfAbsent(key, beeBeeCache);
        }
    }

    /**
     * 添加数据加载逻辑
     */
    public static void add(OnLoadListener onLoadListener) {
        Class<?> clazz = onLoadListener.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            CacheParam cacheParam = method.getAnnotation(CacheParam.class);
            if (null == cacheParam) {
                continue;
            }
            String key = cacheParam.key();
            TimeUnit timeUnit = cacheParam.timeUnit();
            long duration = cacheParam.duration();
            ExpirationTimeType expirationTimeType = cacheParam.expirationTimeType();

            if (null == key || key.trim().equals("")) {
                continue;
            }
            long timeInterval = timeUnit.toMillis(duration);
            if (timeInterval <= 10000L) {
                timeInterval = 10000L;
            }
            BeeCache beeBeeCache = new BeeCache.Builder(key)
                    .setTimeInterval(timeInterval)
                    .setExpirationTimeType(expirationTimeType)
                    .setClass(clazz)
                    .setOnLoadListener(onLoadListener).build();
            ConcurrentHashMap<String, BeeCache> newCaches = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, BeeCache> caches = objCaches.putIfAbsent(onLoadListener, newCaches);
            if (caches == null) {
                caches = newCaches;
            }
            caches.putIfAbsent(key, beeBeeCache);

        }
    }

    /**
     * 移除数据加载逻辑
     */
    public static void remove(OnLoadListener listener) {
        objCaches.remove(listener);
    }

    /**
     * 获取数据
     */
    public static <T> T getData(String key) {
        for (Map.Entry<Class<?>, ConcurrentHashMap<String, BeeCache>> entry : classCaches.entrySet()) {
            ConcurrentHashMap<String, BeeCache> value = entry.getValue();
            BeeCache beeCache = value.get(key);
            if (beeCache != null) {
                return (T) beeCache.getData();
            }
        }
        LOG.info("获取的缓存不存在：key={}", key);
        return null;
    }

    /**
     * 获取数据
     */
    public static <T> T getData(OnLoadListener listener, String key) {
        ConcurrentHashMap<String, BeeCache> caches = objCaches.get(listener);
        if (caches != null) {
            BeeCache beeCache = caches.get(key);
            if (beeCache != null) {
                return (T) beeCache.getData();
            }
        }
        LOG.info("获取的缓存不存在：{}, key={}", listener.getClass(), key);
        return null;
    }
}
