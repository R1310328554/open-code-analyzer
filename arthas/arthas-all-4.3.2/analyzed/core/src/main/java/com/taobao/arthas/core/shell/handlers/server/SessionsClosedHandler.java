package com.taobao.arthas.core.shell.handlers.server;

import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.Handler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批量 Shell 会话关闭计数器：全部会话断开后再触发完成回调。
 * <p>
 * 配合 {@link ShellServerImpl#close()} 等待多个 {@link SessionClosedHandler} 并行注销；
 * 计数归零时通知 {@link #completionHandler}，表示服务端可安全退出。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class SessionsClosedHandler implements Handler<Future<Void>> {
    /** 待关闭会话剩余数量 */
    private final AtomicInteger count;
    /** 全部会话关闭后调用的完成 Handler */
    private final Handler<Future<Void>> completionHandler;

    /**
     * @param count 初始待关闭会话数（通常等于当前活跃会话数）
     * @param completionHandler 计数归零时触发的回调
     */
    public SessionsClosedHandler(AtomicInteger count, Handler<Future<Void>> completionHandler) {
        this.count = count;
        this.completionHandler = completionHandler;
    }

    @Override
    /** 每关闭一个会话递减计数，归零时通知 completionHandler 成功 */
    public void handle(Future<Void> event) {
        if (count.decrementAndGet() == 0) {
            completionHandler.handle(Future.<Void>succeededFuture());
        }
    }
}
