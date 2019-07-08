package com.gizwits.noti.noticlient.handler;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.OhMyNotiClient;
import com.gizwits.noti.noticlient.OhMyNotiClientImpl;
import com.gizwits.noti.noticlient.bean.req.NotiReqCommandType;
import com.gizwits.noti.noticlient.enums.LoginState;
import com.gizwits.noti.noticlient.util.CommandUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * The type Client handler.
 *
 * @author Jcxcc
 * @since 1.0
 */
@Slf4j
public class SnotiChannelHandler extends SimpleChannelInboundHandler<String> {

    private OhMyNotiClient ohMyNotiClient;

    /**
     * Instantiates a new Client handler.
     *
     * @param client the client
     */
    public SnotiChannelHandler(OhMyNotiClientImpl client) {
        this.ohMyNotiClient = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        if (StringUtils.isBlank(message)) {
            log.warn("client received a blank message");

        } else {

            if (log.isDebugEnabled()) {
                log.debug("snoti客户端接收到消息: {}", message);
            }

            JSONObject jsonObject = JSONObject.parseObject(message);
            String cmd = StringUtils.defaultString(jsonObject.getString("cmd"), NotiReqCommandType.invalid_msg.getCode());
            NotiReqCommandType notiReqCommandType = CommandUtils.getReqCmd(cmd);

            switch (notiReqCommandType) {
                case event_push:
                    boolean storeEventSuccessful = ohMyNotiClient.storeInformation(jsonObject);
                    //存储信息成功
                    if (storeEventSuccessful) {
                        //消费端成功获取到消息才回复ack
                    } else {
                        log.error("存储消息失败. 消息[{}]", jsonObject.toJSONString());
                    }

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
                        ohMyNotiClient.setLoginState(LoginState.LOGIN_SUCCESSFUL);
                        ohMyNotiClient.getCallback().loginSuccessful();
                        //登陆成功后才允许推送信息
                        ohMyNotiClient.switchPushMessage();
                    } else {
                        log.warn("snoti客户端登录失败...");
                        String errorMessage = Optional.of(jsonObject)
                                .map(json -> json.getJSONObject("data"))
                                .map(json -> json.getString("msg"))
                                .orElse("登录失败");
                        ohMyNotiClient.getCallback().loginFailed(errorMessage);
                        ohMyNotiClient.setLoginState(LoginState.LOGIN_FAILED);
                    }
                    break;

                case pong:
                    log.info("接收到服务器pong响应...");
                    break;


                case remote_control_res:
                    boolean storeControlRespSuccessful = ohMyNotiClient.storeInformation(jsonObject);
                    //存储信息成功
                    if (storeControlRespSuccessful) {
                        //消费端成功获取到消息才回复ack
                    } else {
                        log.error("存储消息失败. 消息[{}]", jsonObject.toJSONString());
                    }

                case invalid_msg:
                default:
                    log.info("无效消息:[{}]", message);
                    break;

            }

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
        log.debug("连接激活...");

        String loginOrder = this.ohMyNotiClient.getLoginOrder();
        ctx.writeAndFlush(loginOrder).addListener(future -> {
            if (future.isSuccess()) {
                log.info("发送登录指令成功. 登录指令[{}]", loginOrder);

            } else {
                log.warn("发送登录指令失败, 关闭连接以触发重连. 登录指令[{}]", loginOrder);
                ctx.channel().close();
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
        this.ohMyNotiClient.getCallback().disconnected();

        this.ohMyNotiClient.setLoginState(LoginState.NOT_LOGGED);

        this.ohMyNotiClient.doConnect();
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
            switch (event.state()) {
                case WRITER_IDLE:
                    log.debug("No data was sent for a while.");
                    log.info("发送ping请求到服务器...");

                    ctx.writeAndFlush(NotiReqCommandType.ping.getOrder());
                    break;

                default:
                    break;
            }
        }

    }
}
