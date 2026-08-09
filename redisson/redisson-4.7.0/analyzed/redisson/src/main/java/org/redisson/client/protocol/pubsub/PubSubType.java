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
package org.redisson.client.protocol.pubsub;

/**
 * Redis Pub/Sub 状态回复中的操作类型枚举。
 * <p>
 * 对应 {@code subscribe}、{@code psubscribe}、{@code ssubscribe}
 * 及其退订变体的 RESP 推送类型字段。
 *
 * @author Nikita Koksharov
 *
 */
public enum PubSubType {

    /** 频道订阅成功。 */
    SUBSCRIBE,
    /** 模式订阅成功。 */
    PSUBSCRIBE,
    /** 分片频道订阅成功（Redis 7+）。 */
    SSUBSCRIBE,
    /** 模式退订成功。 */
    PUNSUBSCRIBE,
    /** 频道退订成功。 */
    UNSUBSCRIBE,
    /** 分片频道退订成功。 */
    SUNSUBSCRIBE

}
