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

import org.redisson.api.RArray;
import org.redisson.api.array.ArrayEntry;
import reactor.core.publisher.FluxSink;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

/**
 * Reactor 侧 {@link RArray} 迭代的背压感知分页消费者。
 * <p>
 * 数组按稀疏非负索引存值，通过 {@code ARSCAN} 键集分页：每批从上一批末索引之后拉取，
 * {@code count} 为页大小提示。无论上游 {@code accept(long)} 调用多少次，
 * 仅保持一条在途异步链，避免并发 scan。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class ArrayEntryIteratorConsumer<V> implements LongConsumer {

    /** Reactor 下游 sink。 */
    private final FluxSink<ArrayEntry<V>> emitter;
    /** 待迭代的 Redis 数组。 */
    private final RArray<V> array;
    /** ARSCAN 每批 hint 数量。 */
    private final int count;

    /** 下一批 scan 起始索引。 */
    private long nextStart;
    /** 数组最大有效索引（length-1）。 */
    private long endBound;
    /** 是否已异步解析 length。 */
    private boolean endResolved;
    /** 迭代是否已结束。 */
    private boolean finished;

    private final AtomicLong requested = new AtomicLong();

    /** @param count ARSCAN 页大小 hint */
    public ArrayEntryIteratorConsumer(FluxSink<ArrayEntry<V>> emitter, RArray<V> array, int count) {
        this.emitter = emitter;
        this.array = array;
        this.count = count;
    }

    @Override
    public void accept(long value) {
        // 单链守卫：仅当 prior requested==0 时启动 nextValues
        // Single-chain guard: addAndGet(value) == value iff prior counter was 0,
        // i.e. no chain is currently running.
        if (requested.addAndGet(value) == value) {
            nextValues();
        }
    }

    /** 异步拉取下一页或完成流。 */
    private void nextValues() {
        if (finished) {
            emitter.complete();
            return;
        }
        // 首次：异步获取数组 length 确定上界
        if (!endResolved) {
            array.lengthAsync().whenComplete((len, e) -> {
                if (e != null) {
                    emitter.error(e);
                    return;
                }
                endBound = len - 1;
                endResolved = true;
                nextValues();
            });
            return;
        }
        if (nextStart > endBound) {
            finished = true;
            emitter.complete();
            return;
        }
        // ARSCAN 分页拉取
        array.scanAsync(nextStart, endBound, count).whenComplete((page, e) -> {
            if (e != null) {
                emitter.error(e);
                return;
            }
            if (page.isEmpty()) {
                finished = true;
                emitter.complete();
                return;
            }
            for (ArrayEntry<V> entry : page) {
                emitter.next(entry);
                requested.decrementAndGet();
            }
            nextStart = lastIndex(page) + 1;
            nextValues();
        });
    }

    /** 取本页最后一条的索引，作为下批起点。 */
    private long lastIndex(List<ArrayEntry<V>> page) {
        return page.get(page.size() - 1).getIndex();
    }

}
