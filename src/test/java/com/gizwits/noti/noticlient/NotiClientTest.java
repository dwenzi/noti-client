package com.gizwits.noti.noticlient;

import com.gizwits.noti.noticlient.bean.Credential;
import com.gizwits.noti.noticlient.bean.req.NotiReqPushEvents;
import com.gizwits.noti.noticlient.config.SnotiConfig;
import lombok.Data;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Jcxcc
 * @since 1.0.0
 */
public abstract class NotiClientTest {

    @Getter
    private NotiProperties properties;

    public NotiClientTest() {
        readProperties();
    }

    private void readProperties() {
        Properties props = new Properties();
        try (InputStream in = NotiClientTest.class.getResourceAsStream("/noti.properties")) {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        properties = new NotiProperties();
        properties.setProductKey(Objects.requireNonNull(props.getProperty("productKey"), "product key can not be null!"));
        properties.setAuthId(Objects.requireNonNull(props.getProperty("authId"), "auth id can not be null!"));
        properties.setAuthSecret(Objects.requireNonNull(props.getProperty("authSecret"), "auth secret can not be null!"));
        properties.setSubkey(Objects.requireNonNull(props.getProperty("subkey"), "sub key can not be null!"));
        properties.setNotiServerHost(Objects.requireNonNull(props.getProperty("notiServerHost"), "noti server host can not be null!"));
        properties.setNotiServerPort(Integer.valueOf(Objects.requireNonNull(props.getProperty("notiServerPort"), "noti server port can not be null!")));
    }

    protected SnotiConfig getSnotiConfig() {
        return new SnotiConfig()
                .setHost(getProperties().getNotiServerHost())
                .setPort(getProperties().getNotiServerPort());
    }

    protected List<Credential> getCredentials() {
        return Collections.singletonList(Credential.builder()
                .subkey(getProperties().getSubkey())
                .authId(getProperties().getAuthId())
                .authSecret(getProperties().getAuthSecret())
                .productKey(getProperties().getProductKey())
                //此处订阅所有推送事件, 可按需修改
                .events(Arrays.asList(NotiReqPushEvents.values()))
                .build());
    }

    @Data
    public class NotiProperties {

        private String productKey;

        private String authId;

        private String authSecret;

        private String subkey;

        private String notiServerHost;

        private Integer notiServerPort;
    }
}
