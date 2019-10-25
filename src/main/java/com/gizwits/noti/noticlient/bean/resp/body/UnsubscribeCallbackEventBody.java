package com.gizwits.noti.noticlient.bean.resp.body;

import com.alibaba.fastjson.annotation.JSONField;
import com.gizwits.noti.noticlient.bean.resp.NotiRespPushEvents;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 取消订阅回调
 *
 * @author Jcxcc
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UnsubscribeCallbackEventBody extends AbstractPushEventBody {
    private Boolean result;
    private String msg;
    private List<DataBean> data;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.UNSUBSCRIBE_CALLBACK.getCode();
    }

    @Data
    public static class DataBean {
        @JSONField(name = "product_key")
        private String productKey;
        private String subkey;
        private String auth_id;
        private String auth_secret;
        private List<String> events;
    }
}
