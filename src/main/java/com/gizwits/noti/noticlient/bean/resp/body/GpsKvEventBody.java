package com.gizwits.noti.noticlient.bean.resp.body;

import com.alibaba.fastjson.annotation.JSONField;
import com.gizwits.noti.noticlient.bean.resp.NotiRespPushEvents;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 设备GPS地理位置信息
 *
 * @author Jcxcc
 * @since 1.8.8
 */
@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class GpsKvEventBody extends AbstractPushEventBody {

    @JSONField(name = "data")
    private GpsKvData data;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.GPS_KV.getCode();
    }

    @Data
    @NoArgsConstructor
    public static class GpsKvData {

        /**
         * 经度
         */
        private Float longitude;

        /**
         * 纬度
         */
        private Float latitude;

        /**
         * 水平分量精度因子:
         * （horizontal dilution of precision ）
         * 爲緯度和經度等誤差平方和的開根號值。
         */
        @JSONField(name = "hdop")
        private Float hdop;

        /**
         * 卫星个数
         */
        @JSONField(name = "num_satellites")
        private Integer numSatellites;
    }
}
