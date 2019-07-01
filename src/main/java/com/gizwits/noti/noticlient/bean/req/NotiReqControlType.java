package com.gizwits.noti.noticlient.bean.req;

import lombok.Getter;

/**
 * noti控制设备请求类型
 *
 * @author Jcxcc
 * @see <a href="http://docs.gizwits.com/zh-cn/Cloud/NotificationAPI.html#3-%E6%8E%A7%E5%88%B6%E8%AE%BE%E5%A4%87">设备控制</a>
 * @since 1.0
 */
@Getter
public enum NotiReqControlType {
    write("write", "透传"),
    write_v1("write_v1", "透传"),
    write_attrs("write_attrs", "数据点"),
    ;

    private String code;
    private String description;

    NotiReqControlType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
