package com.gizwits.noti.noticlient.handler;

import com.gizwits.noti.noticlient.config.SnotiCallback;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author Jcxcc
 * @since 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class NoDataChannelHandler extends SimpleChannelInboundHandler<String> {

    private final SnotiCallback snotiCallback;

    private final Integer noDataWarningMillis;

    public NoDataChannelHandler(Integer noDataWaringMinutes, SnotiCallback snotiCallback) {
        this.snotiCallback = snotiCallback;
        this.lastUpdateTime = System.currentTimeMillis();
        this.noDataWarningMillis = noDataWaringMinutes * 60 * 1000;
    }

    /**
     * push event 最后更新时间
     */
    private volatile long lastUpdateTime;

    /**
     * 接收信息
     * <p>
     * 这里只能接收到推送事件
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        this.lastUpdateTime = System.currentTimeMillis();

        //交给下一个处理--推送消息计数器
        ctx.fireChannelRead(msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();

            //一段时间内没有接收到来自服务器的数据
            if (Objects.equals(state, IdleState.READER_IDLE)) {
                long howLong = System.currentTimeMillis() - lastUpdateTime;
                boolean noData = howLong > this.noDataWarningMillis;
                if (noData) {
                    this.snotiCallback.noDataForAWhile(howLong / 1000 / 60, ctx);
                }
            }
        }

    }
}
