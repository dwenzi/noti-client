package com.gizwits.noti.noticlient.config;

import com.gizwits.noti.noticlient.AbstractSnotiClient;
import com.gizwits.noti.noticlient.OhMyNotiClient;
import com.gizwits.noti.noticlient.bean.req.body.LoginReqCommandBody;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * snoti配置信息
 *
 * @author Jcxcc
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class SnotiConfig {

    /**
     * Instantiates a new Snoti config.
     */
    public SnotiConfig() {
        //基本信息
        this.host = "snoti.gizwits.com";
        this.port = 2017;
        this.prefetchCount = 50;
        this.receiveQueueCapacity = 64;
        this.controlQueueCapacity = 128;
        this.reConnectSeconds = 20L;
        this.heartbeatIntervalSeconds = 60L;
        //基本信息end

        //无数据检查
        this.noDataWarningMinutes = 2;
        this.enableCheckNoData = true;
        //无数据检查end

        //指标
        this.withMetrics = false;
        //指标end

        //其他配置
        this.useEpoll = true;
        this.automaticConfirmation = true;
    }

    /**
     * 获取ack回复队列容量
     * <p>
     * 容量长度 = prefetchCount * 2
     * 理论上ack回复队列个数等于prefetchCount
     *
     * @return ack reply queue capacity
     */
    public Integer getAckReplyQueueCapacity() {
        return this.getPrefetchCount() * 2;
    }

    /**
     * Sets no data warning minutes.
     *
     * @param noDataWarningMinutes the no data warning minutes
     * @return the no data warning minutes
     */
    public SnotiConfig setNoDataWarningMinutes(Integer noDataWarningMinutes) {
        if (noDataWarningMinutes < 0) {
            throw new IllegalArgumentException("无数据检测分钟不能为负数");
        }
        this.noDataWarningMinutes = noDataWarningMinutes;
        return this;
    }

    /**
     * Sets heartbeat interval seconds.
     *
     * @param heartbeatIntervalSeconds the heartbeat interval seconds
     * @return the heartbeat interval seconds
     */
    public SnotiConfig setHeartbeatIntervalSeconds(Long heartbeatIntervalSeconds) {
        final long
                minHeartbeatIntervalSeconds = 30,
                maxHeartbeatIntervalSeconds = 300;
        if (heartbeatIntervalSeconds < minHeartbeatIntervalSeconds || heartbeatIntervalSeconds > maxHeartbeatIntervalSeconds) {
            throw new IllegalArgumentException("心跳间隔必须在30到300秒之间");
        }
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
        return this;
    }

    /**
     * Sets prefetch count.
     *
     * @param prefetchCount the prefetch count
     * @return the prefetch count
     */
    public SnotiConfig setPrefetchCount(Integer prefetchCount) {
        final int
                minPrefetchCount = 50,
                maxPrefetchCount = 5000;
        if (prefetchCount > maxPrefetchCount || prefetchCount < minPrefetchCount) {
            throw new IllegalArgumentException("prefetchCount必须在50到5000之间");
        }
        this.prefetchCount = prefetchCount;
        return this;
    }

    /**
     * 主机
     * <p>
     * 默认为 snoti.gizwits.com, 即公有云snoti主机
     */
    private String host;

    /**
     * 端口号
     * <p>
     * 默认为 2017, 即公有云snoti端口
     */
    private Integer port;

    /**
     * 预取数
     * <p>
     * 建议50
     * 超过此数据的ack无回复则服务端不再发送信息
     */
    private Integer prefetchCount;

    /**
     * 接收队列容量
     */
    private Integer receiveQueueCapacity;

    /**
     * 控制队列容量
     */
    private Integer controlQueueCapacity;

    /**
     * 无数据分钟数
     * 多少分钟无数据则触发告警
     * 默认2{@link #SnotiConfig()}
     */
    private Integer noDataWarningMinutes;

    /**
     * 是否开启无数据检测
     * 默认开启{@link #SnotiConfig()}
     */
    private Boolean enableCheckNoData;

    /**
     * 重连间隔时间
     * 默认20s{@link #SnotiConfig()}
     */
    private Long reConnectSeconds;

    /**
     * 心跳包间隔时间
     * 默认60s{@link #SnotiConfig()}
     */
    private Long heartbeatIntervalSeconds;

    /**
     * 是否使用指标
     * <p>
     * 默认否{@link #SnotiConfig()}
     */
    private Boolean withMetrics;

    /**
     * 是否使用 epoll
     * 默认为true{@link #SnotiConfig()}
     * <p>
     * useEpoll = true 时, 意味着:
     * 如果系统支持使用epoll则优先使用epoll, 具体实现见 {@link AbstractSnotiClient#automaticallyGeneratedBootstrap(boolean)}.
     * 有时候{@link AbstractSnotiClient#canUseEpoll()} 为true时, 由于系统环境的原因在调用epoll的时候失败, 此时建议设置为false.
     * </p>
     * <p>
     * useEpoll = false 时, 则不会使用epoll.
     * </p>
     */
    private Boolean useEpoll;

    /**
     * 自动确认
     * 默认为true
     * <p>
     * 当true时,
     * 通过使用 {@link OhMyNotiClient#receiveMessage()} 方法接受消息时会自动ack
     * 当false时，
     * 通过使用 {@link OhMyNotiClient#receiveMessage()} 方法接受消息时需要手动调用
     * 注意, 如果此时没有手动回复ack. 当达到了 preFetch{@link LoginReqCommandBody#getPrefetchCount()}时,
     * snoti服务端不会再推送消息.
     */
    private Boolean automaticConfirmation;
}
