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
 * Shared backpressure-aware SCAN-cursor consumer for the RxJava3 facade.
 *
 * Rx counterpart of {@link org.redisson.reactive.IteratorConsumer}. Drives a
 * cursor-paginated SCAN/HSCAN/SSCAN to completion through a {@link ReplayProcessor},
 * advancing the cursor until Redis returns "0", with a single in-flight chain
 * regardless of how many times {@code accept(long)} is invoked by upstream
 * request replenishment (e.g. from {@link io.reactivex.rxjava3.core.Flowable#merge}).
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public abstract class RxIteratorConsumer<V> implements LongConsumer {

    private final ReplayProcessor<V> processor;

    private String nextIterPos = "0";
    private RedisClient client;

    private final AtomicLong requested = new AtomicLong();

    public RxIteratorConsumer(ReplayProcessor<V> processor) {
        this.processor = processor;
    }

    @Override
    public void accept(long value) {
        // Single-chain guard: addAndGet(value) == value iff prior counter was 0,
        // i.e. no chain is currently running. Concurrent requests just bump the
        // counter; the in-flight chain reads it as it goes.
        if (requested.addAndGet(value) == value) {
            nextValues();
        }
    }

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

    protected Object transformValue(Object value) {
        return value;
    }

    protected abstract boolean tryAgain();

    protected abstract RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos);

}
