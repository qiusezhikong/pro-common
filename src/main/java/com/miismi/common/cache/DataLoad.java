package com.miismi.common.cache;

public interface DataLoad<T> {
    T load(String key);
}
