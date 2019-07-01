package com.gizwits.noti.noticlient;

import com.gizwits.noti.noticlient.config.SnotiConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Abstract snoti client.
 *
 * @author Jcxcc
 * @since 1.0
 */
public abstract class AbstractSnotiClient {

    static final Logger log = LoggerFactory.getLogger("snoti客户端");

    protected SnotiConfig snotiConfig;

    public AbstractSnotiClient() {
        this.snotiConfig = new SnotiConfig();
    }

    /**
     * 是否可以使用epoll
     * <p>
     * 针对Linux
     * Linux下epoll各方面表现更优秀
     *
     * @return the boolean
     */
    protected static boolean canUseEpoll() {
        String property = System.getProperty("os.name").toLowerCase().trim();
        return property.contains("linux");
    }

    private static Bootstrap generateBootstrap(EventLoopGroup eventLoopGroup, Class<? extends SocketChannel> channelClazz) {
        return new Bootstrap().group(eventLoopGroup).channel(channelClazz);
    }

    /**
     * 自动生成bootstrap
     *
     * @return the bootstrap
     */
    public static Bootstrap automaticallyGeneratedBootstrap() {
        if (canUseEpoll()) {
            log.info("使用epoll. 当前系统为linux, 支持epoll.");
            return generateBootstrap(new EpollEventLoopGroup(getCoreSize()), EpollSocketChannel.class);

        } else {
            log.info("使用nio. 当前系统不支持支持epoll.");
            return generateBootstrap(new NioEventLoopGroup(getCoreSize()), NioSocketChannel.class);
        }
    }


    /**
     * 获取处理线程数
     * <p>
     * 最少两个, 其中一个用来写控制指令
     *
     * @return the core size
     */
    public static int getCoreSize() {
        final int defaultCoreSize = 4;
        return Math.max(defaultCoreSize, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
    }
}
