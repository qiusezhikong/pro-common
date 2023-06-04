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

    private long dataTimestamp; //数据时间戳
    private Object data;        //缓存数据

    public BeeCache(Builder builder) {
        this.key = builder.key;
        this.timeInterval = builder.timeInterval;
        this.expirationTimeType = builder.expirationTimeType;
        this.clazz = builder.clazz;
    }

    @Override
    public Object getData() {
        if (dataTimestamp == 0 || isExpired()) {
            synchronized (this) {
                if (dataTimestamp == 0 || isExpired()) {
                    try {
                        Method[] methods = clazz.getDeclaredMethods();
                        Object data = null;
                        for (Method method : methods) {
                            CacheParam cacheParam = method.getAnnotation(CacheParam.class);
                            if (this.key.equals(cacheParam.key())) {
                                Object dataLoad = clazz.newInstance();
                                method.setAccessible(true);
                                data = method.invoke(dataLoad);
                                break;
                            }
                        }
                        if (data != null) {
                            this.data = data;
                            this.dataTimestamp = System.currentTimeMillis();
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

        /**
         * @param key 数据表
         */
        public Builder(String key) {
            this.key = key;
        }

        /**
         * @param clazz 数据加载
         */
        public Builder setDataLoadClass(Class clazz) {
            this.clazz = clazz;
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
                throw new NullPointerException("this.dataLoad is null!");
            }
            return new BeeCache(this);
        }
    }
}
