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
package org.redisson.api.queue;

/**
 * {@link QueueMoveElementsArgs} 的可变实现，保存目标队列、数量选择器与转移顺序。
 *
 * @author Nikita Koksharov
 *
 */
public class QueueMoveElementsParams implements QueueMoveElementsArgs,
                                                QueueMoveElementsAmount,
                                                QueueMoveElementsOrder {

    /** 数量选择模式：最多转移或精确转移。 */
    public enum Selector {COUNT, EXACTLY};

    /** 转移顺序：逐条或批量。 */
    public enum Ordering {OBO, BULK};

    private final String destName;
    private Selector selector;
    private int count;
    private Ordering ordering = Ordering.BULK;

    QueueMoveElementsParams(String destName) {
        this.destName = destName;
    }

    @Override
    public QueueMoveElementsOrder count(int value) {
        selector = Selector.COUNT;
        count = value;
        return this;
    }

    @Override
    public QueueMoveElementsOrder exactly(int value) {
        selector = Selector.EXACTLY;
        count = value;
        return this;
    }

    @Override
    public QueueMoveElementsArgs bulk() {
        ordering = Ordering.BULK;
        return this;
    }

    @Override
    public QueueMoveElementsArgs oneByOne() {
        ordering = Ordering.OBO;
        return this;
    }

    /** 返回目标队列名称。 */
    public String getDestName() {
        return destName;
    }

    /** 返回数量选择模式。 */
    public Selector getSelector() {
        return selector;
    }

    /** 返回待转移元素条数。 */
    public int getCount() {
        return count;
    }

    /** 返回转移顺序策略。 */
    public Ordering getOrdering() {
        return ordering;
    }
}
