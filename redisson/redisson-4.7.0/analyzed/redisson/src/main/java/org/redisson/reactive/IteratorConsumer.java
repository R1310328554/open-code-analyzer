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

import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.client.RedisClient;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

/**
 * SCAN/SSCAN 等迭代器的 Reactor 背压基类：
 * {@link #accept(long)} 累加 request 数并驱动 {@link #nextValues()} 链式 scan。
 * 子类实现 {@link #scanIterator} 与 {@link #tryAgain()}。
 *
 * @author Nikita Koksharov
 */
public abstract class IteratorConsumer<V> implements LongConsumer {

    /** 下游 Flux sink。 */
    private final FluxSink<V> emitter;

    /** SCAN 游标，"0" 表示新一轮。 */
    private String nextIterPos = "0";
    /** 当前 scan 绑定的 Redis 节点客户端。 */
    private RedisClient client;

    /** 尚未 emit 的下游请求计数。 */
    private final AtomicLong requested = new AtomicLong();

    public IteratorConsumer(FluxSink<V> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void accept(long value) {
        if (requested.addAndGet(value) == value) {
            nextValues();
        }
    }

    /** 发起一次 scanIterator，emit 元素并决定是否继续。 */
    protected void nextValues() {
        scanIterator(client, nextIterPos).whenComplete((res, e) -> {
            if (e != null) {
                emitter.error(e);
                return;
            }

            client = res.getRedisClient();
            nextIterPos = res.getPos();

            for (Object val : res.getValues()) {
                Object v = transformValue(val);
                emitter.next((V) v);
                requested.decrementAndGet();
            }

            // 游标归零且子类不再重试则完成
            if ("0".equals(nextIterPos) && !tryAgain()) {
                emitter.complete();
                return;
            }

            nextValues();
        });
    }

    /** 子类可覆写以解码/转换 scan 返回值。 */
    protected Object transformValue(Object value) {
        return value;
    }

    /** 游标为 0 时是否继续 scan（如多 key 分片）。 */
    protected abstract boolean tryAgain();

    /** 执行具体 SCAN 命令并返回下一游标与批次值。 */
    protected abstract RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos);

}
