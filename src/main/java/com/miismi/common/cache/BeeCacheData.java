package com.miismi.common.cache;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class BeeCacheData<T> implements BeeCache<T> {
    private static final ConcurrentHashMap<String, BeeCacheData<?>> cacheMap = new ConcurrentHashMap<>();

    private final String key;
    private final long timeInterval;
    private final ExpirationTimeType expirationTimeType;
    private final DataLoad<T> dataLoad;

    private long dataTimestamp;
    private T data;

    public BeeCacheData(Builder<T> builder) {
        this.key = builder.key;
        this.timeInterval = builder.timeInterval;
        this.expirationTimeType = builder.expirationTimeType;
        this.dataLoad = builder.dataLoad;
    }

    @Override
    public T getData() {
        if (dataTimestamp == 0 || isExpired()) {
            synchronized (this) {
                if (dataTimestamp == 0 || isExpired()) {
                    T load = this.dataLoad.load(this.key);
                    if (load != null) {
                        this.data = load;
                        this.dataTimestamp = System.currentTimeMillis();
                    }
                }
            }
        }
        return this.data;
    }

    public T getCloneData() {
        T data = getData();
        if (data instanceof HashMap) {
            data = (T) ((HashMap) data).clone();
        } else if(data instanceof ArrayList) {
            data = (T) ((ArrayList) data).clone();
        } else if(data instanceof LinkedList) {
            data = (T) ((LinkedList) data).clone();
        }
        return data;
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

    /**
     * 数据到期失效类型
     */
    public enum ExpirationTimeType {
        /**
         * 数据过期：自然周期间隔
         */
        NaturalCycleInterval,
        /**
         * 数据过期：顺延周期间隔
         */
        DeferredCycleInterval
    }

    /**
     * key冲突的处理方式
     */
    public enum MergeType {
        /**
         * 丢弃
         */
        DISCARD,
        /**
         * 替换
         */
        REPLACE
    }

    public static class Time{
        public static final long DATE = 24 * 3600 * 1000L;
        public static final long HOUR = 3600 * 1000L;
        public static final long MINUTE = 60 * 1000L;
        public static final long SECOND = 1000L;
    }

    public static class Builder<T> {

        private final String key;
        private ExpirationTimeType expirationTimeType = ExpirationTimeType.NaturalCycleInterval;
        private MergeType mergeType = MergeType.DISCARD;
        private long timeInterval = Time.DATE;
        private DataLoad<T> dataLoad = null;

        /**
         * @param key 数据表
         */
        public Builder(String key) {
            this.key = key;
        }

        /**
         * @param dataLoad 数据加载
         */
        public Builder<T> setDataLoad(DataLoad<T> dataLoad) {
            this.dataLoad = dataLoad;
            return this;
        }

        /**
         * @param timeInterval 数据有效时长
         */
        public Builder<T> setTimeInterval(long timeInterval) {
            this.timeInterval = timeInterval;
            return this;
        }

        /**
         * @param expirationTimeType 数据过期类型
         */
        public Builder<T> setExpirationTimeType(ExpirationTimeType expirationTimeType) {
            this.expirationTimeType = expirationTimeType;
            return this;
        }

        /**
         * @param mergeType mergeType
         */
        public Builder<T> setMergeType(MergeType mergeType) {
            this.mergeType = mergeType;
            return this;
        }

        public BeeCacheData<T> build() {
            if (this.dataLoad == null) {
                throw new NullPointerException("this.dataLoad is null!");
            }
            BeeCacheData<T> newCacheDataImpl = new BeeCacheData<>(this);
            BeeCacheData<?> oldCacheDataImpl = cacheMap.putIfAbsent(this.key, newCacheDataImpl);
            if (oldCacheDataImpl != null) {
                switch (this.mergeType) {
                    case DISCARD:
                        return (BeeCacheData<T>) oldCacheDataImpl;
                    case REPLACE:
                        cacheMap.put(this.key, newCacheDataImpl);
                        return newCacheDataImpl;
                }
            }
            return newCacheDataImpl;
        }
    }
}
