package com.taobao.arthas.core.shell.handlers;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.shell.future.Future;

/**
 * 空操作 Handler：默认忽略事件，仅对失败的 {@link Future} 打错误日志。
 * <p>
 * 用作 {@link ShellServer#listen} 等无需额外逻辑的成功回调占位。
 *
 * @author beiwei30 on 22/11/2016.
 */
public class NoOpHandler<E> implements Handler<E> {

    private static final Logger logger = LoggerFactory.getLogger(NoOpHandler.class);

    @Override
    /** Future 失败时记录 term server 监听错误，其余事件无操作 */
    public void handle(E event) {
        if (event instanceof Future && ((Future) event).failed()) {
            logger.error("Error listening term server:", ((Future) event).cause());
        }
    }
}
