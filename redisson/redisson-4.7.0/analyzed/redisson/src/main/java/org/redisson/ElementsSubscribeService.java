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
package org.redisson;

import org.redisson.connection.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 阻塞队列等元素订阅的循环调度服务。
 * <p>由 {@link org.redisson.connection.ServiceManager} 持有，供 {@link RedissonBlockingQueue} 等
 * 反复调用异步取元素 API 并将结果交给消费者；失败时在非关闭状态下延迟重试。
 *
 * @author Nikita Koksharov
 */
public class ElementsSubscribeService {

    private static final Logger log = LoggerFactory.getLogger(ElementsSubscribeService.class);
    private final Map<Integer, CompletableFuture<?>> subscribeListeners = new ConcurrentHashMap<>();
    private final ServiceManager serviceManager;

    /** @param serviceManager 提供超时调度与关闭状态检测 */
    public ElementsSubscribeService(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    /**
     * 注册异步元素订阅循环（推荐）。
     * <p>{@code func} 持续发起取元素请求，{@code consumer} 处理每个元素并可返回后续 {@link CompletionStage}。
     *
     * @param func 异步取元素供应函数（如 {@code takeAsync}）
     * @param consumer 元素处理函数，勿在其中调用阻塞 API
     * @return 监听器 ID，用于 {@link #unsubscribe(int)}
     */
    public <V> int subscribeOnElements(Supplier<CompletionStage<V>> func, Function<V, CompletionStage<Void>> consumer) {
        int id = System.identityHashCode(consumer);
        CompletableFuture<?> currentFuture = subscribeListeners.putIfAbsent(id, CompletableFuture.completedFuture(null));
        if (currentFuture != null) {
            throw new IllegalArgumentException("Consumer object with listener id " + id + " already registered");
        }
        resubscribe(func, consumer);
        return id;
    }

    /** @deprecated 请改用 {@link #subscribeOnElements(Supplier, Function)} */
    @Deprecated
    public <V> int subscribeOnElements(Supplier<CompletionStage<V>> func, Consumer<V> consumer) {
        int id = System.identityHashCode(consumer);
        CompletableFuture<?> currentFuture = subscribeListeners.putIfAbsent(id, CompletableFuture.completedFuture(null));
        if (currentFuture != null) {
            throw new IllegalArgumentException("Consumer object with listener id " + id + " already registered");
        }
        resubscribe(func, consumer);
        return id;
    }

    /** 取消订阅并中断进行中的异步取元素循环。
     * @param listenerId {@link #subscribeOnElements} 返回的 ID */
    public void unsubscribe(int listenerId) {
        CompletableFuture<?> f = subscribeListeners.remove(listenerId);
        if (f != null) {
            f.cancel(false);
        }
    }

    @Deprecated
    private <V> void resubscribe(Supplier<CompletionStage<V>> func, Consumer<V> consumer) {
        int listenerId = System.identityHashCode(consumer);
        CompletionStage<V> f = (CompletionStage<V>) subscribeListeners.computeIfPresent(listenerId, (k, v) -> {
            return func.get().toCompletableFuture();
        });
        if (f == null) {
            return;
        }

        f.whenComplete((r, e) -> {
            if (e != null) {
                if (serviceManager.isShuttingDown(e)) {
                    return;
                }

                serviceManager.newTimeout(t -> {
                    resubscribe(func, consumer);
                }, 1, TimeUnit.SECONDS);
                return;
            }

            consumer.accept(r);
            resubscribe(func, consumer);
        });
    }

    private <V> void resubscribe(Supplier<CompletionStage<V>> func, Function<V, CompletionStage<Void>> consumer) {
        int listenerId = System.identityHashCode(consumer);
        CompletionStage<V> f = (CompletionStage<V>) subscribeListeners.computeIfPresent(listenerId, (k, v) -> {
            return func.get().toCompletableFuture();
        });
        if (f == null) {
            return;
        }

        f.thenCompose(consumer).whenComplete((r, ex) -> {
            if (ex != null) {
                if (serviceManager.isShuttingDown(ex)) {
                    return;
                }

                log.error(ex.getMessage(), ex);
                serviceManager.newTimeout(t -> {
                    resubscribe(func, consumer);
                }, 1, TimeUnit.SECONDS);
                return;
            }

            resubscribe(func, consumer);
        });
    }


}
