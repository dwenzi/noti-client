package com.gizwits.noti.noticlient.bean.resp.body;

import com.alibaba.fastjson.annotation.JSONField;
import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * The type Abstract push event body.
 *
 * @author Jcxcc
 * @since 1.0
 */
@Setter
@Getter
@Accessors(chain = true)
public abstract class AbstractPushEventBody {

    /**
     * 固定为 event_push
     */
    @JSONField(name = "cmd")
    protected String cmd = NotiGeneralCommandType.event_push.getCode();

    /**
     * ACK
     */
    @JSONField(name = "delivery_id")
    protected String deliveryId;

    /**
     * The Event type.
     */
    @JSONField(name = "event_type")
    protected String eventType;

    /**
     * The Product key.
     */
    @JSONField(name = "product_key")
    protected String productKey;

    /**
     * The Did.
     */
    @JSONField(name = "did")
    protected String did;

    /**
     * The Mac.
     */
    @JSONField(name = "mac")
    protected String mac;

    /**
     * The Msg id.
     */
    @JSONField(name = "msg_id")
    protected String msgId;

    /**
     * The Created at.
     */
    @JSONField(name = "created_at")
    protected Long createdAt;

    /**
     * Push event string.
     *
     * @return the string
     */
    public abstract String pushEvent();
}
