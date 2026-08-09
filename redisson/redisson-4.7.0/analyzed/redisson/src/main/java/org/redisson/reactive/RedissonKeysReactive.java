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

import org.reactivestreams.Publisher;
import org.redisson.RedissonKeys;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.api.RType;
import org.redisson.api.options.KeysScanOptions;
import org.redisson.api.options.KeysScanParams;
import org.redisson.client.RedisClient;
import org.redisson.connection.MasterSlaveEntry;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link org.redisson.api.RKeysReactive} 的 Reactor 实现：跨分片 SCAN 键空间。
 * <p>
 * 对每个 {@link MasterSlaveEntry} 创建独立 {@link Flux}，最终 {@link Flux#merge} 合并；
 * 支持模式、chunk 大小与 {@link RType} 过滤。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonKeysReactive {

    /** Reactor 命令执行上下文。 */
    private final CommandReactiveExecutor commandExecutor;

    /** 同步键 API 委托对象（scanIteratorAsync）。 */
    private final RedissonKeys instance;

    /** 绑定执行器并构造内部 {@link RedissonKeys}。 */
    public RedissonKeysReactive(CommandReactiveExecutor commandExecutor) {
        super();
        instance = new RedissonKeys(commandExecutor);
        this.commandExecutor = commandExecutor;
    }

    /** 全库键流，默认 chunk=10。 */
    public Flux<String> getKeys() {
        return getKeysByPattern(null);
    }

    /** 全库键流，指定每批 SCAN 数量。 */
    public Flux<String> getKeys(int count) {
        return getKeysByPattern(null, count);
    }

    /** 按 glob 模式扫描键，默认 chunk=10。 */
    public Flux<String> getKeysByPattern(String pattern) {
        return getKeysByPattern(pattern, 10);
    }

    /** 按模式与 chunk 大小扫描键。 */
    public Flux<String> getKeysByPattern(String pattern, int count) {
        return getKeys(KeysScanOptions.defaults().pattern(pattern).chunkSize(count));
    }

    /** 按 {@link KeysScanOptions} 在各分片上 SCAN 并 merge 为单一 Flux。 */
    public Flux<String> getKeys(KeysScanOptions options) {
        KeysScanParams params = (KeysScanParams) options;
        List<Publisher<String>> publishers = new ArrayList<>();
        for (MasterSlaveEntry entry : commandExecutor.getConnectionManager().getEntrySet()) {
            publishers.add(createKeysIterator(entry, params.getPattern(), params.getChunkSize(), params.getType()));
        }
        return Flux.merge(publishers);
    }

    /** 为单个分片槽位创建背压驱动的键 SCAN Flux。 */
    private Flux<String> createKeysIterator(MasterSlaveEntry entry, String pattern, int count, RType type) {
        return Flux.create(emitter -> emitter.onRequest(new IteratorConsumer<String>(emitter) {

            @Override
            protected boolean tryAgain() {
                return false;
            }

            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return instance.scanIteratorAsync(client, entry, nextIterPos, pattern, count, type);
            }
        }));
    }

}
