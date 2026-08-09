/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common.lite;

import java.util.Objects;

/**
 * Lite 消费起始偏移选项：支持策略、绝对偏移、尾部 N 条与时间戳四种类型。
 */
public class OffsetOption {

    /** 策略值：从最新位置消费。 */
    public static final long POLICY_LAST_VALUE = 0L;
    /** 策略值：从最早位置消费。 */
    public static final long POLICY_MIN_VALUE = 1L;
    /** 策略值：从最大偏移消费。 */
    public static final long POLICY_MAX_VALUE = 2L;

    /** 偏移类型。 */
    private Type type;
    /** 偏移值或策略枚举值，含义取决于 type。 */
    private long value;

    public OffsetOption() {
    }

    /** 指定类型与数值构造偏移选项。 */
    public OffsetOption(Type type, long value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OffsetOption option = (OffsetOption) o;
        return value == option.value && type == option.type;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(type);
        result = 31 * result + Long.hashCode(value);
        return result;
    }

    @Override
    public String toString() {
        return "OffsetOption{" + "type=" + type +
            ", value=" + value +
            '}';
    }

    /** 偏移选项类型。 */
    public enum Type {
        /** 预定义策略（LAST/MIN/MAX）。 */
        POLICY,
        /** 绝对逻辑偏移。 */
        OFFSET,
        /** 从队列尾部向前 N 条。 */
        TAIL_N,
        /** 按时间戳定位起始消费位点。 */
        TIMESTAMP
    }

}