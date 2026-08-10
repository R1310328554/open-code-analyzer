/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel;

/**
 * {@link SingleThreadEventLoop} which is used to handle OIO {@link Channel}'s. So in general there will be
 * one {@link ThreadPerChannelEventLoop} per {@link Channel}.
 * <p>用于 OIO {@link Channel} 的单线程 {@link EventLoop}：通常每个 {@link Channel} 独占一个实例与线程。</p>
 *
 * @deprecated this will be remove in the next-major release.
 */
@Deprecated
public class ThreadPerChannelEventLoop extends SingleThreadEventLoop {

    /** 所属的 {@link ThreadPerChannelEventLoopGroup} */
    private final ThreadPerChannelEventLoopGroup parent;
    /** 当前绑定到此 loop 的 {@link Channel}，注销后置为 {@code null} */
    private Channel ch;

    /** 创建并绑定到指定 {@link ThreadPerChannelEventLoopGroup} 的 loop */
    public ThreadPerChannelEventLoop(ThreadPerChannelEventLoopGroup parent) {
        super(parent, parent.executor, true);
        this.parent = parent;
    }

    /** 注册成功后记录 channel，失败则从 group 空闲池回收本 loop */
    @Override
    public ChannelFuture register(ChannelPromise promise) {
        return super.register(promise).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ch = future.channel();
                } else {
                    deregister();
                }
            }
        });
    }

    @Deprecated
    @Override
    public ChannelFuture register(Channel channel, ChannelPromise promise) {
        return super.register(channel, promise).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ch = future.channel();
                } else {
                    deregister();
                }
            }
        });
    }

    /** 处理任务并在关闭时关闭 channel；未注册时自动 deregister 回 idle 池 */
    @Override
    protected void run() {
        for (;;) {
            Runnable task = takeTask();
            if (task != null) {
                task.run();
                updateLastExecutionTime();
            }

            Channel ch = this.ch;
            if (isShuttingDown()) {
                if (ch != null) {
                    ch.unsafe().close(ch.unsafe().voidPromise());
                }
                if (confirmShutdown()) {
                    break;
                }
            } else {
                if (ch != null) {
                    // Handle deregistration — channel 已注销时 drain 任务并归还 idle 池
                    if (!ch.isRegistered()) {
                        runAllTasks();
                        deregister();
                    }
                }
            }
        }
    }

    /** 解除 channel 绑定，从 active 移至 idle 子 loop 集合 */
    protected void deregister() {
        ch = null;
        parent.activeChildren.remove(this);
        parent.idleChildren.add(this);
    }

    /** 每 loop 至多绑定一个 channel */
    @Override
    public int registeredChannels() {
        return 1;
    }
}
