package com.gizwits.noti.noticlient;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.bean.req.body.AbstractCommandBody;
import com.gizwits.noti.noticlient.bean.req.body.AuthorizationData;
import com.gizwits.noti.noticlient.bean.req.body.LoginReqCommandBody;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import com.gizwits.noti.noticlient.config.SnotiConfig;
import com.gizwits.noti.noticlient.config.SnotiTrustManager;
import com.gizwits.noti.noticlient.enums.LoginState;
import com.gizwits.noti.noticlient.enums.ProtocolType;
import com.gizwits.noti.noticlient.handler.NoDataChannelHandler;
import com.gizwits.noti.noticlient.handler.PushEventMessageCountingHandler;
import com.gizwits.noti.noticlient.handler.SnotiChannelHandler;
import com.gizwits.noti.noticlient.handler.SnotiMetricsHandler;
import com.gizwits.noti.noticlient.util.CommandUtils;
import com.gizwits.noti.noticlient.util.ControlUtils;
import com.gizwits.noti.noticlient.util.UniqueArrayBlockingQueue;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.gizwits.noti.noticlient.bean.SnotiConstants.STR_DELIVERY_ID;

/**
 * Snoti客户端实现
 *
 * @author Jcxcc
 * @since 1.0
 */
public class OhMyNotiClientImpl extends AbstractSnotiClient implements OhMyNotiClient {

    private BlockingQueue<JSONObject> receiveQueue;
    private BlockingQueue<String> ackReplyQueue;
    private BlockingQueue<String> controlQueue;

    private Channel channel;
    private Bootstrap bootstrap;
    private LoginState loginState = LoginState.NOT_LOGGED;

    private LoginReqCommandBody loginCommand;
    private Map<String, ProtocolType> productKeyProtocolMap;

    private SnotiCallback callback;

    private static final long DEFAULT_POLL_TIMEOUT_MS = 2;

    public OhMyNotiClientImpl() {
        super();
    }

