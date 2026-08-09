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

import org.redisson.RedissonLockEntry;

import java.util.concurrent.CompletableFuture;

/**
 * {@link org.redisson.RedissonSemaphore} 的 Pub/Sub 处理器：
 * 收到释放消息后尝试运行 listener，并按消息值（可用许可数）
 * 批量 {@link org.redisson.misc.CountDownLatch#release(int)} 唤醒等待者。
 *
 * @author Nikita Koksharov
 *
 */
public class SemaphorePubSub extends PublishSubscribe<RedissonLockEntry> {

    /** @param service Pub/Sub 服务 */
    public SemaphorePubSub(PublishSubscribeService service) {
        super(service);
    }

    /** 信号量条目与锁共用 RedissonLockEntry 结构。 */
    @Override
    protected RedissonLockEntry createEntry(CompletableFuture<RedissonLockEntry> newPromise) {
        return new RedissonLockEntry(newPromise);
    }

    /** 释放许可：唤醒数不超过本地已 acquire 的 permit 数。 */
    @Override
    protected void onMessage(RedissonLockEntry value, Long message) {
        // 先执行用户 listener，再按 Redis 推送的可用数释放 latch
        value.tryRunListener();

        value.getLatch().release(Math.min(value.acquired(), message.intValue()));
    }

}
