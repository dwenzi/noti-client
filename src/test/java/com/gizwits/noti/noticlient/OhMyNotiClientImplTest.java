package com.gizwits.noti.noticlient;


import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.bean.req.NotiReqPushEvents;
import com.gizwits.noti.noticlient.bean.req.body.AuthorizationData;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import com.gizwits.noti.noticlient.config.SnotiConfig;
import com.gizwits.noti.noticlient.enums.ProtocolType;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Jcxcc
 * @since 1.0
 */
@Slf4j
public class OhMyNotiClientImplTest {

    public static void main(String[] args) throws InterruptedException {
        String
                did = "did",
                mac = "mac",
                subKey = "sub_key",
                authId = "auth_id",
                authSecret = "auth_secret",
                productKey = "product_key";

        //登陆信息
        AuthorizationData authorizationData = new AuthorizationData()
                //默认为 Wi-Fi_GPRS, 可选 NB_IoT, LORA. 此配置让client自动根据协议类型发送对应的请求体
                .setProtocolType(ProtocolType.WiFi_GPRS)
                .setAuth_id(authId)
                .setAuth_secret(authSecret)
                .setSubkey(subKey)
                //订阅事件, 建议按照需求订阅
                .addEvents(NotiReqPushEvents.values())
                .setProduct_key(productKey);

        //初始化客户端
        OhMyNotiClient client = new OhMyNotiClientImpl(executor)
                //设置snoti回调, 默认回调见SnotiCallback#identity
                .setCallback(SnotiCallback.identity())
                //加载登陆信息
                .addLoginAuthorizes(authorizationData)
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
        client.reload(authorizationData);
        //重新加载登陆信息end

        //结束
        TimeUnit.SECONDS.sleep(30);
        client.doStop();
        //结束end
    }
}