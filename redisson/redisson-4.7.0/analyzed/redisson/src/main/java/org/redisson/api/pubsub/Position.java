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
package org.redisson.api.pubsub;

import java.time.Instant;

/**
 * 表示 PubSub 订阅消息流中的消费位置。
 * <p>
 * 与 {@link Subscription#seek(Position)} 配合使用，控制消息消费的起始点，
 * 支持消息回放或跳过。
 *
 * @author Nikita Koksharov
 *
 */
public interface Position {

    /**
     * 创建指向最新（最新发布）消息的位置。
     * <p>
     * 此为默认位置。
     *
     * @return 位置对象
     */
    static Position latest() {
        return new PositionValue(true);
    }

    /**
     * 创建指向最早可用消息的位置。
     *
     * @return 位置对象
     */
    static Position earliest() {
        return new PositionValue(false);
    }

    /**
     * 创建位于指定消息 ID 处（含该 ID）的位置。
     *
     * @param id 要定位到的消息 ID
     * @return 位置对象
     */
    static Position messageId(String id) {
        return new PositionValue(id, false);
    }

    /**
     * 创建位于指定消息 ID 之后（不含该 ID）的位置。
     *
     * @param id 要定位到其后的消息 ID
     * @return 位置对象
     */
    static Position messageIdExclusive(String id) {
        return new PositionValue(id, true);
    }

    /**
     * 创建位于指定时间戳处（含该时刻）的位置。
     *
     * @param value 要定位到的时间戳
     * @return 位置对象
     */
    static Position timestamp(Instant value) {
        return new PositionValue(value.toEpochMilli(), false);
    }

    /**
     * 创建位于指定时间戳之后（不含该时刻）的位置。
     * <p>
     * 定位到此位置时，消费将从时间戳严格大于指定值的第一条消息开始。
     *
     * @param value 要定位到其后的时间戳
     * @return 位置对象
     */
    static Position timestampExclusive(Instant value) {
        return new PositionValue(value.toEpochMilli(), true);
    }


}
