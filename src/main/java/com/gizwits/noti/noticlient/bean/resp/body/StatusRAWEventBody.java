package com.gizwits.noti.noticlient.bean.resp.body;

import com.alibaba.fastjson.annotation.JSONField;
import com.gizwits.noti.noticlient.bean.resp.NotiRespPushEvents;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Jcxcc
 * @since 1.0
 */
@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class StatusRAWEventBody extends AbstractPushEventBody {
    @JSONField(name = "group_id")
    private String groupId;

    /**
     * <base64 encoding string> (设备状态原始数据 base64 编码字符串)
     */
    @JSONField(name = "data")
    private String data;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.DEVICE_STATUS_RAW.getCode();
    }
}
