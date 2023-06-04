package com.miismi.common.cache;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BeeCacheUtil {
    private static final Logger LOG = LoggerFactory.getLogger(BeeCacheUtil.class);
    private static final ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<>();

    /**
     * 初始化数据加载逻辑
     * @param clazz class对象
     */
    public static void add(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            CacheParam cacheParam = method.getAnnotation(CacheParam.class);

            String key = cacheParam.key();
            TimeUnit timeUnit = cacheParam.timeUnit();
            long duration = cacheParam.duration();
            ExpirationTimeType expirationTimeType = cacheParam.expirationTimeType();

            if (null != key && !key.trim().equals("")) {
                long timeInterval = timeUnit.toMillis(duration);
                if (timeInterval <= 10000L) {
                    timeInterval = 10000L;
                }
                BeeCache beeBeeCache = new BeeCache.Builder(key)
                        .setTimeInterval(timeInterval)
                        .setExpirationTimeType(expirationTimeType)
                        .setDataLoadClass(clazz).build();
                cacheMap.putIfAbsent(key, beeBeeCache);
            } else {
                LOG.info("key is not available! ");
            }
        }
    }

    /**
     * 获取数据
     * @param key
     */
    public static<T> T getData(String key) {
        return (T) cacheMap.get(key).getData();
    }
}
