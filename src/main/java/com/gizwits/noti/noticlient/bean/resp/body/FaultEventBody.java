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
public class FaultEventBody extends AbstractPushEventBody {
    /**
     * <uuid string>, (同一设备的同一故障或报警的发生事件与恢复事件共享同一事件 id)
     */
    @JSONField(name = "event_id")
    private String eventId;

    @JSONField(name = "data")
    private Data data;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.ATTR_FAULT.getCode();
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Data {
        /**
         * <故障或报警数据点标识名>,
         */
        @JSONField(name = "attr_name")
        private String attrName;

        /**
         * <故障或报警数据点显示名称>
         */
        @JSONField(name = "attr_displayname")
        private String attrDisplayName;

        /**
         * 0 | 1 (0 表示从故障恢复或报警取消，1 表示发生了故障或报警)
         */
        @JSONField(name = "value")
        private Integer value;
    }
}
