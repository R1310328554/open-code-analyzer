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
package org.redisson.api.bitset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link BitFieldArgs} 与 {@link BitFieldInitArgs} 的默认实现，
 * 按顺序保存 BITFIELD 子命令操作列表。
 *
 * @author Su Ko
 *
 */
public final class BitFieldParams implements BitFieldArgs, BitFieldInitArgs {

    public enum OperationType {
        /**
         * GET 子命令：读取指定编码/偏移处的值。
         */
        GET,

        /**
         * SET 子命令：写入值并返回旧值；
         * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。
         */
        SET,

        /**
         * INCRBY 子命令：按给定量递增并返回新值；
         * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。
         */
        INCRBY,

        /**
         * OVERFLOW 子命令：为后续 SET/INCRBY 设置溢出行为，直至下一次 OVERFLOW。
         */
        OVERFLOW
    }

    public static final class Operation {
        private final OperationType type;
        private final String encoding;
        private final BitOffset offset;
        private final Long value;
        private final BitFieldOverflow overflow;

        Operation(OperationType type, String encoding, BitOffset offset, Long value, BitFieldOverflow overflow) {
            this.type = type;
            this.encoding = encoding;
            this.offset = offset;
            this.value = value;
            this.overflow = overflow;
        }

        public OperationType getType() {
            return type;
        }

        public String getEncoding() {
            return encoding;
        }

        public BitOffset getOffset() {
            return offset;
        }

        public Long getValue() {
            return value;
        }

        public BitFieldOverflow getOverflow() {
            return overflow;
        }
    }

    private final List<Operation> operations = new ArrayList<>();

    @Override
    public BitFieldArgs overflow(BitFieldOverflow overflow) {
        operations.add(new Operation(OperationType.OVERFLOW, null, null, null, overflow));
        return this;
    }

    @Override
    public BitFieldArgs getSigned(int size, BitOffset offset) {
        operations.add(new Operation(OperationType.GET, "i" + size, offset, null, null));
        return this;
    }

    @Override
    public BitFieldArgs getUnsigned(int size, BitOffset offset) {
        operations.add(new Operation(OperationType.GET, "u" + size, offset, null, null));
        return this;
    }

    @Override
    public BitFieldArgs setSigned(int size, BitOffset offset, long value) {
        operations.add(new Operation(OperationType.SET, "i" + size, offset, value, null));
        return this;
    }

    @Override
    public BitFieldArgs setUnsigned(int size, BitOffset offset, long value) {
        operations.add(new Operation(OperationType.SET, "u" + size, offset, value, null));
        return this;
    }

    @Override
    public BitFieldArgs incrementSignedBy(int size, BitOffset offset, long increment) {
        operations.add(new Operation(OperationType.INCRBY, "i" + size, offset, increment, null));
        return this;
    }

    @Override
    public BitFieldArgs incrementUnsignedBy(int size, BitOffset offset, long increment) {
        operations.add(new Operation(OperationType.INCRBY, "u" + size, offset, increment, null));
        return this;
    }

    public List<Operation> getOperations() {
        return Collections.unmodifiableList(operations);
    }
}
