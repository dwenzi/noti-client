package com.gizwits.noti.noticlient.bean.resp.body;

import com.alibaba.fastjson.annotation.JSONField;
import com.gizwits.noti.noticlient.bean.resp.NotiRespPushEvents;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 设备LBS地理位置信息
 *
 * @author Jcxcc
 * @see <a href="http://docs.gizwits.com/zh-cn/Cloud/NotificationAPI.html#LBS%E6%95%B0%E6%8D%AE">LBS数据</a>
 * @since 1.9.0
 */
@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class LbsKvEventBody extends AbstractPushEventBody {

    @JSONField(name = "data")
    private LbsKvData data;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.LBS_KV.getCode();
    }

    @Data
    @NoArgsConstructor
    public static class LbsKvData {

        /**
         * 经度
         */
        private Float longitude;

        /**
         * 纬度
         */
        private Float latitude;

        /**
         * 国家
         */
        private String country;

        /**
         * 城市
         */
        private String city;

        /**
         * 区
         */
        private String district;

        /**
         * 邮编
         */
        private String adcode;

        /**
         * road
         */
        private String road;

        /**
         * street
         */
        private String street;

        /**
         * poi
         */
        private String poi;

        /**
         * imsi
         */
        private String imsi;

        /**
         * 信号
         */
        private Float signal;
    }
}
