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

import java.util.function.Consumer;

/**
 * 基于 Redis SCAN 的响应式 Set/集合迭代器抽象基类。
 * <p>
 * 实现 {@link Consumer} 供 {@link reactor.core.publisher.Flux#create} 使用；
 * 在 {@code onRequest} 时委托 {@link IteratorConsumer} 分页拉取。
 * 子类只需实现 {@link #scanIterator} 提供具体 SCAN 命令。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 迭代元素类型
 */
public abstract class SetReactiveIterator<V> implements Consumer<FluxSink<V>> {

    /** 注册背压驱动的 SCAN 迭代消费逻辑。 */
    @Override
    public void accept(FluxSink<V> emitter) {
        emitter.onRequest(new IteratorConsumer<V>(emitter) {
            @Override
            /** 子类可覆盖：SCAN 遇空时是否重试（默认 false）。 */
    protected boolean tryAgain() {
                return SetReactiveIterator.this.tryAgain();
            }

            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return SetReactiveIterator.this.scanIterator(client, nextIterPos);
            }
        });
    }
    
    protected boolean tryAgain() {
        return false;
    }
    
    /** 发起一次 SCAN，返回游标与批次数据。 */
    protected abstract RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos);

}
