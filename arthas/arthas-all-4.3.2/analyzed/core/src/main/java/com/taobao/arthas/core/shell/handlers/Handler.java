package com.taobao.arthas.core.shell.handlers;

/**
 * Shell 异步事件回调接口（类似 Vert.x Handler）。
 * <p>
 * 用于 Future 完成、TermServer 生命周期、命令中断等场景。
 */
public interface Handler<E> {
    /**
     * 处理已发生的事件。
     *
     * @param event 待处理的事件对象
     */
    void handle(E event);
}