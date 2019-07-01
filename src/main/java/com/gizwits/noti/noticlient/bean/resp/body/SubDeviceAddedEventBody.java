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
public class SubDeviceAddedEventBody extends AbstractPushEventBody {
    @JSONField(name = "child_product_key")
    private String childProductKey;

    @JSONField(name = "child_did")
    private String childProductDid;

    @JSONField(name = "child_mac")
    private String childProductMac;

    /**
     * 其中child_passcode采用AES加密，
     * AES补码方式为pcks7padding
     * AES key为中控passcode的md5值（16 bytes）
     * AES mode为AES.MODE_ECB
     * <str, AES encrypted, see below>,
     */
    @JSONField(name = "child_passcode")
    private String childPasscode;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.SUB_DEVICE_ADDED.getCode();
    }
}
