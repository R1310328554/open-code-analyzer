package com.taobao.arthas.core.shell.handlers.term;

import com.taobao.arthas.core.shell.handlers.Handler;
import io.termd.core.function.Consumer;
import io.termd.core.util.Vector;

/**
 * termd 终端尺寸变更事件的适配器，将 {@link Consumer} 回调桥接到 Arthas {@link Handler}。
 * <p>
 * 终端窗口 resize 时 termd 传入 {@link Vector}，本类忽略具体尺寸并触发 Shell 侧刷新逻辑。
 *
 * @author beiwei30 on 22/11/2016.
 */
public class SizeHandlerWrapper implements Consumer<Vector> {
    /** 终端尺寸变化时触发的 Arthas 回调 */
    private final Handler<Void> handler;

    /** @param handler 尺寸变更时调用的处理器 */
    public SizeHandlerWrapper(Handler<Void> handler) {
        this.handler = handler;
    }

    @Override
    /** termd 回调：收到 resize 向量后通知 Shell（不传递具体尺寸） */
    public void accept(Vector resize) {
        handler.handle(null);
    }
}
