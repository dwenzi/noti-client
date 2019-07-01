package com.gizwits.noti.noticlient.bean.resp.body;

import com.alibaba.fastjson.annotation.JSONField;
import com.gizwits.noti.noticlient.bean.resp.NotiRespPushEvents;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 设备大文件下载通知事件
 *
 * @author Jcxcc
 * @since 1.0
 */
@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class DownloadEventBody extends AbstractPushEventBody {

    @JSONField(name = "download_url")
    private String downloadUrl;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.DOWNLOAD.getCode();
    }
}
