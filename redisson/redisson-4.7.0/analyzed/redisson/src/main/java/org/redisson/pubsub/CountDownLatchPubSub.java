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
package org.redisson.pubsub;

import org.redisson.RedissonCountDownLatchEntry;

import java.util.concurrent.CompletableFuture;

/**
 * {@link org.redisson.RedissonCountDownLatch} 的 Pub/Sub 监听器：
 * 订阅 Redis 频道上的 Long 消息，驱动本地 {@link org.redisson.misc.CountDownLatch}。
 * <p>
 * {@link #ZERO_COUNT_MESSAGE} 表示计数归零（开门）；
 * {@link #NEW_COUNT_MESSAGE} 表示重新计数（关门）。
 *
 * @author Nikita Koksharov
 *
 */
public class CountDownLatchPubSub extends PublishSubscribe<RedissonCountDownLatchEntry> {

    /** Pub/Sub 消息：计数已为 0，触发等待线程并 open latch。 */
    public static final Long ZERO_COUNT_MESSAGE = 0L;
    /** Pub/Sub 消息：重新设置计数，close latch。 */
    public static final Long NEW_COUNT_MESSAGE = 1L;
    
    /** @param service 全局 Pub/Sub 调度服务 */
    public CountDownLatchPubSub(PublishSubscribeService service) {
        super(service);
    }

    /** 为新的 CountDownLatch 订阅创建条目。 */
    @Override
    protected RedissonCountDownLatchEntry createEntry(CompletableFuture<RedissonCountDownLatchEntry> newPromise) {
        return new RedissonCountDownLatchEntry(newPromise);
    }

    /** 处理频道 Long 消息：归零时执行监听器并 open；新计数时 close。 */
    @Override
    protected void onMessage(RedissonCountDownLatchEntry value, Long message) {
        // 计数归零：依次执行排队 Runnable 并释放 latch
        if (message.equals(ZERO_COUNT_MESSAGE)) {
            Runnable runnableToExecute = value.getListeners().poll();
            while (runnableToExecute != null) {
                runnableToExecute.run();
                runnableToExecute = value.getListeners().poll();
            }

            value.getLatch().open();
        }
        // 重新计数：关闭 latch 阻塞 await
        if (message.equals(NEW_COUNT_MESSAGE)) {
            value.getLatch().close();
        }
    }

}
