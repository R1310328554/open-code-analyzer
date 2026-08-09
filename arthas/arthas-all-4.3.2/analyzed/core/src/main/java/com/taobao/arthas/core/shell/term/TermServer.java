package com.taobao.arthas.core.shell.term;

import com.taobao.arthas.common.ArthasConstants;
import com.taobao.arthas.core.config.Configure;
import com.taobao.arthas.core.shell.ShellServerOptions;
import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.term.impl.TelnetTermServer;

/**
 * 终端服务器抽象：监听 Telnet/HTTP 等协议，为每个入站连接创建 {@link Term}。
 * <p>
 * 典型用法：{@code createTelnetTermServer(...).termHandler(...).listen()}，
 * 客户端连入后 termHandler 收到 Term 并启动 Shell 会话。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class TermServer {

    /**
     * 创建 Telnet 协议终端服务器。
     *
     * @param configure Arthas 配置（IP、端口等）
     * @param options Shell 服务器选项（连接超时等）
     * @return Telnet TermServer 实例
     */
    public static TermServer createTelnetTermServer(Configure configure, ShellServerOptions options) {
        int port = configure.getTelnetPort() != null ? configure.getTelnetPort() : ArthasConstants.TELNET_PORT;
        return new TelnetTermServer(configure.getIp(), port, options.getConnectionTimeout());
    }

    /**
     * 创建 HTTP 协议终端服务器（尚未实现）。
     *
     * @return 当前返回 null
     */
    public static TermServer createHttpTermServer() {
        // TODO
        return null;
    }

    /**
     * 设置入站连接处理器：远程终端连入时以 {@link Term} 回调 handler。
     *
     * @param handler 接收新 Term 的 handler
     * @return this
     */
    public abstract TermServer termHandler(Handler<Term> handler);

    /**
     * 绑定监听端口；须先调用 {@link #termHandler(Handler)}。
     *
     * @return this
     */
    public TermServer listen() {
        return listen(null);
    }

    /**
     * 绑定监听并在完成后回调 listenHandler。
     *
     * @param listenHandler 绑定完成回调，可为 null
     * @return this
     */
    public abstract TermServer listen(Handler<Future<TermServer>> listenHandler);

    /**
     * @return 实际监听端口；绑定 0 时返回系统分配的 ephemeral 端口
     */
    public abstract int actualPort();

    /**
     * 关闭服务器并断开所有现有连接；可能异步完成。
     */
    public abstract void close();

    /**
     * 关闭服务器并在完成后通知 completionHandler。
     *
     * @param completionHandler 关闭完成回调
     */
    public abstract void close(Handler<Future<Void>> completionHandler);

}
