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
    subscribe_req("subscribe_req", "", "订阅请求"),
    subscribe_res("subscribe_res", "", "订阅回复"),
    unsubscribe_req("unsubscribe_req", "", "取消订阅请求"),
    unsubscribe_res("unsubscribe_res", "", "取消订阅回复"),
    event_push("event_push", "", "事件推送"),
    event_ack("event_ack", "", "ack事件回复"),
    remote_control_v2_req("remote_control_v2_req", "", "远程控制设备请求v2版本, 统一了控制接口"),
    @Deprecated
    remote_control_res("remote_control_res", "", "远程控制设备回复"),
    remote_control_v2_res("remote_control_v2_res", "", "远程控制设备回复v2"),
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