    @Override
    public synchronized void switchPushMessage() {
        log.info("推送开关打开...");

        bootstrap.config().group().execute(() -> {
            while (true) {

                if (!Objects.equals(loginState, LoginState.LOGIN_SUCCESSFUL)) {
                    //连接断开时登录状态初始化, 回复线程退出
                    log.warn("非登录成功状态, 退出回复线程. 当前客户端状态[{}]", loginState);
                    break;
                }

                if (channel.isWritable()) {

                    try {
                        String ackMessage = this.ackReplyQueue.poll(DEFAULT_POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                        if (Objects.nonNull(ackMessage)) {
                            channel.writeAndFlush(ackMessage).addListener(future -> {
                                if (!future.isSuccess()) {
                                    log.warn("回复ack失败, 即将返回ack回复队列重试. [{}]", ackMessage);
                                    ackReplyQueue.put(ackMessage);
                                    log.info("重新放入ack回复队列成功. [{}]", ackMessage);
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug("回复ack成功. [{}]", ackMessage);
                                    }
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        log.error("回复ack消息失败. " + e.getMessage());
                        e.printStackTrace();
                    }

                    if (this.controlQueue.size() != 0) {
                        try {
                            String controlOrder = this.controlQueue.poll(DEFAULT_POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                            if (Objects.nonNull(controlOrder)) {
                                channel.writeAndFlush(controlOrder).addListener(future -> {
                                    if (!future.isSuccess()) {
                                        log.warn("下发控制指令失败. [{}]", controlOrder);
                                        controlQueue.put(controlOrder);
                                        log.info("重新放入控制队列成功. [{}]", controlOrder);
                                    } else {
                                        if (log.isDebugEnabled()) {
                                            log.debug("下发控制指令成功. [{}]", controlOrder);
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            log.error("下发控制指令失败. " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                } else if (ackReplyQueue.remainingCapacity() == 0) {
                    channel.flush();
                    log.warn("ack队列已满, 尝试flush");
                } else {
                    log.warn("连接繁忙, 不可写.");
                    try {
                        TimeUnit.MILLISECONDS.sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    @Override
    public JSONObject receiveMessage() {
        try {
            JSONObject json = receiveQueue.take();

            String ackMessage = CommandUtils.getEventAckMessage(json.get(STR_DELIVERY_ID));
            sendAckMessage(ackMessage);
            return json;

        } catch (InterruptedException e) {
            log.warn("获取消息失败, 返回空的json.");
            return new JSONObject();
        }

    }

    private void sendAckMessage(String order) {
        try {
            ackReplyQueue.put(order);
        } catch (Exception e) {
            log.warn("ackMessage入队失败. ackMessage[{}] errorMsg[{}]", order, e.getMessage());
        }
    }

    @Override
    public OhMyNotiClient setSnotiConfig(SnotiConfig snotiConfig) {
        Objects.requireNonNull(snotiConfig, "snoti配置信息不能为空");
        this.snotiConfig = snotiConfig;
        return this;
    }

    private boolean sendControlOrder(final String order, final boolean instant) {
        try {

            if (instant) {
                return controlQueue.add(order);

            } else {
                controlQueue.put(order);
                return true;

            }
        } catch (Exception e) {
            log.info("控制指令入队失败. instant[{}] controlCommand[{}] errorMsg[{}]", instant, order, e.getMessage());
            return false;
        }
    }

    private boolean control(AbstractCommandBody body, final boolean instant) {
        if (Objects.equals(LoginState.LOGIN_SUCCESSFUL, loginState)) {
            String order = body.getOrder();
            if (log.isDebugEnabled()) {
                log.debug("发送控制指令[{}]", order);
            }
            return sendControlOrder(order, instant);
        } else {
            log.warn("snoti客户端未登录, 下发控制失败.");
            return false;
        }
    }

    @Override
    public boolean control(String msgId, String productKey, String mac, String did, Object raw) {
        return this.control(ControlUtils.switchControl(msgId, productKey, mac, did, raw), true);
    }

    @Override
    public boolean control(String msgId, String productKey, String mac, String did, Map<String, Object> dataPoint) {
        ProtocolType protocolType = productKeyProtocolMap.getOrDefault(productKey, ProtocolType.WiFi_GPRS);
        AbstractCommandBody body = ControlUtils.switchControl(msgId, productKey, mac, did, dataPoint, protocolType);
        return this.control(body, false);
    }

    @Override
    public boolean tryControl(String productKey, String mac, String did, Object raw) {
        return this.control(ControlUtils.switchControl(StringUtils.EMPTY, productKey, mac, did, raw), true);
    }

    @Override
    public boolean tryControl(String productKey, String mac, String did, Map<String, Object> dataPoint) {
        ProtocolType protocolType = productKeyProtocolMap.getOrDefault(productKey, ProtocolType.WiFi_GPRS);
        return this.control(ControlUtils.switchControl(StringUtils.EMPTY, productKey, mac, did, dataPoint, protocolType), true);
    }

    @Override
    public OhMyNotiClient setCallback(SnotiCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public SnotiCallback getCallback() {
        return this.callback;
    }

    @Override
    public OhMyNotiClientImpl addLoginAuthorizes(AuthorizationData... authorizes) {
        if (Objects.isNull(loginCommand)) {
            synchronized (OhMyNotiClientImpl.class) {
                if (Objects.isNull(loginCommand)) {
                    loginCommand = new LoginReqCommandBody();
                }
            }
        }

        loginCommand.addLoginAuthorizes(authorizes);
        loginCommand.setPrefetchCount(this.snotiConfig.getPrefetchCount());

        //初始化产品协议map, 方便构建控制指令
        productKeyProtocolMap = loginCommand.getData().stream()
                .collect(Collectors.toMap(AuthorizationData::getProduct_key, AuthorizationData::getProtocolType));
        return this;
    }

    @Override
    public synchronized OhMyNotiClientImpl reload(AuthorizationData... authorizes) {
        loginCommand = null;
        addLoginAuthorizes(authorizes);

        this.channel.close();
        this.callback.reload(authorizes);
        log.info("snoti客户端即将重新加载登录信息 ...");
        return this;
    }

    @Override
    public synchronized void setLoginState(LoginState loginState) {
        if (Objects.equals(loginState, LoginState.NOT_LOGGED)) {
            //初始化需要清空ack队列, 避免ack错误MQ会被断开
            log.warn("snoti客户端登录状态重置为未登录, 即将清空无效的ack消息.");
            this.ackReplyQueue.clear();
            log.info("清空ack队列成功.");
        }

        this.loginState = loginState;
        log.info("客户端登录状态为[{}]", loginState);
    }

    @Override
    public boolean storeInformation(JSONObject jsonMessage) {
        try {
            receiveQueue.put(jsonMessage);
            return true;
        } catch (Exception e) {
            log.warn("存储消息[{}]出错", jsonMessage);
        }
        return false;
    }

    @Override
    public String getLoginOrder() {
        setLoginState(LoginState.LOGGING);
        return loginCommand.getOrder();
    }

    /**
     * 开启客户端
     */
    @Override
    public void doStart() {
        initQueue();
        boolean callbackNull = Objects.isNull(callback);
        if (callbackNull) {
            //回调为空, 设置默认回调
            callback = SnotiCallback.identity();
        }
        try {
            ChannelInitializer<SocketChannel> handler = new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {

                    ChannelPipeline p = socketChannel.pipeline();

                    //ssl
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    sslContext.init(null, new TrustManager[]{new SnotiTrustManager()}, new SecureRandom());
                    SSLEngine sslEngine = sslContext.createSSLEngine();
                    sslEngine.setUseClientMode(true);
                    p.addLast(new SslHandler(sslEngine));

                    //编码
                    p.addLast(new LineBasedFrameDecoder(16384));
                    p.addLast(new StringDecoder());
                    p.addLast(new StringEncoder());

                    //心跳检查
                    Long heartbeatIntervalSeconds = OhMyNotiClientImpl.this.snotiConfig.getHeartbeatIntervalSeconds();
                    log.info("设置snoti客户端与服务器心跳检测间隔. [{}]s", heartbeatIntervalSeconds);
                    p.addLast(new IdleStateHandler(heartbeatIntervalSeconds, heartbeatIntervalSeconds, 0L, TimeUnit.SECONDS));

                    p.addLast(new SnotiChannelHandler(OhMyNotiClientImpl.this));

                    if (OhMyNotiClientImpl.this.snotiConfig.getEnableCheckNoData()) {
                        //开启无数据检测
                        Integer noDataWaringMinutes = OhMyNotiClientImpl.this.snotiConfig.getNoDataWarningMinutes();
                        log.info("设置snoti客户端无数据读取检查. 检查时间间隔[{}]min", noDataWaringMinutes);
                        p.addLast(new NoDataChannelHandler(noDataWaringMinutes, OhMyNotiClientImpl.this.getCallback()));
                    }

                    if (OhMyNotiClientImpl.this.snotiConfig.getWithMetrics()) {
                        log.info("使用snoti指标统计.");
                        p.addLast(new SnotiMetricsHandler());
                    }

                    if (OhMyNotiClientImpl.this.snotiConfig.getEnableMessageCounting()) {
                        //开启推送消息计数
                        log.info("设置snoti客户端推送消息计数器.");
                        p.addLast(new PushEventMessageCountingHandler());
                    }
                }
            };

            this.bootstrap = automaticallyGeneratedBootstrap(this.snotiConfig.getUseEpoll())
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000 * 3)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024 * 128, 1024 * 256))
                    .handler(handler);
            this.doConnect();
        } catch (Exception e) {
            log.error("snoti客户端启动错误!!!");

            throw new RuntimeException(e);
        }

        callback.startup();
    }

    private void initQueue() {
        if (Objects.isNull(this.receiveQueue)) {
            this.receiveQueue = new ArrayBlockingQueue<>(this.snotiConfig.getReceiveQueueCapacity());
        }

        if (Objects.isNull(this.ackReplyQueue)) {
            this.ackReplyQueue = new UniqueArrayBlockingQueue<>(this.snotiConfig.getAckReplyQueueCapacity());
        }

        if (Objects.isNull(this.controlQueue)) {
            this.controlQueue = new UniqueArrayBlockingQueue<>(this.snotiConfig.getControlQueueCapacity());
        }
    }

    /**
     * 建立连接
     */
    @Override
    public synchronized void doConnect() {
        log.info("开始建立连接...");
        if (Objects.equals(LoginState.LOGGING, loginState)) {
            log.info("snoti登录中...");
        } else if (Objects.equals(LoginState.LOGIN_FAILED, loginState)) {
            log.error("snoti登录信息出错, 请使用reload方法重新加载登录信息...");
        } else if (this.channel == null || !this.channel.isActive()) {
            ChannelFuture future = this.bootstrap.connect(this.snotiConfig.getHost(), this.snotiConfig.getPort());
            future.addListener((ChannelFutureListener) futureListener -> {
                if (futureListener.isSuccess()) {
                    this.channel = futureListener.channel();
                    log.info("连接到snoti服务器成功, 即将发起登录请求. host[{}] port[{}]", this.snotiConfig.getHost(), this.snotiConfig.getPort());

                } else {

                    Long reConnectSeconds = this.snotiConfig.getReConnectSeconds();
                    log.warn("连接snoti服务器失败, [{}]秒后尝试重连. host[{}] port[{}]", reConnectSeconds, this.snotiConfig.getHost(), this.snotiConfig.getPort());
                    futureListener.channel().eventLoop().schedule(this::doConnect, reConnectSeconds, TimeUnit.SECONDS);
                }

            });

        }
    }

    /**
     * 停止组件
     */
    private void stopComponents() {
        bootstrap.config().group().shutdownGracefully();
        setLoginState(LoginState.NOT_LOGGED);
        this.channel.close();
        log.warn("client is about to shutdown...");
    }

    /**
     * 停止客户端
     */
    @Override
    public void doStop() {
        log.warn("正在停止snoti客户端...");
        this.stopComponents();
        this.callback.stop();
        log.warn("停止snoti客户端完成!!!");
    }
}
