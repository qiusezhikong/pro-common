package com.miismi.common.example;

import com.miismi.common.cache.CacheParam;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 数据加载逻辑和配置
 */
public class DataLoad {

    @CacheParam(key = "KEY", timeUnit = TimeUnit.SECONDS, duration = 10)
    public HashMap<String, String> load1() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        System.out.println(format.format(new Date()) + "    数据开始加载...");

        //数据加载逻辑
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("key", "KEY");
        hashMap.put("data", "data");
        hashMap.put("time", System.currentTimeMillis() + "");

        System.out.println(format.format(new Date()) + "    加载完成!");

        return hashMap;
    }
}
