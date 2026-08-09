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

import io.reactivex.rxjava3.functions.LongConsumer;
import io.reactivex.rxjava3.processors.ReplayProcessor;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.client.RedisClient;

import java.util.concurrent.atomic.AtomicLong;

/**
 * RxJava3 侧带背压的 SCAN 游标消费者（共享基类）。
 * <p>
 * 对应 Reactive 版 {@link org.redisson.reactive.IteratorConsumer}。
 * 通过 {@link ReplayProcessor} 驱动 SCAN/HSCAN/SSCAN 分页直至游标为 {@code "0"}；
 * {@link #requested} 与单链守卫保证并发 {@code request} 时仅一条拉取链在飞。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public abstract class RxIteratorConsumer<V> implements LongConsumer {

    /** 向下游发射扫描结果的 ReplayProcessor。 */
    private final ReplayProcessor<V> processor;

    /** 当前 SCAN 游标位置。 */
    private String nextIterPos = "0";
    /** 上次扫描绑定的 Redis 连接客户端（集群槽位亲和）。 */
    private RedisClient client;

    /** 下游已 request、尚未 onNext 的许可计数。 */
    private final AtomicLong requested = new AtomicLong();

    public RxIteratorConsumer(ReplayProcessor<V> processor) {
        this.processor = processor;
    }

    @Override
    public void accept(long value) {
        // 单链守卫：仅当计数器由 0 增至 value 时启动拉取链；并发 request 只累加计数，由在飞链按需消费
        if (requested.addAndGet(value) == value) {
            nextValues();
        }
    }

    /** 发起一轮 scanIterator，解析结果后 onNext/onComplete/onError。 */
    protected void nextValues() {
        scanIterator(client, nextIterPos).whenComplete((res, e) -> {
            if (e != null) {
                processor.onError(e);
                return;
            }

            client = res.getRedisClient();
            nextIterPos = res.getPos();

            for (Object val : res.getValues()) {
                Object v = transformValue(val);
                processor.onNext((V) v);
                requested.decrementAndGet();
            }

            if ("0".equals(nextIterPos) && !tryAgain()) {
                processor.onComplete();
                return;
            }

            nextValues();
        });
    }

    /** 子类可覆写：将 Redis 原始值转为下游类型（默认原样返回）。 */
    protected Object transformValue(Object value) {
        return value;
    }

    /** 游标归零后是否再扫一轮（如 Set 在事务态下可能有新增）。 */
    protected abstract boolean tryAgain();

    /** 子类实现具体 SCAN/HSCAN/SSCAN 异步调用。 */
    protected abstract RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos);

}
