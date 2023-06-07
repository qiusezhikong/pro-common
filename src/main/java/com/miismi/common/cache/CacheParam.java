package com.miismi.common.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheParam {

    /**
     * 缓存实例的唯一标识
     */
    String key();

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.DAYS;

    /**
     * 时长
     */
    long duration() default 1;

    /**
     * 自然时间间隔
     */
    ExpirationTimeType expirationTimeType() default ExpirationTimeType.NaturalCycleInterval;
}
