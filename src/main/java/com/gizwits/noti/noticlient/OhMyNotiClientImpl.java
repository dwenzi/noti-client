package com.gizwits.noti.noticlient;

import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.gizwits.noti.noticlient.bean.Credential;
import com.gizwits.noti.noticlient.bean.req.NotiCtrlDTO;
import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import com.gizwits.noti.noticlient.bean.req.body.AbstractCommandBody;
import com.gizwits.noti.noticlient.bean.req.body.SubscribeReqCommandBody;
import com.gizwits.noti.noticlient.bean.req.body.UnsubscribeReqCommandBody;
import com.gizwits.noti.noticlient.bean.resp.body.SubscribeCallbackEventBody;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import com.gizwits.noti.noticlient.config.SnotiConfig;
import com.gizwits.noti.noticlient.config.SnotiTrustManager;
import com.gizwits.noti.noticlient.enums.LoginState;
import com.gizwits.noti.noticlient.handler.NoDataChannelHandler;
import com.gizwits.noti.noticlient.handler.SnotiChannelHandler;
import com.gizwits.noti.noticlient.handler.SnotiMetricsHandler;
import com.gizwits.noti.noticlient.util.CommandUtils;
import com.gizwits.noti.noticlient.util.ControlUtils;
import com.gizwits.noti.noticlient.util.UniqueArrayBlockingQueue;
import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Snoti客户端实现
 *
 * @author Jcxcc
 * @since 1.0
 */
@Slf4j
public class OhMyNotiClientImpl extends AbstractSnotiClient implements OhMyNotiClient {

    private BlockingQueue<JSONObject> receiveQueue;
    private BlockingQueue<String> ackReplyQueue;
    private BlockingQueue<String> controlQueue;

    private Channel channel;
    private Bootstrap bootstrap;
    //metrics
    private MetricRegistry metricRegistry;
    private ScheduledReporter reporter;
    private Meter meter;

    private static final long DEFAULT_POLL_TIMEOUT_MS = 2;
    private static final AtomicBoolean WORK = new AtomicBoolean(false);
    private static final ReentrantReadWriteLock CREDENTIAL_LOCK = new ReentrantReadWriteLock();

    private final Executor executor;

    public OhMyNotiClientImpl(Executor executor) {
        super();
        this.executor = executor;
    }

    public OhMyNotiClientImpl() {
        this(Executors.newSingleThreadExecutor());
    }

    @Override
    public void sendMsg(Object msg) {
        if (!WORK.get()) {
            log.warn("未登陆, 无法控制. [{}]", msg);
            return;
        }
        String strMsg;
        if (msg instanceof String) {
            strMsg = (String) msg;
        } else if (msg instanceof AbstractCommandBody) {
            strMsg = ((AbstractCommandBody) msg).getOrder();
        } else {
            strMsg = String.valueOf(msg);
        }
        sendControlOrder(strMsg);
    }

