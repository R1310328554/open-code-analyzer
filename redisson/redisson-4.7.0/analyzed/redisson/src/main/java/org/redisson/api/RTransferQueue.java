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
package org.redisson.api;

import java.util.List;
import java.util.concurrent.TransferQueue;

/**
 * 基于 Redis 的 {@link java.util.concurrent.TransferQueue} 同步 API；支持阻塞入队与零缓冲元素转移（生产者直接交给等待中的消费者）。
 *
 * @author Nikita Koksharov
 *
 */
public interface RTransferQueue<V> extends TransferQueue<V>, RBlockingQueue<V>, RTransferQueueAsync<V> {

    /**
     * 一次性返回队列全部元素
     *
     * @return 队列元素列表
     */
    List<V> readAll();

}
