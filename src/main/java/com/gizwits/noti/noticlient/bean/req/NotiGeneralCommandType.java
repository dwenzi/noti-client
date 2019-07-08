package com.gizwits.noti.noticlient.bean.req;

import lombok.Getter;

/**
 * @author Jcxcc
 * @since 1.0
 */
@Getter
public enum NotiGeneralCommandType {
    login_req("login_req", "", "登录请求"),
    login_res("login_res", "", "登录回复"),
    event_push("event_push", "", "事件推送"),
    event_ack("event_ack", "", "ack事件回复"),
    remote_control_req("remote_control_req", "", "远程控制设备请求v1版本"),
    remote_control_v2_req("remote_control_v2_req", "", "远程控制设备请求v2版本, 统一了控制接口"),
    remote_control_nb_req("remote_control_nb_req", "", "远程控制设备请求: NB-IoT"),
    remote_control_lora_req("remote_control_lora_req", "", "远程控制设备请求: lora"),
    remote_control_res("remote_control_res", "", "远程控制设备回复"),
    pong("pong", "{\"cmd\":\"pong\"}\n", "服务端发往客户端的心跳"),
    ping("ping", "{\"cmd\":\"ping\"}\n", "客户端发往服务端的心跳"),
    invalid_msg("invalid_msg", "", "无效信息");

    NotiGeneralCommandType(String code, String order, String description) {
        this.code = code;
        this.order = order;
        this.description = description;
    }

    private final String code;
    private final String order;
    private final String description;
}
