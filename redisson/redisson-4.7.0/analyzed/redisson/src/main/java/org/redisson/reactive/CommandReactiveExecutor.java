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
package org.redisson.reactive;

import org.redisson.api.options.ObjectParams;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.ConnectionManager;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;

/**
 * 响应式命令执行器：在 {@link CommandAsyncExecutor} 基础上提供
 * {@link Mono} 包装的 {@link #reactive}，供 RReactive* 对象使用。
 *
 * @author Nikita Koksharov
 *
 */
public interface CommandReactiveExecutor extends CommandAsyncExecutor {

    /** 将 CompletionStage 工厂包装为背压感知的 Mono。 */
    <R> Mono<R> reactive(Callable<CompletionStage<R>> supplier);

    @Override
    CommandReactiveExecutor copy(ObjectParams objectParams);

    /** 工厂：创建默认 {@link CommandReactiveService} 实例。 */
    static CommandReactiveExecutor create(ConnectionManager connectionManager, RedissonObjectBuilder objectBuilder) {
        return new CommandReactiveService(connectionManager, objectBuilder);
    }

}
