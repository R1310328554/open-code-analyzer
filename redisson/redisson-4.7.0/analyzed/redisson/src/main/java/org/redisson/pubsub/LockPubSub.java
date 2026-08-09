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
 * 分布式锁的 Pub/Sub 通知：当 Redis 侧锁释放时通过频道推送 Long 消息，
 * 唤醒本地 {@link org.redisson.RedissonLockEntry} 上等待的线程。
 * <p>
 * {@link #UNLOCK_MESSAGE} 为写锁/独占锁释放；
 * {@link #READ_UNLOCK_MESSAGE} 为读锁批量释放。
 *
 * @author Nikita Koksharov
 *
 */
public class LockPubSub extends PublishSubscribe<RedissonLockEntry> {

    /** 写锁或独占锁释放通知。 */
    public static final Long UNLOCK_MESSAGE = 0L;
    /** 读锁释放通知（可能一次唤醒多个等待者）。 */
    public static final Long READ_UNLOCK_MESSAGE = 1L;

    /** @param service Pub/Sub 服务 */
    public LockPubSub(PublishSubscribeService service) {
        super(service);
    }
    
    /** 创建锁等待条目。 */
    @Override
    protected RedissonLockEntry createEntry(CompletableFuture<RedissonLockEntry> newPromise) {
        return new RedissonLockEntry(newPromise);
    }

    /** 根据消息类型唤醒一个或全部等待线程。 */
    @Override
    protected void onMessage(RedissonLockEntry value, Long message) {
        // 独占释放：运行单个 listener 并 release 一次
        if (message.equals(UNLOCK_MESSAGE)) {
            value.tryRunListener();

            value.getLatch().release();
        // 读锁释放：运行全部 listener，按队列长度批量 release
        } else if (message.equals(READ_UNLOCK_MESSAGE)) {
            value.tryRunAllListeners();

            value.getLatch().release(value.getLatch().getQueueLength());
        }
    }

}
