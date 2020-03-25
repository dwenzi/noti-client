package com.gizwits.noti.noticlient.bean.resp;

/**
 * @author Jcxcc
 * @since 1.0
 */
public enum NotiRespPushEvents {
    DEVICE_ONLINE("device_online", "设备上线"),
    DEVICE_OFFLINE("device_offline", "设备下线"),
    ATTR_FAULT("attr_fault", "设备故障"),
    ATTR_ALERT("attr_alert", "设备告警"),
    DEVICE_STATUS_RAW("device_status_raw", "设备状态-透传"),
    DEVICE_STATUS_KV("device_status_kv", "设备状态-数据点"),
    SUB_DEVICE_ADDED("sub_device_added", "中控添加子设备"),
    SUB_DEVICE_DELETED("sub_device_deleted", "中控删除子设备"),
    BIND("device_bind", "用户绑定设备"),
    UNBIND("device_unbind", "用户解绑设备"),
    DATA_POINTS_CHANGED("datapoints_changed", "数据点改变"),
    RESET("device_reset", "设备重置"),
    DOWNLOAD("device_file_download", "设备大文件下载通知"),
    APP_TO_DEV_KV("app2dev_kv", "app控制设备-数据点"),
    APP_TO_DEV_RAW("app2dev_raw", "app控制设备-透传"),
    GPS_KV("device_gps_kv", "GPS地理位置信息"),
    LBS_KV("device_lbs_kv", "LBS地理位置信息"),
    //设备回调当作推送事件
    CONTROL_CALLBACK("remote_control_res", "设备控制回调"),
    V2_CONTROL_CALLBACK("remote_control_v2_res", "v2版本设备控制回调"),
    SUBSCRIBE_CALLBACK("subscribe_res", "订阅产品数据回调"),
    UNSUBSCRIBE_CALLBACK("unsubscribe_res", "取消订阅产品数据回调"),
    INVALID("invalid", "无效"),
    ;

    private String code;
    private String description;

    NotiRespPushEvents(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }
}
