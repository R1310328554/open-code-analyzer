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
package org.redisson.client.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import org.redisson.client.WriteRedisConnectionException;
import org.redisson.client.protocol.QueueCommand;
import org.redisson.client.protocol.QueueCommandHolder;
import org.redisson.misc.LogHelper;
import org.redisson.misc.SpinLock;

import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 普通 Redis 连接的出站命令队列处理器。
 * <p>
 * 保证同一 Channel 上命令按序发送，并在连接关闭时失败未完成命令。
 *
 * @author Nikita Koksharov
 *
 */
public class CommandsQueue extends ChannelDuplexHandler {

    /** Channel 属性键，存储待发送/待回复的命令双端队列。 */
    public static final AttributeKey<Deque<QueueCommandHolder>> COMMANDS_QUEUE = AttributeKey.valueOf("COMMANDS_QUEUE");

    /** 注册 Channel 时初始化命令队列。 */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        ctx.channel().attr(COMMANDS_QUEUE).set(new ConcurrentLinkedDeque<>());
    }

    /** 连接断开时移除非阻塞命令并以异常完成其 Promise。 */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Queue<QueueCommandHolder> queue = ctx.channel().attr(COMMANDS_QUEUE).get();
        Iterator<QueueCommandHolder> iterator = queue.iterator();
        while (iterator.hasNext()) {
            QueueCommandHolder command = iterator.next();
            if (command.getCommand().isBlockingCommand()) {
                continue;
            }

            iterator.remove();
            command.getChannelPromise().tryFailure(
                    new WriteRedisConnectionException("Channel has been closed! Can't write command: "
                                + LogHelper.toString(command.getCommand()) + " to channel: " + ctx.channel()));
        }

        super.channelInactive(ctx);
    }

    private final SpinLock lock = new SpinLock();

    /** 将 {@link QueueCommand} 入队并串行 writeAndFlush。 */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof QueueCommand) {
            QueueCommand data = (QueueCommand) msg;
            QueueCommandHolder holder = new QueueCommandHolder(data, promise);
            Queue<QueueCommandHolder> queue = ctx.channel().attr(COMMANDS_QUEUE).get();

            holder.getChannelPromise().addListener(future -> {
                if (!future.isSuccess()) {
                    queue.remove(holder);
                }
            });

            lock.executeInterruptibly(() -> {
                try {
                    queue.add(holder);
                    ctx.writeAndFlush(data, holder.getChannelPromise());
                } catch (Exception e) {
                    queue.remove(holder);
                    throw e;
                }
            });
        } else {
            super.write(ctx, msg, promise);
        }
    }

}
