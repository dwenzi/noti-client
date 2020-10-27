package com.gizwits.noti.noticlient.handler;

import com.codahale.metrics.Meter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * snoti指标处理器
 *
 * @author Jcxcc
 * @since 1.8.7
 */
@Slf4j
@ChannelHandler.Sharable
public class SnotiMetricsHandler extends SimpleChannelInboundHandler<String> {

    private final Meter meter;

    /**
     * Instantiates a new Snoti metrics handler.
     * <p>
     * 暂时不开放指标配置
     */
    public SnotiMetricsHandler(Meter meter) {
        super();
        this.meter = meter;

//        MetricRegistry metricRegistry = new MetricRegistry();
//        final Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
//                .outputTo(log)
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.SECONDS)
//                .build();
//
//        pushEventMsgMeter = metricRegistry.meter("push_event_msg_meter");
//        log.info("初始化snoti指标成功.");
//
//        reporter.start(15, 30, TimeUnit.SECONDS);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        meter.mark();
        ctx.fireChannelRead(msg);
    }
}
