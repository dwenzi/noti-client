package com.gizwits.noti.noticlient.config;

import com.gizwits.noti.noticlient.bean.req.body.AuthorizationData;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * snoti 回调
 *
 * @author Jcxcc
 * @since 1.0
 */
public interface SnotiCallback {

    /**
     * The constant log.
     */
    Logger log = LoggerFactory.getLogger(SnotiCallback.class);

    /**
     * Startup.
     */
    default void startup() {
        log.info("snoti 客户端启动...");
    }

    /**
     * Login successful.
     */
    default void loginSuccessful() {
        log.info("snoti登录成功...");
    }

    /**
     * Login failed.
     *
     * @param errorMessage the error message
     */
    default void loginFailed(String errorMessage) {
        log.error("snoti登录失败[{}]...", errorMessage);
    }


    /**
     * Disconnected.
     */
    default void disconnected() {
        log.warn("snoti客户端连接断开, 即将尝试重连...");
    }

    /**
     * Reload.
     *
     * @param authorizationData the login authorize infos
     */
    default void reload(AuthorizationData... authorizationData) {
        log.info("snoti重载登录信息[{}]...", Stream.of(authorizationData).map(AuthorizationData::toString).collect(Collectors.joining(",")));
    }

    /**
     * Stop.
     */
    default void stop() {
        log.warn("snoti客户端结束...");
    }

    /**
     * No data for a while.
     *
     * @param minutes the minutes
     * @param ctx
     */
    default void noDataForAWhile(Long minutes, ChannelHandlerContext ctx) {
        log.warn("snoti客户端[{}]分钟没有接收到数据. 即将重新连接客户端 ", minutes);
        ctx.channel().close();
    }

    /**
     * Identity noti callback.
     *
     * @return the noti callback
     */
    static SnotiCallback identity() {
        return new SnotiCallback() {
        };
    }
}
