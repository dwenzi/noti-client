package com.gizwits.noti.noticlient.login;

import com.gizwits.noti.noticlient.NotiClientTest;
import com.gizwits.noti.noticlient.OhMyNotiClient;
import com.gizwits.noti.noticlient.OhMyNotiClientImpl;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import lombok.extern.slf4j.Slf4j;
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
                .setCredentials(getCredentials())
                .setCallback(callback);

        client.doStart();
        synchronized (loginLock) {
            loginLock.wait();
        }
    }
}
