package com.miismi.common.cache;

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