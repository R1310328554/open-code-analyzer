package com.taobao.arthas.core.shell.handlers;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.shell.future.Future;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TermServer {@link #listen} 完成后的回调 Handler。
 * <p>
 * 监听失败时记录错误并将 {@link ArthasBootstrap} 的 bind 标志复位，
 * 避免外部误以为端口已成功监听。
 *
 * @author ralf0131 2017-04-24 18:23.
 */
public class BindHandler implements Handler<Future<Void>> {

    private static final Logger logger = LoggerFactory.getLogger(BindHandler.class);

    private AtomicBoolean isBindRef;

    /** @param isBindRef 指向 Bootstrap 中 isBind 标志的原子引用 */
    public BindHandler(AtomicBoolean isBindRef) {
        this.isBindRef = isBindRef;
    }

    @Override
    /** bind 失败时打日志并将 isBindRef 从 true 改回 false */
    public void handle(Future<Void> event) {
        if (event.failed()) {
            logger.error("Error listening term server:", event.cause());
            isBindRef.compareAndSet(true, false);
        }
    }
}
