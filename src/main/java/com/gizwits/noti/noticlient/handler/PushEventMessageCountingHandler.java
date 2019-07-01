package com.gizwits.noti.noticlient.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Jcxcc
 * @since 1.0
 */
@Slf4j
public class PushEventMessageCountingHandler extends SimpleChannelInboundHandler<String> {

    private static final LongAdder longCounter = new LongAdder();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        longCounter.increment();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        if (evt instanceof IdleStateEvent) {
            //伴随着心跳, 打印推送消息计数
            log.info("接收到snoti推送消息[{}]条", longCounter.sum());
        }
    }

    /**
     * 连接断开时， 打印并重置计数
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("接收到snoti推送消息[{}]条", longCounter.sumThenReset());
    }
}
