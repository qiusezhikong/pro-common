package com.miismi.common.example;

import com.miismi.common.cache.BeeCacheUtil;
import com.miismi.common.cache.OnLoadListener;

import java.util.Map;

public class ExampleMain implements OnLoadListener {

    public static void main(String[] args) throws InterruptedException {
        //1.初始化数据加载逻辑
        BeeCacheUtil.add(DataLoad.class);

        //2.获取数据
        for (int i = 0; i < 10; i++) {

            System.out.printf("第%d次获取数据%n", i + 1);
            //使用全局单例获取数据
            Map<String, String> data = BeeCacheUtil.getData("KEY");
            System.out.println(data);

            Thread.sleep(3000);
        }
    }
}
