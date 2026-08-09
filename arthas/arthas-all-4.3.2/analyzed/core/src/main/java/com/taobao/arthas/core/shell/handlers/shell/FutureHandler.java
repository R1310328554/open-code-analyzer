package com.taobao.arthas.core.shell.handlers.shell;

import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.Handler;

/**
 * 将 Void 事件桥接为 {@link Future#complete()} 的简单 Handler。
 * <p>
 * 用于 Job 结束、终端关闭等需要以 Future 通知上游的场景，
 * 收到事件后立即标记 Future 为成功完成。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class FutureHandler implements Handler<Void> {
    /** 待完成的 Future 实例 */
    private Future future;

    /** @param future 收到事件后要 complete 的 Future */
    public FutureHandler(Future future) {
        this.future = future;
    }

    @Override
    /** 标记 Future 为成功完成 */
    public void handle(Void event) {
        future.complete();
    }
}
