package com.miismi.common.cache;

public interface BeeCache<T> {
    T getData();

    boolean isExpired();
}
