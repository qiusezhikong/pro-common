package com.miismi.common.cache;

public interface Cache {
    Object getData();

    boolean isExpired();
}
