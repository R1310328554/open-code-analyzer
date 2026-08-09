package com.taobao.arthas.core.shell.term.impl.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.taobao.arthas.common.ArthasConstants;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.term.impl.http.session.HttpSession;
import com.taobao.arthas.core.shell.term.impl.http.session.HttpSessionManager;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.termd.core.http.HttpTtyConnection;

/**
 * 扩展 termd {@link HttpTtyConnection}，将 HTTP Session 注入 WebSocket TTY。
 * <p>
 * WebSocket 握手前 {@link BasicHttpAuthenticatorHandler} 已写入 Subject/userId；
 * 本类在 {@link #extSessions()} 中传递给 Arthas {@link Session}。
 *
 * @author hengyunabc 2021-03-04
 */
public class ExtHttpTtyConnection extends HttpTtyConnection {
    /** Netty channel 上下文，用于写 WebSocket 帧与调度任务 */
    private ChannelHandlerContext context;
    /** 是否为 quiet 模式（URL {@code ?quiet=true}，减少交互输出） */
    private final boolean quiet;

    /** @param context WebSocket 升级后的 channel 上下文 */
    public ExtHttpTtyConnection(ChannelHandlerContext context) {
        this(context, false);
    }

    public ExtHttpTtyConnection(ChannelHandlerContext context, boolean quiet) {
        this.context = context;
        this.quiet = quiet;
    }

    @Override
    /** 将 TTY 输出编码为 {@link TextWebSocketFrame} 写出 */
    protected void write(byte[] buffer) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(buffer);
        if (context != null) {
            context.writeAndFlush(new TextWebSocketFrame(byteBuf));
        }
    }

    @Override
    public void schedule(Runnable task, long delay, TimeUnit unit) {
        if (context != null) {
            context.executor().schedule(task, delay, unit);
        }
    }

    @Override
    public void execute(Runnable task) {
        if (context != null) {
            context.executor().execute(task);
        }
    }

    @Override
    public void close() {
        if (context != null) {
            context.close();
        }
    }

    /** 收集 HTTP Session 中的 Subject、userId 及 quiet 标志供 Shell 使用 */
    public Map<String, Object> extSessions() {
        Map<String, Object> result = new HashMap<String, Object>();
        if (quiet) {
            result.put(Session.QUIET, Boolean.TRUE);
        }
        if (context != null) {
            HttpSession httpSession = HttpSessionManager.getHttpSessionFromContext(context);
            if (httpSession != null) {
                Object subject = httpSession.getAttribute(ArthasConstants.SUBJECT_KEY);
                if (subject != null) {
                    result.put(ArthasConstants.SUBJECT_KEY, subject);
                }
                // 将 HTTP Session 中的 userId 传递给 Arthas Session
                Object userId = httpSession.getAttribute(ArthasConstants.USER_ID_KEY);
                if (userId != null) {
                    result.put(ArthasConstants.USER_ID_KEY, userId);
                }
            }
        }
        if (!result.isEmpty()) {
            return result;
        }
        return Collections.emptyMap();
    }

}