    @Override
    public synchronized void switchPushMessage() {
        log.debug("推送开关打开...");

        WORK.set(true);
        executor.execute(() -> {
            while (true) {
                if (!WORK.get()) {
                    //连接断开时登录状态初始化, 回复线程退出
                    log.warn("非工作状态, 退出回复线程.");
                    break;
                }

                if (channel.isWritable()) {

                    try {
                        String deliveryId = this.ackReplyQueue.poll(DEFAULT_POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                        if (Objects.nonNull(deliveryId)) {
                            String eventAckMessage = CommandUtils.getEventAckMessage(deliveryId);
                            channel.writeAndFlush(eventAckMessage).addListener(future -> {
                                if (!future.isSuccess()) {
                                    if (!WORK.get()) {
                                        log.warn("回复ack失败, 即将返回ack回复队列重试. deliveryId[{}]", deliveryId);
                                        confirm(deliveryId);
                                        log.info("重新放入ack回复队列成功. deliveryId[{}]", deliveryId);
                                    } else {
                                        log.info("当前状态为未登录, 不重试ack. deliveryId[{}]", deliveryId);
                                    }
                                } else {
                                    log.debug("回复ack成功. deliveryId[{}]", deliveryId);
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        log.warn("回复ack消息失败. " + e.getMessage());
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
                                        log.debug("下发控制指令成功. [{}]", controlOrder);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            log.warn("下发控制指令失败. " + e.getMessage());
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
    public void disconnected() {
        WORK.set(false);
        this.callback.disconnected();
        log.info("连接已断开.");
        this.credentials.forEach(it -> it.setLoginState(LoginState.NOT_LOGGED));
    }

    @Override
    public JSONObject receiveMessage() {
        try {
            JSONObject json = receiveQueue.take();

            String cmd = json.getString("cmd");
            //推送事件才需要回复ack
            boolean needAck = StringUtils.equals(cmd, NotiGeneralCommandType.event_push.getCode());
            if (needAck && this.snotiConfig.getAutomaticConfirmation()) {
                //推送事件自动回复ack
                confirm(json);
            }

            return json;

        } catch (InterruptedException e) {
            log.warn("获取消息失败, 返回空的json.");
            return new JSONObject();
        }

    }

    @Override
    public OhMyNotiClient setSnotiConfig(SnotiConfig snotiConfig) {
        Objects.requireNonNull(snotiConfig, "snoti配置信息不能为空");
        this.snotiConfig = snotiConfig;
        return this;
    }

    private boolean sendControlOrder(final String order) {
        try {
            controlQueue.put(order);
            return true;
        } catch (Exception e) {
            log.info("控制指令入队失败. controlCommand[{}] errorMsg[{}]", order, e.getMessage());
            return false;
        }
    }

    private boolean control(AbstractCommandBody body) {
        if (WORK.get()) {
            String order = body.getOrder();
            if (log.isDebugEnabled()) {
                log.debug("发送控制指令[{}]", order);
            }
            return sendControlOrder(order);
        } else {
            log.warn("snoti客户端未工作, 控制指令将在切换到工作状态后下发.");
            return false;
        }
    }

    @Override
    public boolean control(String msgId, String productKey, String mac, String did, Object data) {
        return this.control(ControlUtils.parseCtrl(msgId, NotiCtrlDTO.of(productKey, mac, did, data)));
    }

    @Override
    public boolean batchControl(String msgId, NotiCtrlDTO... ctrlDTOs) {
        return this.control(ControlUtils.parseCtrl(msgId, ctrlDTOs));
    }

    @Override
    public boolean confirm(String deliveryId) {
        try {
            ackReplyQueue.put(deliveryId);
            return true;
        } catch (Exception e) {
            log.warn("ack message入队失败. deliverId[{}] {}", deliveryId, e.getMessage());
            e.printStackTrace();
            return false;
        }
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
    public List<Credential> getCredentials() {
        CREDENTIAL_LOCK.readLock().lock();
        List<Credential> credentials = null;
        try {
            credentials = this.credentials;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CREDENTIAL_LOCK.readLock().unlock();
        }

        return credentials;
    }

    @Override
    public OhMyNotiClient setCredentials(List<Credential> _credentials) {
        CREDENTIAL_LOCK.writeLock().lock();
        try {
            Preconditions.checkArgument(_credentials != null && _credentials.size() > 0,
                    "credentials can not be empty.");

            if (WORK.get()) {
                //旧对新的差集, 取消订阅
                this.credentials.stream()
                        .filter(c -> !_credentials.contains(c))
                        .forEach(c -> handleSubscribe(false, c));

                for (Credential credential : _credentials) {
                    if (!this.credentials.contains(credential)) {
                        //新增订阅
                        handleSubscribe(true, credential);
                    }
                }
            }

            this.credentials = _credentials;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CREDENTIAL_LOCK.writeLock().unlock();
            log.debug("释放 CREDENTIAL_LOCK.");
        }
        return this;
    }


    /**
     * Handle subscribe.
     *
     * @param subscribe  the subscribe
     * @param credential the credential
     */
    private void handleSubscribe(boolean subscribe, Credential credential) {
        String order = subscribe
                ? new SubscribeReqCommandBody(credential).getOrder()
                : new UnsubscribeReqCommandBody(credential).getOrder();
        String type = subscribe ? "subscribe" : "unsubscribe";
        log.info("Sending {} request. {} ", type, order);
        sendMsg(order);
    }

    @Override
    public void markLoginState(JSONObject json) {
        CREDENTIAL_LOCK.writeLock().lock();
        try {
            SubscribeCallbackEventBody body = CommandUtils.parsePushEvent(json, SubscribeCallbackEventBody.class);
            setCredentials(getCredentials().stream()
                    .peek(it -> {
                        if (StringUtils.equals(it.getProductKey(), body.getProductKey())) {
                            it.setLoginState(body.getResult() ? LoginState.LOGIN_SUCCESSFUL : LoginState.LOGIN_FAILED);
                        }
                    })
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CREDENTIAL_LOCK.writeLock().unlock();
            log.debug("释放 CREDENTIAL_LOCK.");
        }
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
            ChannelInitializer<SocketChannel> handler = getSocketChannelChannelInitializer();
            this.bootstrap = automaticallyGeneratedBootstrap(this.snotiConfig.getUseEpoll())
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000 * 3)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024 * 128, 1024 * 256))
                    .handler(handler);
            this.doConnect();
        } catch (Exception e) {
            log.warn("snoti客户端启动错误!!!");

            throw new RuntimeException(e);
        }

        callback.startup();
    }

    private ChannelInitializer<SocketChannel> getSocketChannelChannelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {

                //check config
                Boolean automaticConfirmation = snotiConfig.getAutomaticConfirmation();
                if (!automaticConfirmation) {
                    //手动回复
                    log.info("手动回复已开启, 请在处理完消息后手动回复ack.");
                }

                ChannelPipeline p = socketChannel.pipeline();

                //ssl
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{new SnotiTrustManager()}, new SecureRandom());
                SSLEngine sslEngine = sslContext.createSSLEngine();
                sslEngine.setUseClientMode(true);
                p.addLast(new SslHandler(sslEngine));

                //编码
                p.addLast(new LineBasedFrameDecoder(16384));
                p.addLast(new StringDecoder(CharsetUtil.UTF_8));
                p.addLast(new StringEncoder(CharsetUtil.UTF_8));

                //心跳检查, 每隔一定的时间如果 client 和 server 没有通信消息. client 就会主动发送 ping, server 收到后会恢复 pong
                Long heartbeatIntervalSeconds = OhMyNotiClientImpl.this.snotiConfig.getHeartbeatIntervalSeconds();
                log.info("设置snoti客户端与服务器心跳检测间隔. [{}]s", heartbeatIntervalSeconds);
                p.addLast(new IdleStateHandler(heartbeatIntervalSeconds, heartbeatIntervalSeconds, heartbeatIntervalSeconds, TimeUnit.SECONDS));

                p.addLast(new SnotiChannelHandler(OhMyNotiClientImpl.this));

                if (OhMyNotiClientImpl.this.snotiConfig.getEnableCheckNoData()) {
                    //开启无数据检测
                    Integer noDataWaringMinutes = OhMyNotiClientImpl.this.snotiConfig.getNoDataWarningMinutes();
                    log.info("设置snoti客户端无数据读取检查. 检查时间间隔[{}]min", noDataWaringMinutes);
                    p.addLast(new NoDataChannelHandler(noDataWaringMinutes, OhMyNotiClientImpl.this.getCallback()));
                }

                if (OhMyNotiClientImpl.this.snotiConfig.getWithMetrics()) {
                    log.info("使用snoti指标统计.");
                    p.addLast(new SnotiMetricsHandler(createMeterIfNecessary()));
                }
            }
        };
    }

    private Meter createMeterIfNecessary() {
        if (Objects.isNull(this.meter)) {
            synchronized (OhMyNotiClientImpl.class) {
                if (Objects.isNull(this.meter)) {
                    this.metricRegistry = new MetricRegistry();
                    this.reporter = Slf4jReporter.forRegistry(metricRegistry)
                            .outputTo(log)
                            .convertRatesTo(TimeUnit.SECONDS)
                            .convertDurationsTo(TimeUnit.SECONDS)
                            .build();
                    this.meter = metricRegistry.meter("event_msg_meter");
                    log.info("初始化snoti指标成功.");
                    reporter.start(15, 30, TimeUnit.SECONDS);
                }
            }
        }

        return meter;
    }

    private void initQueue() {
        if (Objects.isNull(this.receiveQueue)) {
            this.receiveQueue = new ArrayBlockingQueue<>(this.snotiConfig.getReceiveQueueCapacity());
            log.info("初始化消息接收队列成功.");
        }

        if (Objects.isNull(this.ackReplyQueue)) {
            this.ackReplyQueue = new UniqueArrayBlockingQueue<>(this.snotiConfig.getAckReplyQueueCapacity());
            log.info("初始化ack队列成功.");
        }

        if (Objects.isNull(this.controlQueue)) {
            this.controlQueue = new UniqueArrayBlockingQueue<>(this.snotiConfig.getControlQueueCapacity());
            log.info("初始化控制队列成功.");
        }
    }

    /**
     * 建立连接
     */
    @Override
    public synchronized void doConnect() {
        log.info("开始建立连接...");
        if (this.channel == null || !this.channel.isActive()) {
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
        try {
            bootstrap.config().group().shutdownGracefully();
            this.channel.close();
            this.reporter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
