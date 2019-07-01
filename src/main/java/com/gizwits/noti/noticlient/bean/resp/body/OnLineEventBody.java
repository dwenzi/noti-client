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
public class OnLineEventBody extends AbstractPushEventBody {
    @JSONField(name = "group_id")
    private String groupId;

    @JSONField(name = "ip")
    private String ip;

    @JSONField(name = "country")
    private String country;

    @JSONField(name = "region")
    private String region;

    @JSONField(name = "city")
    private String city;

    /*至18_08_15， nb特有字段*/
    @JSONField(name = "longitude")
    private Double longitude;

    /*至18_08_15， nb特有字段*/
    @JSONField(name = "latitude")
    private Double latitude;

    /*至18_09_18， nb特有字段*/
    private String iccid;

    /*至18_09_18， nb特有字段*/
    private String imsi;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.DEVICE_ONLINE.getCode();
    }
}
