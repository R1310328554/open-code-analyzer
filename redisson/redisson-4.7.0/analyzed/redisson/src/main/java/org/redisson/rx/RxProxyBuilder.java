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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.redisson.misc.ProxyBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;


/**
 * 将 Redisson 异步 API 动态代理为 RxJava3 接口（{@link Single}/{@link Completable}/{@link Flowable}）。
 * <p>
 * 委托 {@link org.redisson.misc.ProxyBuilder}：方法返回类型为 Completable/Single 时
 * 分别映射为 {@code ignoreElements()} 与 {@code singleOrError()}；其余走 Flowable 并过滤空 Map/Collection。
 *
 * @author Nikita Koksharov
 *
 */
public class RxProxyBuilder {

    /** 无额外 implementation 对象的代理工厂入口。 */
    public static <T> T create(CommandRxExecutor commandExecutor, Object instance, Class<T> clazz) {
        return create(commandExecutor, instance, null, clazz);
    }

    /** 将 instance 上的异步方法包装为 Rx 类型并生成 clazz 接口代理。 */
    public static <T> T create(CommandRxExecutor commandExecutor, Object instance, Object implementation, Class<T> clazz) {
        return ProxyBuilder.create((callable, instanceMethod) -> {
            Flowable<Object> flowable = commandExecutor.flowable((Callable<CompletionStage<Object>>) (Object) callable);

            // Completable：丢弃所有元素，只关心完成/错误
            if (instanceMethod.getReturnType() == Completable.class) {
                return flowable.ignoreElements();
            }
            // Single：必须恰好一个元素或错误
            if (instanceMethod.getReturnType() == Single.class) {
                return flowable.singleOrError();
            }
            // 默认 Maybe：过滤空 Map/Collection 后取至多一个元素
            return flowable
                    .filter(v -> !(v instanceof Map && ((Map<?, ?>) v).isEmpty())
                            && !(v instanceof Collection && ((Collection<?>) v).isEmpty()))
                    .singleElement();
        }, instance, implementation, clazz, commandExecutor.getServiceManager());
    }
    
}
