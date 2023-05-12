package com.miismi.common;

import com.miismi.common.cache.BeeCacheData.*;
import com.miismi.common.cache.BeeCacheData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BeeCacheInstance {
    //全局单例
    public static final BeeCacheData<Map<String,String>> TABLE =
            new Builder<Map<String,String>>("table")
                    .setTimeInterval(Time.SECOND * 10)                              //数据有效时间10s
                    .setMergeType(MergeType.DISCARD)                                //key冲突时丢弃
                    .setExpirationTimeType(ExpirationTimeType.NaturalCycleInterval) //自然时间间隔
                    .setDataLoad(key -> {

                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        System.out.println(format.format(new Date()) + "    数据开始加载...");

                        //数据加载逻辑
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("key", key);
                        hashMap.put("data", "data");
                        hashMap.put("time", System.currentTimeMillis() + "");

                        System.out.println(format.format(new Date()) + "    加载完成!");

                        return hashMap;
                    }).build();

}
