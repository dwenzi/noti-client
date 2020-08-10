package com.gizwits.noti.noticlient.handler;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.OhMyNotiClient;
import com.gizwits.noti.noticlient.OhMyNotiClientImpl;
import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import com.gizwits.noti.noticlient.bean.req.body.AbstractCommandBody;
import com.gizwits.noti.noticlient.bean.req.body.LoginReqCommandBody;
import com.gizwits.noti.noticlient.bean.req.body.SubscribeReqCommandBody;
import com.gizwits.noti.noticlient.enums.LoginState;
import com.gizwits.noti.noticlient.util.CommandUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Client handler.
 *
 * @author Jcxcc
 * @since 1.0
 */
@Slf4j
public class SnotiChannelHandler extends SimpleChannelInboundHandler<String> {

    private OhMyNotiClient client;

    /**
     * Instantiates a new Client handler.
     *
     * @param client the client
     */
    public SnotiChannelHandler(OhMyNotiClientImpl client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        if (StringUtils.isBlank(message)) {
            log.warn("snoti客户端接收到空消息.");

        } else {

            if (log.isDebugEnabled()) {
                //log为debug级别时 输出接收消息
                log.debug("snoti客户端接收到消息: {}", message);
            }

            JSONObject jsonObject = JSONObject.parseObject(message);
            String cmd = StringUtils.defaultString(jsonObject.getString("cmd"), NotiGeneralCommandType.invalid_msg.getCode());
            NotiGeneralCommandType notiGeneralCommandType = CommandUtils.getReqCmd(cmd);

            switch (notiGeneralCommandType) {
                case event_push:
                    storeMsg(jsonObject);
                    //交给下一个handler处理
                    ctx.fireChannelRead(message);
                    break;

                case login_res:
                    Boolean loginResult = Optional.of(jsonObject)
                            .map(json -> json.getJSONObject("data"))
                            .map(json -> json.getBooleanValue("result"))
                            .orElse(false);
                    if (loginResult) {
                        log.debug("snoti客户端登录成功...");
                        client.getCallback().loginSuccessful();
                        //登陆成功后才允许推送信息
                        client.switchPushMessage();

                        //登陆成功, 开始准备订阅信息
                        this.client.getCredentials().stream()
                                .map(SubscribeReqCommandBody::new)
                                .map(AbstractCommandBody::getOrder)
                                .forEach(client::sendMsg);
                    } else {
                        log.warn("snoti客户端登录失败...");
                        String errorMessage = Optional.of(jsonObject)
                                .map(json -> json.getJSONObject("data"))
                                .map(json -> json.getString("msg"))
                                .orElse("登录失败");
                        client.getCallback().loginFailed(errorMessage);
                    }
                    break;

                case pong:
                    log.info("接收到服务器pong响应...");
                    break;

                case subscribe_res:
                    client.markLoginState(jsonObject);
                    storeMsg(jsonObject);
                    break;

                case unsubscribe_res:
                case remote_control_res:
                case remote_control_v2_res:
                    storeMsg(jsonObject);
                    break;

                case invalid_msg:
                default:
                    log.info("无效消息:[{}]", message);
                    break;
            }
        }
    }

    private void storeMsg(JSONObject jsonObject) {
        boolean storeControlRespSuccessful = client.storeInformation(jsonObject);
        //存储信息成功
        if (storeControlRespSuccessful) {
            if (log.isDebugEnabled()) {
                log.info("存储消息成功. 消息{}", jsonObject.toJSONString());
            }

        } else {
            log.warn("存储消息失败. 消息{}", jsonObject.toJSONString());
        }
    }

    /**
     * 连接激活后发起登录请求
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.debug("连接激活即将发起登陆信息...");

        //智能登陆
        log.info("即将智能登陆...");
        doSmartLogin(ctx);
    }

    private void doSmartLogin(ChannelHandlerContext ctx) {
        //首先发送空的登陆信息，然后接收到登陆成功回调后再发起订阅
        LoginReqCommandBody emptyCommandBody = new LoginReqCommandBody();
        emptyCommandBody.setPrefetchCount(emptyCommandBody.getPrefetchCount());
        String order = emptyCommandBody.getOrder();
        ctx.writeAndFlush(order).addListener(future -> {
            if (!future.isSuccess()) {
                log.warn("发送空登陆指令失败, 关闭连接以触发重连. 登陆指令 {}", order);
                ctx.channel().close();
            } else {
                log.info("发送空登陆指令成功. 登陆指令 {}", order);
            }
        });
    }

    /**
     * 断开重连
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("连接断开, 即将开始重连...");
        super.channelInactive(ctx);

        log.debug("触发连接断开回调...");
        this.client.getCallback().disconnected();

        this.client.setCredentials(this.client.getCredentials().stream().peek(it -> it.setLoginState(LoginState.NOT_LOGGED)).collect(Collectors.toList()));

        this.client.doConnect();
    }

    /**
     * 心跳维持
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                log.info("发送ping请求到服务器...");
                ctx.writeAndFlush(NotiGeneralCommandType.ping.getOrder()).addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("发送 ping 请求到服务器成功.");
                    } else {
                        log.warn("发送 ping 请求到服务器失败.");
                    }
                });
            }
        }

    }
}
