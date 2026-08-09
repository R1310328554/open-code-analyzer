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
import io.reactivex.rxjava3.processors.ReplayProcessor;
import org.redisson.api.options.ObjectParams;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.command.CommandAsyncService;
import org.redisson.connection.ConnectionManager;
import org.redisson.liveobject.core.RedissonObjectBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link CommandRxExecutor} 默认实现，继承 {@link CommandAsyncService}。
 * <p>
 * {@link #flowable} 在 doOnRequest 时调用 supplier，用 {@link ReplayProcessor} 发射
 * 至多一个元素；取消订阅时 cancel 底层 CompletableFuture。
 *
 * @author Nikita Koksharov
 *
 */
public class CommandRxService extends CommandAsyncService implements CommandRxExecutor {

    CommandRxService(CommandAsyncExecutor executor, boolean trackChanges) {
        super(executor, trackChanges);
    }

    CommandRxService(ConnectionManager connectionManager, RedissonObjectBuilder objectBuilder) {
        super(connectionManager, objectBuilder, RedissonObjectBuilder.ReferenceType.RXJAVA);
    }

    CommandRxService(CommandAsyncExecutor executor, ObjectParams objectParams) {
        super(executor, objectParams);
    }

    @Override
    public CommandRxExecutor copy(boolean trackChanges) {
        return new CommandRxService(this, trackChanges);
    }

    @Override
    public CommandRxExecutor copy(ObjectParams objectParams) {
        return new CommandRxService(this, objectParams);
    }

    /** Request 驱动：supplier 在首次 request 时执行，结果 onNext 后 onComplete；取消则中断 future。 */
    @Override
    public <R> Flowable<R> flowable(Callable<CompletionStage<R>> supplier) {
        // ReplayProcessor 缓存至多一个结果；futureRef 供 doOnCancel 中断
        ReplayProcessor<R> p = ReplayProcessor.create();
        AtomicReference<CompletionStage<R>> futureRef = new AtomicReference<>();
        return p.doOnRequest(t -> {
                    CompletionStage<R> future;
                    try {
                        future = supplier.call();
                        futureRef.set(future);
                    } catch (Exception e) {
                        p.onError(e);
                        return;
                    }

                    future.whenComplete((res, e) -> {
                       if (e != null) {
                           // 解包 CompletionException 以传递真实 cause
                           if (e instanceof CompletionException) {
                               e = e.getCause();
                           }
                           p.onError(e);
                           return;
                       }

                       if (res != null) {
                           p.onNext(res);
                       }
                       p.onComplete();
                    });
                }).doOnCancel(() -> {
                    CompletionStage<R> future = futureRef.get();
                    if (future != null) {
                        future.toCompletableFuture().cancel(true);
                    }
                });
    }

}
