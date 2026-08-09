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

import org.redisson.api.SortOrder;

/**
 * 聚合分组归约器，用于 {@link org.redisson.api.RSearch#aggregate(String, String, AggregationOptions)}。
 * <p>
 * 提供 AVG、SUM、COUNT 等内置归约及自定义函数工厂方法。
 *
 * @author Nikita Koksharov
 *
 */
public interface Reducer {

    /** 对数值字段求平均值。 */
    static Reducer avg(String fieldName) {
        return new ReducerParams("AVG", fieldName);
    }

    /** 对数值字段求和。 */
    static Reducer sum(String fieldName) {
        return new ReducerParams("SUM", fieldName);
    }

    /** 取数值字段最大值。 */
    static Reducer max(String fieldName) {
        return new ReducerParams("MAX", fieldName);
    }

    /** 取数值字段最小值。 */
    static Reducer min(String fieldName) {
        return new ReducerParams("MIN", fieldName);
    }

    /**
     * 计算指定分位数值。
     *
     * @param fieldName 数值字段名
     * @param percent 分位百分比
     */
    static Reducer quantile(String fieldName, Double percent) {
        return new ReducerParams("QUANTILE", fieldName, percent.toString());
    }

    /** 统计分组内文档数量。 */
    static Reducer count() {
        return new ReducerParams("COUNT");
    }

    /** 统计字段去重后的取值个数。 */
    static Reducer countDistinct(String fieldName) {
        return new ReducerParams("COUNT", fieldName);
    }

    /** 近似统计字段去重取值个数。 */
    static Reducer countDistinctish(String fieldName) {
        return new ReducerParams("COUNT_DISTINCTISH", fieldName);
    }

    /** 取分组内某字段的第一个值。 */
    static Reducer firstValue(String fieldName) {
        return new ReducerParams("FIRST_VALUE", fieldName);
    }

    /**
     * 按指定字段排序后取第一个值。
     *
     * @param fieldName 待取值字段
     * @param sortFieldName 排序依据字段
     * @param sortOrder 排序方向
     */
    static Reducer firstValue(String fieldName, String sortFieldName, SortOrder sortOrder) {
        return new ReducerParams("FIRST_VALUE", fieldName, "BY", sortFieldName, sortOrder.toString());
    }

    /** 从字段值中随机抽取指定数量的样本。 */
    static Reducer randomSample(String fieldName, int size) {
        return new ReducerParams("FIRST_VALUE", fieldName, Integer.toString(size));
    }

    /** 计算数值字段的标准差。 */
    static Reducer stddev(String fieldName) {
        return new ReducerParams("STDDEV", fieldName);
    }

    /** 将字段值收集为列表。 */
    static Reducer toList(String fieldName) {
        return new ReducerParams("TOLIST", fieldName);
    }

    /**
     * 调用自定义归约函数。
     *
     * @param functionName 函数名
     * @param args 附加参数
     */
    static Reducer custom(String functionName, String... args) {
        return new ReducerParams(functionName, args);
    }

    /**
     * 为归约结果指定别名。
     *
     * @param alias 结果别名
     * @return 当前归约器
     */
    Reducer as(String alias);

}
