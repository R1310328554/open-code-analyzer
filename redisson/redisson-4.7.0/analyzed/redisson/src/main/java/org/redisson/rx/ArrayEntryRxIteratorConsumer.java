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
import org.redisson.api.RArray;
import org.redisson.api.array.ArrayEntry;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link RArray} 在 RxJava3 侧带背压的分页迭代消费者。
 * <p>
 * 对应 Reactive 版 {@link org.redisson.reactive.ArrayEntryIteratorConsumer}。
 * 数组按稀疏非负下标存值，通过 {@code ARSCAN} 键集分页：每批从上一批末项下标之后拉取，
 * {@code count} 为每页大小提示。
 * <p>
 * 实现 {@link LongConsumer}，在 {@link ReplayProcessor#doOnRequest} 中驱动拉取；
 * {@link #requested} 计数保证未消费完前不会超额 emit。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class ArrayEntryRxIteratorConsumer<V> implements LongConsumer {

    /** 下游订阅者连接的 ReplayProcessor。 */
    private final ReplayProcessor<ArrayEntry<V>> processor;
    /** 底层 RArray 实例。 */
    private final RArray<V> array;
    /** ARSCAN 每批条数提示。 */
    private final int count;

    /** 下一页扫描起始下标。 */
    private long nextStart;
    /** 数组逻辑末下标（length-1），异步解析 length 后赋值。 */
    private long endBound;
    /** 是否已解析过 length。 */
    private boolean endResolved;
    /** 迭代是否已结束。 */
    private boolean finished;

    /** 下游已 request 尚未 onNext 的许可数。 */
    private final AtomicLong requested = new AtomicLong();

    public ArrayEntryRxIteratorConsumer(ReplayProcessor<ArrayEntry<V>> processor, RArray<V> array, int count) {
        this.processor = processor;
        this.array = array;
        this.count = count;
    }

    @Override
    public void accept(long value) {
        // 单链守卫：仅当 prior requested==0 时 addAndGet 返回值等于本次增量，才启动 nextValues 链
        if (requested.addAndGet(value) == value) {
            nextValues();
        }
    }

    /** 递归异步拉页：先 resolve length，再 scanAsync，按 requested 逐条 onNext。 */
    private void nextValues() {
        if (finished) {
            processor.onComplete();
            return;
        }
        if (!endResolved) {
            array.lengthAsync().whenComplete((len, e) -> {
                if (e != null) {
                    processor.onError(e);
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
            processor.onComplete();
            return;
        }
        array.scanAsync(nextStart, endBound, count).whenComplete((page, e) -> {
            if (e != null) {
                processor.onError(e);
                return;
            }
            if (page.isEmpty()) {
                finished = true;
                processor.onComplete();
                return;
            }
            for (ArrayEntry<V> entry : page) {
                processor.onNext(entry);
                requested.decrementAndGet();
            }
            nextStart = lastIndex(page) + 1;
            nextValues();
        });
    }

    /** 取本页最后一条的下标，作为下一页 nextStart 的前驱。 */
    private long lastIndex(List<ArrayEntry<V>> page) {
        return page.get(page.size() - 1).getIndex();
    }

}
