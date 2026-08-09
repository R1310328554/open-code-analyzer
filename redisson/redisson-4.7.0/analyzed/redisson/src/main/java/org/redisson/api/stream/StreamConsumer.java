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

/**
 * 流消费者详情对象。
 * <p>
 * 包含消费者名称、待处理消息数、空闲时长及非活跃时长等统计信息。
 *
 * @author Nikita Koksharov
 *
 */
public final class StreamConsumer {

    /** 消费者名称。 */
    private final String name;
    /** 待处理消息数量。 */
    private final int pending;
    /** 自上次消费以来的空闲毫秒数。 */
    private final long idleTime;
    /** 自上次成功交互以来的非活跃毫秒数。 */
    private final long inactive;

    public StreamConsumer(String name, int pending, long idleTime, long inactive) {
        this.name = name;
        this.pending = pending;
        this.idleTime = idleTime;
        this.inactive = inactive;
    }

    /**
     * 返回该消费者的待处理消息数量。
     *
     * @return 待处理消息数
     */
    public int getPending() {
        return pending;
    }

    /**
     * 返回消费者名称。
     *
     * @return 消费者名称
     */
    public String getName() {
        return name;
    }

    /**
     * 返回自该消费者未消费消息以来的空闲时长（毫秒）。
     *
     * @return 空闲时长（毫秒）
     */
    public long getIdleTime() {
        return idleTime;
    }

    /**
     * 返回自该消费者上次成功交互以来的时长（毫秒）。
     * <p>
     * 需要 <b>Redis 7.2.0 及以上版本。</b>
     *
     * @return 非活跃时长（毫秒）
     */
    public long getInactive() {
        return inactive;
    }

    @Override
    public String toString() {
        return "StreamConsumer{" +
                "name='" + name + '\'' +
                ", pending=" + pending +
                ", idleTime=" + idleTime +
                ", inactive=" + inactive +
                '}';
    }
}
