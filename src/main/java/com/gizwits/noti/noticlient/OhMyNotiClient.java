package com.gizwits.noti.noticlient;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.bean.req.body.AuthorizationData;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import com.gizwits.noti.noticlient.config.SnotiConfig;
import com.gizwits.noti.noticlient.enums.LoginState;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Snoti客户端接口
 *
 * @author Jcxcc
 * @since 1.0
 */
public interface OhMyNotiClient {

    /**
     * Sets login state.
     *
     * @param loginState the login state
     */
    void setLoginState(LoginState loginState);

    /**
     * Store information.
     *
     * @param jsonMessage the json message
     * @return the boolean
     */
    boolean storeInformation(JSONObject jsonMessage);

    /**
     * 添加登录信息
     *
     * @param authorizes the authorizes
     * @return the oh my noti client
     */
    OhMyNotiClient addLoginAuthorizes(AuthorizationData... authorizes);

    /**
     * 获取登录指令
     *
     * @return the login order
     */
    String getLoginOrder();

    /**
     * 建立连接
     */
    void doConnect();

    /**
     * 重新加载登录信息
     *
     * @param authorizes the authorizes
     * @return the oh my noti client
     */
    OhMyNotiClient reload(AuthorizationData... authorizes);

    /**
     * 开始工作
     */
    void doStart();

    /**
     * 停止工作
     */
    void doStop();

    /**
     * 打开推送消息开关
     */
    void switchPushMessage();

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
     * V4 产品数据点协议格式，填写write_attrs
     *
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param dataPoint  the data point
     * @return the boolean
     */
    default boolean control(String productKey, String mac, String did, Map<String, Object> dataPoint) {
        //设置msgId为空, 则会自动生成msgId
        return this.control(StringUtils.EMPTY, productKey, mac, did, dataPoint);
    }

    /**
     * 阻塞式发送控制指令
     *
     * @param msgId      the msg id
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param raw        the raw
     * @return the boolean
     */
    boolean control(String msgId, String productKey, String mac, String did, Object raw);

    /**
     * 阻塞式发送控制指令
     *
     * @param msgId      the msg id
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param dataPoint  the data point
     * @return the boolean
     */
    boolean control(String msgId, String productKey, String mac, String did, Map<String, Object> dataPoint);

    /**
     * 尝试发送透传控制
     * <p>
     * 该方法会尝试发送控制指令,
     * 客户端当前满足发送指令的必备条件时会快速返成功, 否则快速返回失败
     *
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param raw        the raw
     * @return the boolean
     */
    boolean tryControl(String productKey, String mac, String did, Object raw);

    /**
     * 尝试发送数据点控制
     * <p>
     * 该方法会尝试发送控制指令,
     * 客户端当前满足发送指令的必备条件时会快速返成功, 否则快速返回失败
     *
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param dataPoint  the data point
     * @return the boolean
     */
    boolean tryControl(String productKey, String mac, String did, Map<String, Object> dataPoint);
}
