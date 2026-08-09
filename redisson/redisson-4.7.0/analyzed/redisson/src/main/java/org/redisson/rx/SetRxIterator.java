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

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.ReplayProcessor;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.client.RedisClient;

/**
 * 集合 SSCAN 迭代的 Rx {@link Flowable} 工厂抽象基类。
 * <p>
 * {@link #create()} 创建 {@link ReplayProcessor} 并绑定 {@link RxIteratorConsumer}，
 * 在 {@code doOnRequest} 中按背压驱动 {@link #scanIterator}。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public abstract class SetRxIterator<V> {

    /** 构建带背压的 SSCAN {@link Flowable}。 */
    public Flowable<V> create() {
        ReplayProcessor<V> p = ReplayProcessor.create();
        return p.doOnRequest(new RxIteratorConsumer<V>(p) {
            @Override
            /** 游标结束后是否重试（默认 false）。 */
    protected boolean tryAgain() {
                return SetRxIterator.this.tryAgain();
            }

            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return SetRxIterator.this.scanIterator(client, nextIterPos);
            }
        });
    }
    
    protected boolean tryAgain() {
        return false;
    }

    /** 子类提供具体集合的 SSCAN 异步实现。 */
    protected abstract RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos);

}
