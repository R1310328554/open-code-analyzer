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
package org.redisson.api.queue;

/**
 * 指定队列消息处理操作的确认模式。
 * <p>
 * 该枚举定义从队列消费消息时系统如何处理确认。
 *
 * @author Nikita Koksharov
 *
 */
public enum AcknowledgeMode {


    /**
     * 自动确认模式。
     * <p>
     * 此模式下，消息交付给消费者后由系统自动确认，
     * 无论处理是否成功。
     */
    AUTO,

    /**
     * 手动确认模式。
     * <p>
     * 此模式下，消费者须在成功处理消息后显式确认。
     */
    MANUAL

}
