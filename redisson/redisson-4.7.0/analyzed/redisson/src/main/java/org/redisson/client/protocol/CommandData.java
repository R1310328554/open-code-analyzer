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

import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.decoder.MultiDecoder;
import org.redisson.misc.LogHelper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * 单条 Redis 命令的队列元素：命令、参数、编解码器与异步 Promise。
 * <p>
 * 实现 {@link QueueCommand}，供连接层排队发送与解码响应。
 *
 * @author Nikita Koksharov
 *
 * @param <T> input type
 * @param <R> output type
 */
public class CommandData<T, R> implements QueueCommand {

    /** 命令结果的异步 Future。 */
    final CompletableFuture<R> promise;
    /** Redis 命令定义（含解码器）。 */
    RedisCommand<T> command;
    /** 命令参数数组。 */
    final Object[] params;
    /** 参数与返回值编解码器。 */
    final Codec codec;
    /** 可选的消息级多段解码器。 */
    final MultiDecoder<Object> messageDecoder;

    /** 使用默认消息解码器构造命令数据。 */
    public CommandData(CompletableFuture<R> promise, Codec codec, RedisCommand<T> command, Object[] params) {
        this(promise, null, codec, command, params);
    }

    /** 指定自定义消息解码器构造命令数据。 */
    public CommandData(CompletableFuture<R> promise, MultiDecoder<Object> messageDecoder, Codec codec, RedisCommand<T> command, Object[] params) {
        this.promise = promise;
        this.command = command;
        this.params = params;
        this.codec = codec;
        this.messageDecoder = messageDecoder;
    }

    public RedisCommand<T> getCommand() {
        return command;
    }

    public Object[] getParams() {
        return params;
    }

    public MultiDecoder<Object> getMessageDecoder() {
        return messageDecoder;
    }

    /** 返回命令结果的 Promise。 */
    public CompletableFuture<R> getPromise() {
        return promise;
    }
    
    public Throwable cause() {
        try {
            promise.getNow(null);
            return null;
        } catch (CompletionException e) {
            return e.getCause();
        } catch (CancellationException e) {
            return e;
        }
    }

    public boolean isSuccess() {
        return promise.isDone() && !promise.isCompletedExceptionally();
    }

    /** 以异常完成 Promise。 */
    public boolean tryFailure(Throwable cause) {
        return promise.completeExceptionally(cause);
    }

    public Codec getCodec() {
        return codec;
    }

    @Override
    public String toString() {
        return "CommandData [command=" + LogHelper.toString(this) + ", codec=" + codec + "]";
    }

    @Override
    public List<CommandData<Object, Object>> getPubSubOperations() {
        if (RedisCommands.PUBSUB_COMMANDS.contains(getCommand().getName())) {
            return Collections.singletonList((CommandData<Object, Object>) this);
        }
        return Collections.emptyList();
    }
    
    /** 判断是否为阻塞类命令（如 BLPOP、XREAD BLOCK）。 */
    public boolean isBlockingCommand() {
        return command.isBlockingCommand();
    }

    /** 命令是否已完成（Promise 已结束）。 */
    @Override
    public boolean isExecuted() {
        return promise.isDone();
    }

}
