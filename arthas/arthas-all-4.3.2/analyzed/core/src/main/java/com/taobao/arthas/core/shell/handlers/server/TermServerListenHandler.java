package com.taobao.arthas.core.shell.handlers.server;

import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.impl.ShellServerImpl;
import com.taobao.arthas.core.shell.term.TermServer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多 {@link TermServer} 并行 listen 的聚合回调 Handler。
 * <p>
 * Telnet/HTTP 等终端服务各自异步绑定端口；本 Handler 统计全部 listen 完成或失败，
 * 成功时启动 Shell 定时器，失败时关闭已启动的 TermServer 并向上游报告错误。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class TermServerListenHandler implements Handler<Future<TermServer>> {
    /** 所属 Shell 服务端 */
    private ShellServerImpl shellServer;
    /** listen 全部完成或失败后的上层回调 */
    private Handler<Future<Void>> listenHandler;
    /** 待启动的 TermServer 列表，失败时用于逐个 close */
    private List<TermServer> toStart;
    /** 剩余未完成 listen 的 TermServer 数量 */
    private AtomicInteger count;
    /** 任一 TermServer listen 失败则置 true */
    private AtomicBoolean failed;

    /**
     * @param shellServer 所属 Shell 服务端
     * @param listenHandler 全部 listen 结束后的回调
     * @param toStart 本次并行启动的 TermServer 列表
     */
    public TermServerListenHandler(ShellServerImpl shellServer, Handler<Future<Void>> listenHandler, List<TermServer> toStart) {
        this.shellServer = shellServer;
        this.listenHandler = listenHandler;
        this.toStart = toStart;
        this.count = new AtomicInteger(toStart.size());
        this.failed = new AtomicBoolean();
    }

    @Override
    /** 递减 listen 计数；全部完成后按 failed 标志通知成功或失败 */
    public void handle(Future<TermServer> ar) {
        if (ar.failed()) {
            failed.set(true);
        }

        if (count.decrementAndGet() == 0) {
            if (failed.get()) {
                // 任一端口绑定失败：上报 cause 并关闭已启动的 TermServer
                listenHandler.handle(Future.<Void>failedFuture(ar.cause()));
                for (TermServer termServer : toStart) {
                    termServer.close();
                }
            } else {
                // 全部成功：标记服务端未关闭并启动定时任务
                shellServer.setClosed(false);
                shellServer.setTimer();
                listenHandler.handle(Future.<Void>succeededFuture());
            }
        }
    }
}
