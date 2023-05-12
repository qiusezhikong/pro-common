package com.miismi.common;

import com.miismi.common.cache.BeeCacheData.*;
import com.miismi.common.cache.BeeCacheData;

import java.util.HashMap;
import java.util.Map;

public class BeeCacheInstance {
    //全局单例
    public static final BeeCacheData<Map<String,String>> TABLE =
            new BeeCacheData.Builder<Map<String,String>>("table")
                    .setTimeInterval(Time.DATE)
                    .setMergeType(MergeType.DISCARD)
                    .setExpirationTimeType(ExpirationTimeType.NaturalCycleInterval)
                    .setDataLoad(key -> {

                        //数据加载逻辑
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("key", key);
                        hashMap.put("time", System.currentTimeMillis() + "");

                        return hashMap;
                    }).build();

}
