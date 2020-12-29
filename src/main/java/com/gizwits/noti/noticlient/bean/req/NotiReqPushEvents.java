package com.gizwits.noti.noticlient.bean.req;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 登陆请求以及订阅请求的推送事件
 *
 * @author Jcxcc
 * @see <a href="http://docs.gizwits.com/zh-cn/Cloud/NotificationAPI.html#1-%E8%BF%9E%E6%8E%A5%E4%B8%8E%E7%99%BB%E9%99%86">连接与登陆</a>
 * @since 1.0.0
 */
@Getter
public enum NotiReqPushEvents {
    FAULT("device.attr_fault", "故障"),
    ALERT("device.attr_alert", "告警"),
    ONLINE("device.online", "上线"),
    OFFLINE("device.offline", "下线"),
    RAW("device.status.raw", "透传"),
    KV("device.status.kv", "数据点"),
    GPS_KV("device.gps.kv", "GPS地理位置信息"),
    LBS_KV("device.lbs.kv", "LBS地理位置信息"),
    BIND("device.bind", "设备绑定"),
    UNBIND("device.unbind", "设备解绑"),
    RESET("device.reset", "设备重置"),
    SUB_ADDED("center_control.sub_device_added", "中控子设备添加"),
    SUB_DELETED("center_control.sub_device_deleted", "中控子设备删除"),
    DATA_POINT_CHANGED("datapoints.changed", "产品数据点改变"),
    FILE_DOWNLOAD("device.file.download", "设备文件下载"),
    APP_2_DEV_KV("device.app2dev.kv", "app控制设备-数据点形式"),
    APP_2_DEV_RAW("device.app2dev.raw", "app控制设备-透传形式"),
    ;

    private final String code;
    private final String description;

    private final static Map<String, NotiReqPushEvents> CODE_2_EVENT_MAP;

    static {
        CODE_2_EVENT_MAP = Stream.of(NotiReqPushEvents.values())
                .collect(Collectors.toMap(NotiReqPushEvents::getCode, Function.identity(), (o, n) -> n));
    }

    NotiReqPushEvents(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static NotiReqPushEvents codeOf(String code) {
        Preconditions.checkArgument(StringUtils.isNotBlank(code),
                "The code can not be blank");
        return CODE_2_EVENT_MAP.getOrDefault(code, null);
    }
}
