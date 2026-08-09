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
package org.redisson.api.stream;

import org.redisson.api.RStream;

import java.util.Objects;

/**
 * 流消息 ID 对象。
 * <p>
 * 封装 Redis Stream 消息的时间戳-序列号标识及特殊占位常量。
 * 
 * @author Nikita Koksharov
 *
 */
public final class StreamMessageId {

    /**
     * 接收从未投递给其他消费者的新消息。
     * <p>
     * 用于 {@link RStream#readGroup} 方法
     */
    public static final StreamMessageId NEVER_DELIVERED = new StreamMessageId(-1);

    /**
     * 由 Redis 自动生成消息 ID。
     * <p>
     * 用于 {@link RStream#add} 方法
     */
    public static final StreamMessageId AUTO_GENERATED = new StreamMessageId(-1);

    /**
     * 表示最小消息 ID。
     * <p>
     * 用于 {@link RStream#range} 方法
     */
    public static final StreamMessageId MIN = new StreamMessageId(-1);
    
    /**
     * 表示最大消息 ID。
     * <p>
     * 用于 {@link RStream#range} 方法
     */
    public static final StreamMessageId MAX = new StreamMessageId(-1);
    
    /**
     * 接收自调用时刻起写入的新消息。
     * <p>
     * 用于 {@link RStream#read}、{@link RStream#createGroup} 方法
     */
    public static final StreamMessageId NEWEST = new StreamMessageId(-1);

    /**
     * 接收最新的流条目。
     * <p>
     * 用于 {@link RStream#read}、{@link RStream#createGroup} 方法
     * <p>
     * 需要 Redis 7.4 及以上版本
     *
     */
    public static final StreamMessageId LAST = new StreamMessageId(-1);

    /**
     * 接收所有流条目。
     * <p>
     * 用于 {@link RStream#read}、{@link RStream#createGroup} 方法
     */
    public static final StreamMessageId ALL = new StreamMessageId(-1);
    
    private final long id0;
    private long id1;
    
    private boolean autogenerateSequenceId;
    
    public StreamMessageId(long id0) {
        super();
        this.id0 = id0;
    }

    public StreamMessageId(long id0, long id1) {
        super();
        this.id0 = id0;
        this.id1 = id1;
    }
    
    public StreamMessageId autogenerateSequenceId() {
        this.autogenerateSequenceId = true;
        return this;
    }
    
    /**
     * 返回 ID 的第一部分（毫秒时间戳）。
     * 
     * @return ID 第一部分
     */
    public long getId0() {
        return id0;
    }

    /**
     * 返回 ID 的第二部分（序列号）。
     * 
     * @return ID 第二部分
     */
    public long getId1() {
        return id1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreamMessageId that = (StreamMessageId) o;
        return id0 == that.id0 && id1 == that.id1 && autogenerateSequenceId == that.autogenerateSequenceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id0, id1, autogenerateSequenceId);
    }
    
    @Override
    @SuppressWarnings("AvoidInlineConditionals")
    public String toString() {
        if (this == NEVER_DELIVERED) {
            return ">";
        }
        if (this == LAST) {
            return "+";
        }
        if (this == NEWEST) {
            return "$";
        }
        if (this == MIN) {
            return "-";
        }
        if (this == MAX) {
            return "+";
        }
        if (this == ALL) {
            return "0";
        }
        if (this == AUTO_GENERATED) {
            return "*";
        }

        return id0 + "-" + (autogenerateSequenceId ? "*" : id1);
    }
    
}
