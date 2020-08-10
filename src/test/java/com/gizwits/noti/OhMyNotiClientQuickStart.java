package com.gizwits.noti;


import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.OhMyNotiClient;
import com.gizwits.noti.noticlient.OhMyNotiClientImpl;
import com.gizwits.noti.noticlient.bean.Credential;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import com.gizwits.noti.noticlient.config.SnotiConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 快速上手
 *
 * @author Jcxcc
 * @since 1.0
 */
@Slf4j
public class OhMyNotiClientQuickStart {

    public static void main(String[] args) throws InterruptedException {
        String
                did = "did",
                mac = "mac",
                subKey = "sub_key",
                authId = "auth_id",
                authSecret = "auth_secret",
                productKey = "product_key";

        //登陆信息
        Credential credential = Credential.builder()
                .subkey(subKey)
                .authId(authId)
                .authSecret(authSecret)
                .productKey(productKey)
                .build();

        //初始化客户端
        OhMyNotiClient client = new OhMyNotiClientImpl()
                //设置snoti回调, 默认回调见SnotiCallback#identity
                .setCallback(SnotiCallback.identity())
                //加载登陆信息
                .setCredentials(credential)
                //snoti配置, 初始化配置见SnotiConfig
                .setSnotiConfig(new SnotiConfig());

        //启动客户端
        client.doStart();

        //模拟消费信息
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            for (; ; ) {
                JSONObject jsonObject = client.receiveMessage();
                log.info(jsonObject.toJSONString());
            }
        });
        //模拟消费信息end

        //客户端登陆后才可以控制.
        TimeUnit.SECONDS.sleep(15);

        //透传控制
        Byte[] rawAttrs = new Byte[]{};
        client.control(productKey, mac, did, rawAttrs);
        //透传控制end

        //数据点控制
        Map<String, Object> jsonAttrs = new HashMap<>();
        jsonAttrs.put("data_point_code1", "data_point_value1");
        jsonAttrs.put("data_point_code2", "data_point_value2");
        jsonAttrs.put("data_point_code3", "data_point_value3");
        client.control(productKey, mac, did, jsonAttrs);
        //数据点控制end


        //重新加载登陆信息
        TimeUnit.SECONDS.sleep(30);
        client.setCredentials(credential);
        //重新加载登陆信息end

        //结束
        TimeUnit.SECONDS.sleep(30);
        client.doStop();
        //结束end
    }
}