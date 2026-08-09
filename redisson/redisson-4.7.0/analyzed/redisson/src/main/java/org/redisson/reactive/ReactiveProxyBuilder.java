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

import org.redisson.api.annotation.EmptyAsAbsent;
import org.redisson.misc.ProxyBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;

/**
 * 将 Redisson 同步/异步对象动态代理为 Reactor {@link Mono}/{@link Flux} API。
 * <p>
 * 通过 {@link ProxyBuilder} 拦截方法调用，经 {@link CommandReactiveExecutor#reactive}
 * 包装为 Mono；返回 Flux 的方法自动 flatMapMany 展开 Iterable。
 * {@link EmptyAsAbsent} 方法会过滤空 Map/Collection。
 *
 * @author Nikita Koksharov
 *
 */
public class ReactiveProxyBuilder {

    /** 无自定义实现类时创建响应式代理。 */
    public static <T> T create(CommandReactiveExecutor commandExecutor, Object instance, Class<T> clazz) {
        return create(commandExecutor, instance, null, clazz);
    }

    /** 创建响应式代理，{@code implementation} 提供需显式覆盖的方法体。 */
    public static <T> T create(CommandReactiveExecutor commandExecutor, Object instance, Object implementation, Class<T> clazz) {
        return ProxyBuilder.create((callable, instanceMethod) -> {
            Mono<Object> result = commandExecutor.reactive((Callable<CompletionStage<Object>>) (Object) callable);
            // 返回 Flux 时：Mono<Iterable> → flatMapMany 展开为流
            if (instanceMethod.getReturnType().isAssignableFrom(Flux.class)) {
                Mono<Iterable> monoListResult = result.cast(Iterable.class);
                return monoListResult.flatMapMany(Flux::fromIterable);
            }
            // @EmptyAsAbsent：空 Map/Collection 视为 absent，不向下游 emit
            if (instanceMethod.isAnnotationPresent(EmptyAsAbsent.class)) {
                return result.filter(v ->
                        !(v instanceof Map && ((Map<?, ?>) v).isEmpty())
                            && !(v instanceof Collection && ((Collection<?>) v).isEmpty()));
            }
            return result;
        }, instance, implementation, clazz, commandExecutor.getServiceManager());
    }
    
}
