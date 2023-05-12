package com.miismi.common;


import java.util.Map;

public class TestMain {

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 10; i++) {

            System.out.printf("第%d次获取数据%n", i + 1);
            //使用全局单例获取数据
            Map<String, String> data = BeeCacheInstance.TABLE.getData();
            System.out.println(data);

            Thread.sleep(3000);
        }
    }
}