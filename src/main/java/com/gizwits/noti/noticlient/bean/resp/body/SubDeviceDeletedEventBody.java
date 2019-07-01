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
public class SubDeviceDeletedEventBody extends AbstractPushEventBody {
    @JSONField(name = "child_product_key")
    private String childProductKey;

    @JSONField(name = "child_did")
    private String childProductDid;

    @JSONField(name = "child_mac")
    private String childProductMac;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.SUB_DEVICE_DELETED.getCode();
    }
}
