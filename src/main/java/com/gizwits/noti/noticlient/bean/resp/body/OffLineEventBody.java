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
public class OffLineEventBody extends AbstractPushEventBody {
    @JSONField(name = "group_id")
    private String groupId;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.DEVICE_OFFLINE.getCode();
    }
}
