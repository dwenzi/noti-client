package com.gizwits.noti.noticlient.bean.req.body;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.gizwits.noti.noticlient.bean.req.SourceCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内部控制请求指令
 * <p>
 * 内部控制请求指令与WiFi GPRS 控制指令请求体不同
 * 1. 支持 NB_IoT, 此时 source 必填{@link #setSource(SourceCategory)}
 * 2. 支持 LoRa, source 留空 {@link #setSource(SourceCategory)}
 *
 * @author Jcxcc
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public final class InternalControlReqCommandBody extends AbstractCommandBody implements InternalControlOrder {
    /**
     * 1. NB-IoT时:设备的来源，目前仅支持华为平台
     * 2. lora时留空
     */
    private SourceCategory source;

    /**
     * 可用于标识本消息，将会在回复指令中返回
     */
    @JSONField(name = "msg_id")
    private String msgId;

    @JSONField(name = "did")
    private String did;

    @JSONField(name = "mac")
    private String mac;

    @JSONField(name = "product_key")
    private String productKey;

    /**
     * "name1": <value1>,("name1"指数据点的标识名(name)，<value1>指数据点
     * 的值。值可以为 true/false(bool)，Unicode 编码的字符串如\u62bd(enum)，数字或 byte 数组
     * (如 [23,2,3]，用于扩展类型))
     */
    @JSONField(name = "attrs")
    private JSONObject attrs;

    public InternalControlReqCommandBody() {
        super();
    }

    public InternalControlReqCommandBody(String productKey, String mac, String did, JSONObject attrs) {
        this.did = did;
        this.mac = mac;
        this.productKey = productKey;
        this.attrs = attrs;
    }

    @Override
    String getJson() {
        return JSONObject.toJSONString(this, SerializerFeature.IgnoreNonFieldGetter, SerializerFeature.WriteEnumUsingName);
    }
}
