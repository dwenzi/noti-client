package com.gizwits.noti.noticlient.bean.resp.body;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.gizwits.noti.noticlient.bean.resp.NotiRespPushEvents;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Jcxcc
 * @since 1.0
 */
@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class App2DevKvEventBody extends AbstractPushEventBody {
    /**
     * "client"|"open_api"|"scheduler"|"enterprise_api"|"gateway"
     */
    private String source;

    /**
     * missing if source="enterprise_api"|"gateway" or v1.0 devices
     */
    @JSONField(name = "appid")
    private String appId;

    /**
     * missing if source="enterprise_api"|"gateway" or v1.0 devices
     */
    private String uid;

    /**
     * <enterprise_id string>, (only for source="enterprise_api")
     */
    @JSONField(name = "enterprise_id")
    private String enterpriseId;

    private JSONObject data;

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.APP_TO_DEV_KV.getCode();
    }
}
