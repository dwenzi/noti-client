package com.gizwits.noti.noticlient.config;

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

    public SnotiConfig() {
        this.host = "snoti.gizwits.com";
        this.port = 2017;
        this.prefetchCount = 50;
        this.receiveQueueCapacity = 64;
        this.controlQueueCapacity = 128;

        this.noDataWarningMinutes = 2;
        this.enableCheckNoData = true;

        this.reConnectSeconds = 20L;

        this.heartbeatIntervalSeconds = 60L;

        this.enableMessageCounting = false;
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
     */
    private String host;

    /**
     * 端口号
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
     * 是否开启消息计数
     * 默认不开启{@link #SnotiConfig()}
     */
    private Boolean enableMessageCounting;
}
