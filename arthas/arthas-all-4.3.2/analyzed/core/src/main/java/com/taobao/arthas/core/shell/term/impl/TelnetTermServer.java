package com.taobao.arthas.core.shell.term.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.term.Term;
import com.taobao.arthas.core.shell.term.TermServer;
import io.termd.core.function.Consumer;
import io.termd.core.telnet.netty.NettyTelnetTtyBootstrap;
import io.termd.core.tty.TtyConnection;

import java.util.concurrent.TimeUnit;

/**
 * Telnet 终端服务器封装，基于 termd {@link NettyTelnetTtyBootstrap}。
 * <p>
 * 监听 TCP 端口接受 Telnet 客户端，每个 {@link TtyConnection} 包装为 {@link TermImpl}
 * 供 Shell 交互；二进制模式开启以支持 ANSI 控制序列。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class TelnetTermServer extends TermServer {

    private static final Logger logger = LoggerFactory.getLogger(TelnetTermServer.class);

    /** termd Telnet 引导器实例 */
    private NettyTelnetTtyBootstrap bootstrap;
    private String hostIp;
    private int port;
    private long connectionTimeout;

    /** 终端连接就绪时的 Shell 回调 */
    private Handler<Term> termHandler;

    /** @param hostIp 绑定 IP；@param port 端口；@param connectionTimeout 启动超时毫秒 */
    public TelnetTermServer(String hostIp, int port, long connectionTimeout) {
        this.hostIp = hostIp;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public TermServer termHandler(Handler<Term> handler) {
        termHandler = handler;
        return this;
    }

    @Override
    public TermServer listen(Handler<Future<TermServer>> listenHandler) {
        // TODO: 从 options 注入 charset 与 inputrc
        bootstrap = new NettyTelnetTtyBootstrap()
                .setHost(hostIp)
                .setPort(port)
                .setInBinary(true)
                .setOutBinary(true);
        try {
            bootstrap.start(new Consumer<TtyConnection>() {
                @Override
                /** Telnet 连接建立：加载 keymap 并创建 TermImpl */
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
    public void close() {
        close(null);
    }

    @Override
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

    /** @return bootstrap 实际监听端口 */
    public int actualPort() {
        return bootstrap.getPort();
    }
}
