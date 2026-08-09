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
package org.redisson.api.search.aggregate;

import java.time.Duration;

/**
 * 启用游标、可迭代拉取结果的聚合选项。
 * <p>
 * 构造时默认开启 {@code WITHCURSOR}，适合分批消费大型聚合结果集。
 *
 * @author seakider
 *
 */
public final class IterableAggregationOptions extends AggregationBaseOptions<IterableAggregationOptions> {

    private IterableAggregationOptions() {
        withCursor = true;
    }

    /** 返回默认的可迭代聚合选项实例。 */
    public static IterableAggregationOptions defaults() {
        return new IterableAggregationOptions();
    }

    @Override
    public IterableAggregationOptions cursorCount(int count) {
        cursorCount = count;
        return this;
    }

    @Override
    public IterableAggregationOptions cursorMaxIdle(Duration duration) {
        cursorMaxIdle = duration;
        return this;
    }

}
