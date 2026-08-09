package com.taobao.arthas.core.shell.term.impl.httptelnet;

import java.nio.charset.Charset;

import com.taobao.arthas.core.shell.term.impl.http.session.HttpSessionManager;

import io.netty.util.concurrent.EventExecutorGroup;
import io.termd.core.function.Consumer;
import io.termd.core.function.Supplier;
import io.termd.core.telnet.TelnetHandler;
import io.termd.core.telnet.TelnetTtyConnection;
import io.termd.core.tty.TtyConnection;
import io.termd.core.util.CompletableFuture;
import io.termd.core.util.Helper;

/**
 * {@link NettyHttpTelnetBootstrap} 的 TTY 友好封装：配置 BINARY 选项与字符集，
 * 并将 {@link TelnetTtyConnection} 工厂注入底层引导。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author hengyunabc 2019-11-05
 */
public class NettyHttpTelnetTtyBootstrap {

    private final NettyHttpTelnetBootstrap httpTelnetTtyBootstrap;
    /** Telnet 输出是否启用 BINARY 选项 */
    private boolean outBinary;
    /** Telnet 输入是否启用 BINARY 选项 */
    private boolean inBinary;
    private Charset charset = Charset.forName("UTF-8");

    public NettyHttpTelnetTtyBootstrap(EventExecutorGroup workerGroup, HttpSessionManager httpSessionManager) {
        this.httpTelnetTtyBootstrap = new NettyHttpTelnetBootstrap(workerGroup, httpSessionManager);
    }

    public String getHost() {
        return httpTelnetTtyBootstrap.getHost();
    }

    public NettyHttpTelnetTtyBootstrap setHost(String host) {
        httpTelnetTtyBootstrap.setHost(host);
        return this;
    }

    public int getPort() {
        return httpTelnetTtyBootstrap.getPort();
    }

    public NettyHttpTelnetTtyBootstrap setPort(int port) {
        httpTelnetTtyBootstrap.setPort(port);
        return this;
    }

    public boolean isOutBinary() {
        return outBinary;
    }

    /**
     * 启用或禁用 Telnet 输出的 BINARY 选项。
     *
     * @param outBinary true 要求客户端以二进制接收
     * @return this
     */
    public NettyHttpTelnetTtyBootstrap setOutBinary(boolean outBinary) {
        this.outBinary = outBinary;
        return this;
    }

    public boolean isInBinary() {
        return inBinary;
    }

    /**
     * 启用或禁用 Telnet 输入的 BINARY 选项。
     *
     * @param inBinary true 要求客户端以二进制发送
     * @return this
     */
    public NettyHttpTelnetTtyBootstrap setInBinary(boolean inBinary) {
        this.inBinary = inBinary;
        return this;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /** 异步启动，返回 CompletableFuture */
    public CompletableFuture<?> start(Consumer<TtyConnection> factory) {
        CompletableFuture<?> fut = new CompletableFuture();
        start(factory, Helper.startedHandler(fut));
        return fut;
    }

    /** 异步停止 */
    public CompletableFuture<?> stop() {
        CompletableFuture<?> fut = new CompletableFuture();
        stop(Helper.stoppedHandler(fut));
        return fut;
    }

    public void start(final Consumer<TtyConnection> factory, Consumer<Throwable> doneHandler) {
        httpTelnetTtyBootstrap.start(new Supplier<TelnetHandler>() {
            @Override
            public TelnetHandler get() {
                return new TelnetTtyConnection(inBinary, outBinary, charset, factory);
            }
        }, factory, doneHandler);
    }

    public void stop(Consumer<Throwable> doneHandler) {
        httpTelnetTtyBootstrap.stop(doneHandler);
    }
}
