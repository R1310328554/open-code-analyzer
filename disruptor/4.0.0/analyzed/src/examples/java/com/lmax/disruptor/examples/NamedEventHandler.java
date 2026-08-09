/**
 * 在 onStart/onShutdown 中设置与恢复线程名的 EventHandler 包装器。
 */

package com.lmax.disruptor.examples;

import com.lmax.disruptor.EventHandler;

public class NamedEventHandler<T> implements EventHandler<T>
{
    private String oldName;
    private final String name;

    public NamedEventHandler(final String name)
    {
        this.name = name;
    }

    @Override
    public void onEvent(final T event, final long sequence, final boolean endOfBatch)
    {
    }

    @Override
    public void onStart()
    {
        final Thread currentThread = Thread.currentThread();
        // 步骤：启动时保存旧名并设置为可读名称
        oldName = currentThread.getName();
        currentThread.setName(name);
    }

    @Override
    public void onShutdown()
    {
        // 步骤：关闭时恢复原始线程名
        Thread.currentThread().setName(oldName);
    }
}
