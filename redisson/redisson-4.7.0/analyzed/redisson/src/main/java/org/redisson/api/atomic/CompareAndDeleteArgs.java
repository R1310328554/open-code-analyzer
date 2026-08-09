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
package org.redisson.api.atomic;

import java.util.Objects;

/**
 * {@link org.redisson.api.RAtomicLong#compareAndDelete(CompareAndDeleteArgs)}
 * 与 {@link org.redisson.api.RAtomicDouble#compareAndDelete(CompareAndDeleteArgs)} 的参数对象；
 * 定义按数值条件删除原子值的规则。
 *
 * @author Nikita Koksharov
 *
 */
public final class CompareAndDeleteArgs {

    private final ComparisonCondition condition;
    private final Number threshold;

    private CompareAndDeleteArgs(ComparisonCondition condition, Number threshold) {
        Objects.requireNonNull(condition, "Condition can't be null");
        Objects.requireNonNull(threshold, "Threshold can't be null");
        this.condition = condition;
        this.threshold = threshold;
    }

    /**
     * 当存储值小于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs less(long value) {
        return new CompareAndDeleteArgs(ComparisonCondition.LESS, value);
    }

    /**
     * 当存储值小于或等于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs lessOrEqual(long value) {
        return new CompareAndDeleteArgs(ComparisonCondition.LESS_OR_EQUAL, value);
    }

    /**
     * 当存储值大于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs greater(long value) {
        return new CompareAndDeleteArgs(ComparisonCondition.GREATER, value);
    }

    /**
     * 当存储值大于或等于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs greaterOrEqual(long value) {
        return new CompareAndDeleteArgs(ComparisonCondition.GREATER_OR_EQUAL, value);
    }

    /**
     * 当存储值等于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs equal(long value) {
        return new CompareAndDeleteArgs(ComparisonCondition.EQUAL, value);
    }

    /**
     * 当存储值不等于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs notEqual(long value) {
        return new CompareAndDeleteArgs(ComparisonCondition.NOT_EQUAL, value);
    }

    /**
     * 当存储值小于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs less(double value) {
        return new CompareAndDeleteArgs(ComparisonCondition.LESS, value);
    }

    /**
     * 当存储值小于或等于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs lessOrEqual(double value) {
        return new CompareAndDeleteArgs(ComparisonCondition.LESS_OR_EQUAL, value);
    }

    /**
     * 当存储值大于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs greater(double value) {
        return new CompareAndDeleteArgs(ComparisonCondition.GREATER, value);
    }

    /**
     * 当存储值大于或等于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs greaterOrEqual(double value) {
        return new CompareAndDeleteArgs(ComparisonCondition.GREATER_OR_EQUAL, value);
    }

    /**
     * 当存储值等于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs equal(double value) {
        return new CompareAndDeleteArgs(ComparisonCondition.EQUAL, value);
    }

    /**
     * 当存储值不等于指定阈值时删除条目。
     *
     * @param value 阈值
     * @return 参数对象
     */
    public static CompareAndDeleteArgs notEqual(double value) {
        return new CompareAndDeleteArgs(ComparisonCondition.NOT_EQUAL, value);
    }

    public ComparisonCondition getCondition() {
        return condition;
    }

    public Number getThreshold() {
        return threshold;
    }

}
