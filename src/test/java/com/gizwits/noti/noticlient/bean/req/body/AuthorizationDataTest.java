package com.gizwits.noti.noticlient.bean.req.body;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.NotiClientTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Jcxcc
 * @since 1.0.0
 */
public class AuthorizationDataTest extends NotiClientTest {

    @Test
    public void jsonSerialize() {
        AuthorizationData authorizationData = getAuthorizationData();
        JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(authorizationData));
        //服务端认证字段
        String[] fields = new String[]{"product_key", "auth_secret", "auth_id", "subkey", "events"};
        for (String field : fields) {
            assertTrue(String.format("The lack of called %s field", field), json.containsKey(field));
        }
    }
}