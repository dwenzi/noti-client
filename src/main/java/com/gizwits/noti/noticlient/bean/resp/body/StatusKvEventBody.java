package com.gizwits.noti.noticlient.bean.resp.body;

import com.alibaba.fastjson.JSONObject;
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
public class StatusKvEventBody extends AbstractPushEventBody {
    @JSONField(name = "group_id")
    private String groupId;

    @JSONField(name = "data")
    private JSONObject data;

    /*至18_09_18， nb特有字段*/
    private String iccid;

    /*至18_09_18， nb特有字段*/
    private String imsi;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.DEVICE_STATUS_KV.getCode();
    }
}
