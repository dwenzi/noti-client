package com.gizwits.noti.noticlient.bean.req;

import lombok.Getter;

/**
 * 设备来源 类别
 *
 * @author Jcxcc
 * @since 1.0
 */
@Getter
public enum SourceCategory {
    noti("默认source"),

    huawei("电信白色家电(私有)NB-IoT, 至18-05-04, 仅支持华为"),;

    private String description;

    SourceCategory(String description) {
        this.description = description;
    }

}
