package com.gizwits.noti.noticlient;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.bean.Credential;
import com.gizwits.noti.noticlient.bean.req.NotiCtrlDTO;
import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import com.gizwits.noti.noticlient.bean.resp.body.AbstractPushEventBody;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import com.gizwits.noti.noticlient.config.SnotiConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gizwits.noti.noticlient.bean.SnotiConstants.STR_DELIVERY_ID;

/**
 * Snoti客户端接口
 *
 * @author Jcxcc
 * @since 1.0
 */
public interface OhMyNotiClient {

    /**
     * markLoginState
     *
     * @param json
     */
    void markLoginState(JSONObject json);

    /**
     * Store information.
     *
     * @param jsonMessage the json message
     * @return the boolean
     */
    boolean storeInformation(JSONObject jsonMessage);

    /**
     * get credentials
     *
     * @return credentials
     */
    List<Credential> getCredentials();

    /**
     * set credentials
     *
     * @param credentials credentials
     * @return
     */
    OhMyNotiClient setCredentials(List<Credential> credentials);

    /**
     * 建立连接
     */
    void doConnect();

    /**
     * 开始工作
     */
    void doStart();

    /**
     * 停止工作
     */
    void doStop();

    /**
     * send msg
     *
     * @param msg
     */
    void sendMsg(Object msg);

    /**
     * 打开推送消息开关
     */
    void switchPushMessage();

    void disconnected();

    /**
     * 设置snoti配置信息
     *
     * @param snotiConfig the snoti config
     * @return the snoti config
     */
    OhMyNotiClient setSnotiConfig(SnotiConfig snotiConfig);

    /**
     * 设置snoti回调
     * <p>
     * 注意不要在回调信息中执行类似while(true)的代码
     *
     * @param callback the callback
     * @return the callback
     */
    OhMyNotiClient setCallback(SnotiCallback callback);

    /**
     * 获取snoti回调
     *
     * @return the callback
     */
    SnotiCallback getCallback();

    /**
     * 接受消息
     *
     * @return the json object
     */
    JSONObject receiveMessage();

    /**
     * 阻塞式发送控制指令
     * <p>
     * V4 产品自定义协议格式，填写 write
     * 当使用通用数据点透传指令时，Raw 指令以 0x05 开头
     *
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param raw        the raw
     * @return the boolean
     */
    default boolean control(String productKey, String mac, String did, Object raw) {
        return this.control(StringUtils.EMPTY, productKey, mac, did, raw);
    }

    /**
     * 阻塞式发送控制指令
     *
     * @param msgId      the msg id
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param data       the raw
     * @return the boolean
     */
    boolean control(String msgId, String productKey, String mac, String did, Object data);

    /**
     * Batch control boolean.
     *
     * @param msgId    the msg id
     * @param ctrlDTOs the ctrl dt os
     * @return the boolean
     */
    boolean batchControl(String msgId, NotiCtrlDTO... ctrlDTOs);

    /**
     * Batch control boolean.
     *
     * @param ctrlDTOs the ctrl dt os
     * @return the boolean
     */
    default boolean batchControl(NotiCtrlDTO... ctrlDTOs) {
        return this.batchControl(StringUtils.EMPTY, ctrlDTOs);
    }

    /**
     * Confirmation boolean.
     *
     * @param deliveryId the delivery id
     * @return the boolean
     */
    boolean confirm(String deliveryId);

    /**
     * Confirmation boolean.
     *
     * @param json the message
     * @return the boolean
     */
    default boolean confirm(JSONObject json) {
        String cmd = json.getString("cmd");
        boolean needAck = StringUtils.equals(cmd, NotiGeneralCommandType.event_push.getCode());
        if (needAck) {
            if (!json.containsKey(STR_DELIVERY_ID)) {
                LoggerFactory.getLogger(OhMyNotiClient.class).warn("消息不含delivery id, 确认消息失败. {}", json);
                return false;
            }

            return confirm(json.getString(STR_DELIVERY_ID));
        }

        return false;
    }

    /**
     * Confirmation boolean.
     *
     * @param <B>  the type parameter
     * @param body the body
     * @return the boolean
     */
    default <B extends AbstractPushEventBody> boolean confirm(B body) {
        String cmd = body.getCmd();
        boolean needAck = StringUtils.equals(cmd, NotiGeneralCommandType.event_push.getCode());
        String deliveryId = body.getDeliveryId();
        if (needAck) {
            if (StringUtils.isBlank(deliveryId)) {
                LoggerFactory.getLogger(OhMyNotiClient.class).warn("delivery id为空, 确认消息失败. {}", body);
                return false;
            }
            return confirm(deliveryId);
        }

        return false;
    }

    default OhMyNotiClient setCredentials(Credential credential) {
        List<Credential> credentials = Stream.concat(getCredentials().stream(), Stream.of(credential))
                .distinct().collect(Collectors.toList());
        return setCredentials(credentials);
    }
}
