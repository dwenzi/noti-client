package com.gizwits.noti.noticlient.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

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
}