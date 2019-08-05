package com.gizwits.noti.noticlient.bean.req.body;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * v2版本控制
 * <p>
 * 接口特性
 * 1. 统一了 NB-IoT、LoRa、Wi-Fi、GPRS控制接口
 * 2. 数据点兼容了扩展数据点
 *
 * @author Jcxcc
 * @since 1.0
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public final class ControlV2ReqCommandBody extends ControlReqCommandBody {

    @Override
    String getJson() {
        //设置为v2控制
        this.cmd = NotiGeneralCommandType.remote_control_v2_req;
        return JSONObject.toJSONString(this, SerializerFeature.IgnoreNonFieldGetter, SerializerFeature.WriteEnumUsingName);
    }
}
