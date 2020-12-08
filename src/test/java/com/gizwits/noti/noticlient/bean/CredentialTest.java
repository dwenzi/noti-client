package com.gizwits.noti.noticlient.bean;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.bean.req.NotiReqPushEvents;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * @author Jcxcc
 * @since 1.0.0
 */
@Slf4j
public class CredentialTest {

    @Test
    public void builder() {
        Credential credential = Credential.builder()
                .authId("auth id")
                .authSecret("auth secret")
                .productKey("product key")
                .subkey("sub key")
                .build();

        assertTrue(Objects.nonNull(credential));
        log.info(JSONObject.toJSONString(credential));
    }

    @Test
    public void equal() {
        Credential c0 = Credential.builder()
                .authId("id")
                .authSecret("secret")
                .productKey("pk1")
                .subkey("dev")
                .build();

        Credential c1 = Credential.builder()
                .authId("id")
                .authSecret("secret")
                .events(Arrays.asList(NotiReqPushEvents.ONLINE, NotiReqPushEvents.OFFLINE, NotiReqPushEvents.KV))
                .productKey("pk1")
                .subkey("dev")
                .build();

        Credential c2 = Credential.builder()
                .events(Arrays.asList(NotiReqPushEvents.OFFLINE, NotiReqPushEvents.KV, NotiReqPushEvents.ONLINE))
                .authId("id")
                .authSecret("secret")
                .productKey("pk1")
                .subkey("dev")
                .build();

        Credential c3 = Credential.builder()
                .authId("id")
                .authSecret("secret")
                .productKey("pk2")
                .subkey("dev")
                .build();

        assertNotEquals(c0, c2);
        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
    }
}