package com.gizwits.noti.noticlient.ctrl;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.NotiClientTest;
import com.gizwits.noti.noticlient.OhMyNotiClient;
import com.gizwits.noti.noticlient.OhMyNotiClientImpl;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jcxcc
 * @since 1.0.0
 */
@Slf4j
public class NotiClientControlTest extends NotiClientTest {

    private OhMyNotiClient client;
    private final Object loginLock = new Object();


    private String productKey;
    private String mac;
    private String did;
    private boolean loginSuccessful = false;

    @Before
    public void startClient() {
        //请替换为自己的测试设备信息
        mac = "macmacmacmacmac";
        did = "diddiddiddiddid";
        productKey = getProperties().getProductKey();

        log.info("Client be initializing");
        SnotiCallback callback = new SnotiCallback() {
            @Override
            public void loginFailed(String errorMessage) {
                log.warn("Login failed, please check the parameters. {}", errorMessage);
                synchronized (loginLock) {
                    loginSuccessful = false;
                    loginLock.notifyAll();
                }
            }

            @Override
            public void loginSuccessful() {
                log.info("Login success.");
                synchronized (loginLock) {
                    loginSuccessful = true;
                    loginLock.notifyAll();
                }
            }
        };
        client = new OhMyNotiClientImpl()
                .setCallback(callback)
                .setSnotiConfig(getSnotiConfig())
                .setCredentials(getCredentials());

        client.doStart();
        log.info("Client start");
    }

    /**
     * 数据点控制
     * <p>
     * 适用于定义了数据点的产品
     */
    @Test
    @Ignore//服务端返回 invalid msg
    public void dataPointControl() throws InterruptedException {
        Map<String, Object> attrs = new HashMap<>();
        //布尔
        attrs.put("power_switch", true);
        //数值
        attrs.put("temperature", 36);
        /* 扩展类型
         *
         * 当数据点类型为扩展类型数据点时, value 为 hex string，不足数据点长度需在右补 '0'.
         * 示例:
         * 数据点名称为 "binary_1", 数据点长度为 4字节, 需要下发 1 如下.
         * 注意!!! hex string 两个字符表示一个字节所以需根据数据点长度x2
         */
        int length_binary_1 = 4;
        attrs.put("binary_1", StringUtils.rightPad(String.format("%02x", 1), length_binary_1 * 2, '0'));

        synchronized (loginLock) {
            loginLock.wait();
        }

        if (!loginSuccessful) {
            log.info("The client has not logged in");
            return;
        }

        client.control(productKey, mac, did, attrs);
        while (true) {
            JSONObject json = client.receiveMessage();
            boolean isCtrlResp = StringUtils.equals("remote_control_v2_res", json.getString("cmd"))
                    || StringUtils.equals("remote_control_res", json.getString("cmd"));
            if (isCtrlResp) {
                log.info("ctrl resp. {}", json.toJSONString());
                break;
            }
        }
    }

    /**
     * raw 控制
     * <p>
     * 即, 透传控制
     * 适用于没有定义数据点的产品
     */
    @Test
    @Ignore//服务端返回 invalid msg
    public void rawControl() throws InterruptedException {
        //下发内容为空
        Byte[] rawAttrs = new Byte[]{};

        synchronized (loginLock) {
            loginLock.wait();
        }

        if (!loginSuccessful) {
            log.info("The client has not logged in");
            return;
        }

        client.control(productKey, mac, did, rawAttrs);

        while (true) {
            JSONObject json = client.receiveMessage();
            boolean isCtrlResp = StringUtils.equals("remote_control_v2_res", json.getString("cmd"))
                    || StringUtils.equals("remote_control_res", json.getString("cmd"));
            if (isCtrlResp) {
                log.info("ctrl resp. {}", json.toJSONString());
                break;
            }
        }
    }

    @After
    public void after() {
        log.info("noti client ctrl test end.");
        loginSuccessful = false;
    }
}
