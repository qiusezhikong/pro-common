package com.miismi.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class BeeCache implements Cache {
    private static final Logger LOG = LoggerFactory.getLogger(BeeCache.class);
    private final String key;
    private final long timeInterval;
    private final ExpirationTimeType expirationTimeType;
    private final Class<?> clazz;
    private final OnLoadListener onLoadListener;

    private long dataTimestamp; //数据时间戳
    private Object data;        //缓存数据

    public BeeCache(Builder builder) {
        this.key = builder.key;
        this.timeInterval = builder.timeInterval;
        this.expirationTimeType = builder.expirationTimeType;
        this.clazz = builder.clazz;
        this.onLoadListener = builder.onLoadListener;
    }

    @Override
    public Object getData() {
        if (this.dataTimestamp == 0 || isExpired()) {
            synchronized (this) {
                if (this.dataTimestamp == 0 || isExpired()) {
                    try {
                        Method[] methods = this.clazz.getDeclaredMethods();
                        for (Method method : methods) {
                            CacheParam cacheParam = method.getAnnotation(CacheParam.class);
                            if (cacheParam != null && this.key.equals(cacheParam.key())) {
                                method.setAccessible(true);
                                Object instance = this.onLoadListener;
                                if (instance == null) {
                                    instance = clazz.newInstance();
                                }
                                Object result = method.invoke(instance);
                                if (result != null) {
                                    this.data = result;
                                    this.dataTimestamp = System.currentTimeMillis();
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        LOG.info("DataLoad Exception...");
                        e.printStackTrace();
                    }
                }
            }
        }
        return this.data;
    }

    public long getDataTimestamp() {
        return this.dataTimestamp;
    }

    public void clear() {
        this.dataTimestamp = 0;
        this.data = null;
    }

    /**
     * 是否过期
     *
     * @return 是否过期
     */
    @Override
    public boolean isExpired() {
        long time = 0L;
        try {
            time = new SimpleDateFormat("yyyyMMdd").parse("19700101").getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long timestamp = this.dataTimestamp - time;
        long currentTime = System.currentTimeMillis() - time;
        switch (this.expirationTimeType) {
            case NaturalCycleInterval:
                return currentTime / this.timeInterval > timestamp / this.timeInterval;
            case DeferredCycleInterval:
                return currentTime - timestamp > this.timeInterval;
        }
        return false;
    }

    public static class Builder {

        private final String key;
        private ExpirationTimeType expirationTimeType = ExpirationTimeType.NaturalCycleInterval;
        private long timeInterval = TimeUnit.DAYS.toMillis(1);
        private Class<?> clazz = null;
        private OnLoadListener onLoadListener = null;


        /**
         * @param key 数据表
         */
        public Builder(String key) {
            this.key = key;
        }

        /**
         * @param clazz 数据加载
         */
        public Builder setClass(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        /**
         * @param listener 数据加载
         */
        public Builder setOnLoadListener(OnLoadListener listener) {
            this.onLoadListener = listener;
            return this;
        }

        /**
         * @param timeInterval 数据有效时长
         */
        public Builder setTimeInterval(long timeInterval) {
            this.timeInterval = timeInterval;
            return this;
        }

        /**
         * @param expirationTimeType 数据过期类型
         */
        public Builder setExpirationTimeType(ExpirationTimeType expirationTimeType) {
            this.expirationTimeType = expirationTimeType;
            return this;
        }

        public BeeCache build() {
            if (this.clazz == null) {
                throw new NullPointerException("this.clazz is null!");
            }
            return new BeeCache(this);
        }
    }
}
