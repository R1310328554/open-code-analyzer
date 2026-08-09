package com.taobao.arthas.core.shell.handlers.term;

import com.taobao.arthas.core.shell.handlers.Handler;
import io.termd.core.function.Consumer;

/**
 * 将 Arthas {@link Handler} 适配为 termd {@link Consumer} 的关闭事件包装器。
 * <p>
 * termd TTY 层使用 {@code Consumer<Void>} 回调连接关闭；
 * 本类桥接到 Shell 层的 {@link Handler} 接口，便于复用 {@link com.taobao.arthas.core.shell.handlers.shell.CloseHandler} 等实现。
 *
 * @author beiwei30 on 22/11/2016.
 */
public class CloseHandlerWrapper implements Consumer<Void> {
    /** 被包装的 Shell 层关闭 Handler */
    private final Handler<Void> handler;

    /** @param handler 终端关闭时要调用的 Handler */
    public CloseHandlerWrapper(Handler<Void> handler) {
        this.handler = handler;
    }

    @Override
    /** 将 termd 关闭事件转发给 Shell Handler */
    public void accept(Void v) {
        handler.handle(v);
    }
}
