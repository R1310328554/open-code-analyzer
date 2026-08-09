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
package org.redisson.api.search;

import org.redisson.api.SortOrder;

/**
 * RediSearch 聚合查询的归约器（reducer）工厂接口。
 * <p>
 * 提供 AVG、SUM、COUNT 等内置归约函数及自定义归约的静态构造方法，
 * 用于 GROUPBY 阶段对各分组字段进行聚合计算。
 *
 * @author Nikita Koksharov
 *
 */
public interface Reducer {

    /** 计算指定字段的算术平均值 */
    static Reducer avg(String fieldName) {
        return new ReducerParams("AVG", fieldName);
    }

    /** 计算指定字段的求和 */
    static Reducer sum(String fieldName) {
        return new ReducerParams("SUM", fieldName);
    }

    /** 返回指定字段的最大值 */
    static Reducer max(String fieldName) {
        return new ReducerParams("MAX", fieldName);
    }

    /** 返回指定字段的最小值 */
    static Reducer min(String fieldName) {
        return new ReducerParams("MIN", fieldName);
    }

    /** 计算指定字段的分位数，percent 为 0~1 之间的小数 */
    static Reducer quantile(String fieldName, Double percent) {
        return new ReducerParams("QUANTILE", fieldName, percent.toString());
    }

    /** 统计分组内的文档数量 */
    static Reducer count() {
        return new ReducerParams("COUNT");
    }

    /** 统计指定字段的去重值数量 */
    static Reducer countDistinct(String fieldName) {
        return new ReducerParams("COUNT", fieldName);
    }

    /** 统计指定字段的近似去重值数量 */
    static Reducer countDistinctish(String fieldName) {
        return new ReducerParams("COUNT_DISTINCTISH", fieldName);
    }

    /** 返回指定字段的第一个值 */
    static Reducer firstValue(String fieldName) {
        return new ReducerParams("FIRST_VALUE", fieldName);
    }

    /** 按排序字段返回指定字段的第一个值 */
    static Reducer firstValue(String fieldName, String sortFieldName, SortOrder sortOrder) {
        return new ReducerParams("FIRST_VALUE", fieldName, "BY", sortFieldName, sortOrder.toString());
    }

    /** 从指定字段随机采样 size 个值 */
    static Reducer randomSample(String fieldName, int size) {
        return new ReducerParams("FIRST_VALUE", fieldName, Integer.toString(size));
    }

    /** 计算指定字段的标准差 */
    static Reducer stddev(String fieldName) {
        return new ReducerParams("STDDEV", fieldName);
    }

    /** 将指定字段的值收集为列表 */
    static Reducer toList(String fieldName) {
        return new ReducerParams("TOLIST", fieldName);
    }

    /** 使用自定义函数名及参数构造归约器 */
    static Reducer custom(String functionName, String... args) {
        return new ReducerParams(functionName, args);
    }

    /**
     * 为归约结果设置别名。
     *
     * @param alias 结果别名
     * @return 当前归约器
     */
    Reducer as(String alias);

}
