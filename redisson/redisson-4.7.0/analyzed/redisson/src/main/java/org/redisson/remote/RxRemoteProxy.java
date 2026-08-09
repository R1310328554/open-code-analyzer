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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.executor.RemotePromise;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.rx.CommandRxExecutor;

import java.util.Arrays;
import java.util.List;

/**
 * RxJava 3 风格的远程服务动态代理：
 * 继承 {@link AsyncRemoteProxy}，将远程调用结果包装为
 * {@link Completable}、{@link Single} 或 {@link Maybe}。
 * <p>
 * 若传入的 {@link CommandAsyncExecutor} 非 {@link CommandRxExecutor}，
 * 会自动包装以支持 {@link Flowable} 适配。
 *
 * @author Nikita Koksharov
 *
 */
public class RxRemoteProxy extends AsyncRemoteProxy {

    /** 构造 Rx 远程代理，必要时将执行器转为 {@link CommandRxExecutor}。 */
    public RxRemoteProxy(CommandAsyncExecutor commandExecutor, String name, String responseQueueName,
                        Codec codec, String executorId, String cancelRequestMapName, BaseRemoteService remoteService) {
        super(convert(commandExecutor), name, responseQueueName, codec, executorId, cancelRequestMapName, remoteService);
    }

    /** 确保底层执行器支持 Rx Flowable 适配。 */
    private static CommandAsyncExecutor convert(CommandAsyncExecutor commandExecutor) {
        if (commandExecutor instanceof CommandRxExecutor) {
            return commandExecutor;
        }
        return CommandRxExecutor.create(commandExecutor.getConnectionManager(), commandExecutor.getObjectBuilder());
    }

    /** 允许作为返回类型的 Rx 类型：Completable、Single、Maybe。 */
    @Override
    protected List<Class<?>> permittedClasses() {
        return Arrays.asList(Completable.class, Single.class, Maybe.class);
    }
    
    /** 将 {@link RemotePromise} 转为对应 Rx 类型。 */
    @Override
    protected Object convertResult(RemotePromise<Object> result, Class<?> returnType) {
        // 以 RemotePromise 为源的 Flowable
        Flowable<Object> flowable = ((CommandRxExecutor) commandExecutor).flowable(() -> new CompletableFutureWrapper<>(result));
        
        // void 语义：忽略元素仅等待完成
        if (returnType == Completable.class) {
            return flowable.ignoreElements();
        }
        // 必须有且仅有一个元素
        if (returnType == Single.class) {
            return flowable.singleOrError();
        }
        // 0 或 1 个元素 → Maybe
        return flowable.singleElement();
    }
    
}
