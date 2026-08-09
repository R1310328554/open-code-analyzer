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
package org.redisson.remote;

import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.executor.RemotePromise;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.reactive.CommandReactiveExecutor;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Reactor 风格远程服务代理：方法返回 {@link Mono}。
 * <p>
 * 继承 {@link AsyncRemoteProxy}，将 {@link CommandAsyncExecutor} 转为
 * {@link CommandReactiveExecutor}，{@link #convertResult} 包装为 Mono。
 * <p>
 * 由 {@link BaseRemoteService#get} 在 {@link RRemoteReactive} 注解时创建。
 *
 * @author Nikita Koksharov
 *
 */
public class ReactiveRemoteProxy extends AsyncRemoteProxy {

    /** 必要时将 executor 包装为 {@link CommandReactiveExecutor}。 */
    public ReactiveRemoteProxy(CommandAsyncExecutor commandExecutor, String name, String responseQueueName,
                                Codec codec, String executorId, String cancelRequestMapName, BaseRemoteService remoteService) {
        super(convert(commandExecutor), name, responseQueueName, codec, executorId, cancelRequestMapName, remoteService);
    }

    /** 已是 ReactiveExecutor 则直接返回，否则 create 包装。 */
    private static CommandAsyncExecutor convert(CommandAsyncExecutor commandExecutor) {
        if (commandExecutor instanceof CommandReactiveExecutor) {
            return commandExecutor;
        }
        return CommandReactiveExecutor.create(commandExecutor.getConnectionManager(), commandExecutor.getObjectBuilder());
    }

    /** 响应式接口仅允许 Mono 返回类型。 */
    @Override
    protected List<Class<?>> permittedClasses() {
        return Arrays.asList(Mono.class);
    }

    /** 通过 {@link CommandReactiveExecutor#reactive} 将 RemotePromise 转为 Mono。 */
    @Override
    protected Object convertResult(RemotePromise<Object> result, Class<?> returnType) {
        return ((CommandReactiveExecutor) commandExecutor).reactive(() -> new CompletableFutureWrapper<>(result));
    }
    
}
