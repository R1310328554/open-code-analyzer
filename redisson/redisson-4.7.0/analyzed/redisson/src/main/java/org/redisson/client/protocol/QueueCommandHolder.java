/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client.protocol;

import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.channel.ChannelPromise;

/**
 * 队列中待发送命令的包装，关联 Netty {@link ChannelPromise} 与发送标志。
 * <p>
 * {@link #trySend()} 保证同一命令仅写入通道一次。
 *
 * @author Nikita Koksharov
 *
 */
public class QueueCommandHolder {

    /** 是否已向通道写出（CAS 防重复发送）。 */
    final AtomicBoolean sent = new AtomicBoolean();
    /** 写入完成时触发的 Netty Promise。 */
    final ChannelPromise channelPromise;
    /** 待发送的队列命令。 */
    final QueueCommand command;

    /** 绑定队列命令与通道写入 Promise。 */
    public QueueCommandHolder(QueueCommand command, ChannelPromise channelPromise) {
        super();
        this.command = command;
        this.channelPromise = channelPromise;
    }

    public QueueCommand getCommand() {
        return command;
    }

    public ChannelPromise getChannelPromise() {
        return channelPromise;
    }

    /** 原子地将发送标志从 false 置为 true，成功表示可发送。 */
    public boolean trySend() {
        return sent.compareAndSet(false, true);
    }

    @Override
    public String toString() {
        return "QueueCommandHolder [command=" + command + "]";
    }

}
