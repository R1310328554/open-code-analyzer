package com.taobao.arthas.core.shell.term;

/**
 * 终端信号（如 Ctrl+C、Ctrl+Z）投递接口。
 * <p>
 * 由 {@link Term#interruptHandler} / {@link Term#suspendHandler} 注册，
 * {@code deliver} 返回 true 表示信号已被消费，不再向上传播。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface SignalHandler {
    /**
     * 处理终端按键信号。
     *
     * @param key termd 键码（如 Ctrl+C 对应值）
     * @return true 表示信号已处理
     */
    boolean deliver(int key);
}
