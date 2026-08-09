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
package org.redisson.rx;

import io.reactivex.rxjava3.core.Flowable;
import org.redisson.api.options.ObjectParams;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.ConnectionManager;
import org.redisson.liveobject.core.RedissonObjectBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;

/**
 * RxJava3 命令执行器接口：在 {@link CommandAsyncExecutor} 上增加 {@link Flowable} 适配。
 * <p>
 * {@link #flowable} 将 {@link CompletionStage} 转为背压 {@link Flowable}；
 * {@link #create} 工厂方法返回 {@link CommandRxService} 默认实现。
 *
 * @author Nikita Koksharov
 *
 */
public interface CommandRxExecutor extends CommandAsyncExecutor {

    /** 懒执行 supplier 得到 CompletionStage，并以 Flowable 形式暴露单次结果。 */
    <R> Flowable<R> flowable(Callable<CompletionStage<R>> supplier);

    @Override
    CommandRxExecutor copy(ObjectParams objectParams);

    /** 基于连接管理器与对象构建器创建默认 {@link CommandRxService}。 */
    static CommandRxExecutor create(ConnectionManager connectionManager, RedissonObjectBuilder objectBuilder) {
        return new CommandRxService(connectionManager, objectBuilder);
    }

}
