package com.taobao.arthas.core.shell.term.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.term.Term;
import com.taobao.arthas.core.shell.term.TermServer;
import com.taobao.arthas.core.shell.term.impl.http.NettyWebsocketTtyBootstrap;
import com.taobao.arthas.core.shell.term.impl.http.session.HttpSessionManager;

import io.netty.util.concurrent.EventExecutorGroup;
import io.termd.core.function.Consumer;
import io.termd.core.tty.TtyConnection;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Netty WebSocket 的 HTTP 终端服务器实现。
 * <p>
 * 监听 HTTP 端口并升级 WebSocket，每个连接包装为 {@link TermImpl} 交给 Shell。
 * 与 {@link TelnetTermServer} 并列，供 Web Console 与 API 客户端接入。
 *
 * @author beiwei30 on 18/11/2016.
 */
public class HttpTermServer extends TermServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpTermServer.class);

    /** 新终端连接建立时的回调，通常由 ShellServer 注册 */
    private Handler<Term> termHandler;
    /** Netty WebSocket TTY 引导器，负责 bind 与 pipeline */
    private NettyWebsocketTtyBootstrap bootstrap;
    private String hostIp;
    private int port;
    private long connectionTimeout;
    private EventExecutorGroup workerGroup;
    private HttpSessionManager httpSessionManager;

    /**
     * @param hostIp 绑定地址
     * @param port 监听端口
     * @param connectionTimeout 启动等待超时（毫秒）
     * @param workerGroup HTTP 业务线程池
     * @param httpSessionManager HTTP 会话管理器
     */
    public HttpTermServer(String hostIp, int port, long connectionTimeout, EventExecutorGroup workerGroup, HttpSessionManager httpSessionManager) {
        this.hostIp = hostIp;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
        this.workerGroup = workerGroup;
        this.httpSessionManager = httpSessionManager;
    }

    @Override
    public TermServer termHandler(Handler<Term> handler) {
        this.termHandler = handler;
        return this;
    }

    @Override
    public TermServer listen(Handler<Future<TermServer>> listenHandler) {
        // TODO: 从 ShellServerOptions 读取 charset 与 inputrc
        bootstrap = new NettyWebsocketTtyBootstrap(workerGroup, httpSessionManager).setHost(hostIp).setPort(port);
        try {
            bootstrap.start(new Consumer<TtyConnection>() {
                @Override
                /** WebSocket 握手完成后创建 Term 并交给 Shell */
                public void accept(final TtyConnection conn) {
                    termHandler.handle(new TermImpl(Helper.loadKeymap(), conn));
                }
            }).get(connectionTimeout, TimeUnit.MILLISECONDS);
            listenHandler.handle(Future.<TermServer>succeededFuture());
        } catch (Throwable t) {
            logger.error("Error listening to port " + port, t);
            listenHandler.handle(Future.<TermServer>failedFuture(t));
        }
        return this;
    }

    @Override
    /** @return 实际绑定的监听端口（含 port=0 时系统分配） */
    public int actualPort() {
        return bootstrap.getPort();
    }

    @Override
    public void close() {
        close(null);
    }

    @Override
    /** 停止 bootstrap 并可选通知 completionHandler */
    public void close(Handler<Future<Void>> completionHandler) {
        if (bootstrap != null) {
            bootstrap.stop();
            if (completionHandler != null) {
                completionHandler.handle(Future.<Void>succeededFuture());
            }
        } else {
            if (completionHandler != null) {
                completionHandler.handle(Future.<Void>failedFuture("telnet term server not started"));
            }
        }
    }
}
