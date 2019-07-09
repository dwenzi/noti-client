package com.gizwits.noti.noticlient.bean.req.body;

import com.gizwits.noti.noticlient.bean.req.NotiReqPushEvents;
import com.gizwits.noti.noticlient.enums.ProtocolType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jcxcc
 * @since 1.0
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"protocolType", "events"})
@Accessors(chain = true)
public final class AuthorizationData {

    /**
     * 协议类型
     * <p>
     * 不属于登录请求参数
     * 用于控制设备时候的自动构造控制指令
     */
    private ProtocolType protocolType;
    private String product_key;
    private String auth_id;
    private String auth_secret;
    private String subkey;
    private List<String> events;

    public AuthorizationData() {
        this.events = new ArrayList<>();
        this.protocolType = ProtocolType.WiFi_GPRS;
    }

    /**
     * 添加事件
     *
     * @param notiReqPushEvents
     * @return
     */
    public AuthorizationData addEvents(NotiReqPushEvents... notiReqPushEvents) {
        Arrays.stream(notiReqPushEvents)
                .map(NotiReqPushEvents::getCode)
                .forEach(events::add);

        setEvents(getEvents().stream().distinct().collect(Collectors.toList()));

        return this;
    }
}
