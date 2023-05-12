package com.miismi.common;

import java.util.Map;

public class TestMain {
    public static void main(String[] args) {
        //通过全局单例获取数据
        Map<String, String> data = BeeCacheInstance.TABLE.getData();
        System.out.println(data);
    }
}
