package com.gizwits.noti.noticlient;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.bean.req.body.AuthorizationData;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import com.gizwits.noti.noticlient.config.SnotiConfig;
import com.gizwits.noti.noticlient.enums.LoginState;

import java.util.Map;

/**
 * The interface Oh my noti client.
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
     * Gets login order.
     *
     * @return the login order
     */
    String getLoginOrder();

    /**
     * Do start.
     */
    void doStart();

    /**
     * Reload oh my noti client.
     *
     * @param authorizes the authorizes
     * @return the oh my noti client
     */
    OhMyNotiClient reload(AuthorizationData... authorizes);

    /**
     * Do connect.
     */
    void doConnect();

    /**
     * Do stop.
     */
    void doStop();

    /**
     * Switch push message.
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
     * 设置端口
     * <p>
     * 在以后的版本中会丢弃
     * 建议通过 snotiConfig 来设置 port. {@link SnotiConfig#setPort(Integer)}
     *
     * @param port the port
     * @return the port
     * @see #setSnotiConfig(SnotiConfig) #setSnotiConfig(SnotiConfig)#setSnotiConfig(SnotiConfig)
     */
    @Deprecated
    OhMyNotiClient setPort(int port);

    /**
     * 设置主机
     * <p>
     * 在以后的版本中会丢弃
     * 建议通过 snotiConfig 来设置host. {@link SnotiConfig#setHost(String)}
     *
     * @param host the host
     * @return the host
     * @see #setSnotiConfig(SnotiConfig) #setSnotiConfig(SnotiConfig)#setSnotiConfig(SnotiConfig)
     */
    @Deprecated
    OhMyNotiClient setHost(String host);

    /**
     * Sets callback.
     *
     * @param callback the callback
     * @return the callback
     */
    OhMyNotiClient setCallback(SnotiCallback callback);

    /**
     * Gets callback.
     *
     * @return the callback
     */
    SnotiCallback getCallback();

    /**
     * Add login authorizes oh my noti client.
     *
     * @param authorizes the authorizes
     * @return the oh my noti client
     */
    OhMyNotiClient addLoginAuthorizes(AuthorizationData... authorizes);

    /**
     * Receive message json object.
     *
     * @return the json object
     */
    JSONObject receiveMessage();

    /**
     * V4 产品自定义协议格式，填写 write
     * 当使用通用数据点透传指令时，Raw 指令以 0x05 开头
     *
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param raw        the raw
     * @return the boolean
     */
    boolean control(String productKey, String mac, String did, Object raw);

    /**
     * V4 产品数据点协议格式，填写write_attrs
     *
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param dataPoint  the data point
     * @return the boolean
     */
    boolean control(String productKey, String mac, String did, Map<String, Object> dataPoint);

    /**
     * Control boolean.
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
