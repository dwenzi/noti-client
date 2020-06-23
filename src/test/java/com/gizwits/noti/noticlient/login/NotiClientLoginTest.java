package com.gizwits.noti.noticlient.login;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.NotiClientTest;
import com.gizwits.noti.noticlient.OhMyNotiClient;
import com.gizwits.noti.noticlient.OhMyNotiClientImpl;
import com.gizwits.noti.noticlient.bean.req.body.AuthorizationData;
import com.gizwits.noti.noticlient.bean.resp.body.SubscribeCallbackEventBody;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import com.gizwits.noti.noticlient.config.SnotiConfig;
import com.gizwits.noti.noticlient.util.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * noti login test
 *
 * @author Jcxcc
 * @since 1.0.0
 */
@Slf4j
public class NotiClientLoginTest extends NotiClientTest {

    private final Object loginLock = new Object();

    /**
     * noti 登陆
     * <p>
     * 普通登陆方式
     *
     * @throws InterruptedException
     */
    @Test
    public void login() throws InterruptedException {
        SnotiCallback callback = new SnotiCallback() {
            @Override
            public void loginFailed(String errorMessage) {
                log.warn("Login failed, please check the parameters. {}", errorMessage);
                synchronized (loginLock) {
                    loginLock.notifyAll();
                }
            }

            @Override
            public void loginSuccessful() {
                log.info("Login success.");
                synchronized (loginLock) {
                    loginLock.notifyAll();
                }
            }
        };

        OhMyNotiClient client = new OhMyNotiClientImpl()
                .setSnotiConfig(getSnotiConfig())
                .addLoginAuthorizes(getAuthorizationData())
                .setCallback(callback);

        client.doStart();

        synchronized (loginLock) {
            loginLock.wait();
        }
    }

    /**
     * 动态订阅方式
     * <p>
     * 1. 适用与订阅消息会频繁更新的情况
     *
     * @throws InterruptedException
     */
    @Test
    public void subscribe() throws InterruptedException {
        SnotiConfig snotiConfig = getSnotiConfig();
        AuthorizationData authorizationData = getAuthorizationData();

        SnotiCallback callback = new SnotiCallback() {
            @Override
            public void loginFailed(String errorMessage) {
                log.warn("Login failed, please check the parameters. {}", errorMessage);
                synchronized (loginLock) {
                    loginLock.notifyAll();
                }
            }

            @Override
            public void loginSuccessful() {
                log.info("Login success.");
                synchronized (loginLock) {
                    loginLock.notifyAll();
                }
            }
        };

        OhMyNotiClient client = new OhMyNotiClientImpl()
                .setSnotiConfig(snotiConfig)
                .setCallback(callback);

        client.doStart();

        synchronized (loginLock) {
            loginLock.wait();
        }

        client.subscribe(authorizationData);

        while (true) {
            JSONObject json = client.receiveMessage();
            boolean isSubscribeResp = StringUtils.equals("subscribe_res", json.getString("cmd"));
            if (isSubscribeResp) {
                log.info("subscribe resp. {}", json.toJSONString());
                SubscribeCallbackEventBody body = CommandUtils.parsePushEvent(json, SubscribeCallbackEventBody.class);
                log.info("subscribe result[{}] msg[{}] ", body.getResult(), body.getMsg());
                break;
            }
        }
    }
}
