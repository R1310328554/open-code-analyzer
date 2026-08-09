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

import java.util.concurrent.CompletableFuture;

/**
 * Pub/Sub 订阅条目的引用计数与完成信号。
 * <p>{@link #acquire}/{@link #release} 管理并发订阅；{@link #getPromise} 在订阅就绪时完成。
 *
 * @author Nikita Koksharov
 */
public interface PubSubEntry<E> {

    /** 增加一次引用（默认 1）。 */
    void acquire();

    /** 增加指定数量的引用。 */
    void acquire(int permits);

    /** 释放引用并返回剩余计数。 */
    int release();

    /** 订阅完成或失败时完成的 Future。 */
    CompletableFuture<E> getPromise();

}
