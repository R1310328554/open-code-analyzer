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
 * RediSearch 聚合查询选项。
 * <p>
 * 在 {@link AggregationBaseOptions} 通用配置基础上，额外支持游标（cursor）分页读取。
 *
 * @author Nikita Koksharov
 *
 */
public class AggregationOptions extends AggregationBaseOptions<AggregationOptions> {

    protected AggregationOptions() {
    }

    /**
     * 返回默认聚合查询选项。
     *
     * @return 默认选项实例
     */
    public static AggregationOptions defaults() {
        return new AggregationOptions();
    }

    /**
     * 启用游标模式，支持分批读取大量聚合结果。
     *
     * @return 当前选项对象
     */
    public AggregationOptions withCursor() {
        withCursor = true;
        return this;
    }

    /**
     * 启用游标模式并设置每批返回的记录数。
     *
     * @param count 每批记录数
     * @return 当前选项对象
     */
    public AggregationOptions withCursor(int count) {
        withCursor = true;
        cursorCount = count;
        return this;
    }

    /**
     * 启用游标模式，设置每批记录数及游标最大空闲时间。
     *
     * @param count 每批记录数
     * @param maxIdle 游标最大空闲时间（毫秒）
     * @return 当前选项对象
     */
    public AggregationOptions withCursor(int count, int maxIdle) {
        withCursor = true;
        cursorCount = count;
        cursorMaxIdle = Duration.ofMillis(maxIdle);
        return this;
    }

}
