package com.gizwits.noti.noticlient;

import com.gizwits.noti.noticlient.bean.req.body.LoginReqCommandBody;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import com.gizwits.noti.noticlient.config.SnotiConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;
import lombok.Getter;
import org.slf4j.LoggerFactory;

/**
 * Snoti客户端
 *
 * @author Jcxcc
 * @since 1.0
 */
public abstract class AbstractSnotiClient {

    /**
     * The Callback.
     */
    protected SnotiCallback callback;
    /**
     * The Snoti config.
     */
    @Getter
    protected SnotiConfig snotiConfig;
    /**
     * The Login command.
     */
    @Getter
    protected LoginReqCommandBody loginCommand;


    /**
     * Instantiates a new Abstract snoti client.
     */
    public AbstractSnotiClient() {
        this.snotiConfig = new SnotiConfig();
    }

    /**
     * Send msg.
     *
     * @param msg the msg
     */
    public abstract void sendMsg(Object msg);

    /**
     * 是否可以使用epoll
     * <p>
     * 针对Linux
     * Linux下epoll各方面表现更优秀
     *
     * @return the boolean
     */
    protected static boolean canUseEpoll() {
        return Epoll.isAvailable();
    }

    private static Bootstrap generateBootstrap(EventLoopGroup eventLoopGroup, Class<? extends SocketChannel> channelClazz) {
        return new Bootstrap().group(eventLoopGroup).channel(channelClazz);
    }

    /**
     * 自动生成bootstrap
     *
     * @param useEpoll the use epoll
     * @return the bootstrap
     */
    public static Bootstrap automaticallyGeneratedBootstrap(final boolean useEpoll) {
        if (useEpoll && canUseEpoll()) {
            LoggerFactory.getLogger("snoti client").info("生成epoll bootstrap");
            return generateBootstrap(new EpollEventLoopGroup(getCoreSize()), EpollSocketChannel.class);

        } else {
            LoggerFactory.getLogger("snoti client").info("生成nio bootstrap");
            return generateBootstrap(new NioEventLoopGroup(getCoreSize()), NioSocketChannel.class);
        }
    }


    /**
     * 获取处理线程数
     *
     * @return the core size
     */
    public static int getCoreSize() {
        final int defaultCoreSize = 4;
        return Math.max(defaultCoreSize, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
    }
}
