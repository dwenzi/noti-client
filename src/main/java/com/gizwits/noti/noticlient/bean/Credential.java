package com.gizwits.noti.noticlient.bean;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.gizwits.noti.noticlient.bean.req.NotiReqPushEvents;
import com.gizwits.noti.noticlient.enums.LoginState;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * snoti 凭证
 *
 * @author Jcxcc
 * @since 1.0.0
 */
@Setter
@Getter
@EqualsAndHashCode(of = {"productKey", "subkey", "authId", "authSecret", "events"})
public class Credential {

    /**
     * product key
     */
    @JSONField(name = "product_key")
    private String productKey;

    /**
     * sub key
     */
    @JSONField(name = "subkey")
    private String subkey;

    /**
     * auth id
     */
    @JSONField(name = "auth_id")
    private String authId;

    /**
     * auth secret
     */
    @JSONField(name = "auth_secret")
    private String authSecret;

    /**
     * events
     */
    @JSONField(name = "events")
    private List<String> events;

    @Setter
    @JSONField(serialize = false, deserialize = false)
    private LoginState loginState;

    @Builder
    private Credential(
            @NonNull String productKey,
            @NonNull String subkey,
            @NonNull String authId,
            @NonNull String authSecret,
            List<NotiReqPushEvents> events) {
        this.productKey = productKey;
        this.subkey = subkey;
        this.authId = authId;
        this.authSecret = authSecret;

        this.events = events != null
                ? events.stream().map(NotiReqPushEvents::getCode).sorted().collect(Collectors.toList())
                : Stream.of(NotiReqPushEvents.values()).map(NotiReqPushEvents::getCode).sorted().collect(Collectors.toList());
        this.loginState = LoginState.NOT_LOGGED;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
