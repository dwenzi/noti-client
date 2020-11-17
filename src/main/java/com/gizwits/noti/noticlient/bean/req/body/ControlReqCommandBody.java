package com.gizwits.noti.noticlient.bean.req.body;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import com.gizwits.noti.noticlient.bean.req.NotiReqControlType;
import com.gizwits.noti.noticlient.bean.req.SourceCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 控制接口
 * <p>
 * 特性
 * 1. 统一了 NB-IoT、LoRa、Wi-Fi、GPRS控制接口
 * 2. 数据点兼容了扩展数据点
 *
 * @author Jcxcc
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ControlReqCommandBody extends AbstractCommandBody {

    /**
     * 可用于标识本消息，将会在回复指令中返回
     */
    @JSONField(name = "msg_id")
    private String msgId;

    @JSONField(name = "data")
    private List<ControlBody> controlBody;

    @Data
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class ControlBody {
        /**
         * V4 产品数据点协议格式，填写write_attrs；V4 产品自定义协议格式，填写 write；V1 产品协议格式，填写 write_v1
         */
        private NotiReqControlType cmd;

        /**
         * 固定填写 noti
         */
        private SourceCategory source;

        @JSONField(name = "data")
        private Payload payload;

        public ControlBody() {
            this.source = SourceCategory.noti;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Accessors(chain = true)
        public static class Payload {

            @JSONField(name = "mac")
            private String mac;

            @JSONField(name = "did")
            private String did;

            @JSONField(name = "product_key")
            private String productKey;

            /**
             * V4 产品数据点协议格式，选择data.data.attrs；
             */
            private JSONObject attrs;

            /**
             * V4 产品自定义协议格式（参考通用数据点协议之透传业务指令），选择data.data.raw；
             * V1 产品协议格式，选择 data.data.raw
             */
            private int[] raw;
        }
    }


    @Override
    String getJson() {
        //设置为v2控制
        this.cmd = NotiGeneralCommandType.remote_control_v2_req;
        return JSONObject.toJSONString(this, SerializerFeature.IgnoreNonFieldGetter, SerializerFeature.WriteEnumUsingName);
    }
}
