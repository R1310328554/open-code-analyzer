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
package org.redisson.config;

/**
 * Redis 通信协议版本枚举。
 * <p>在 {@link org.redisson.config.Config} 中设置，影响握手与部分命令的
 * 回复格式（如 RESP3 的多类型回复）。
 *
 * @author Nikita Koksharov
 *
 */
public enum Protocol {

    /** Redis 2.x 起广泛使用的 RESP2 协议。 */
    RESP2,

    /** Redis 6+ 引入的 RESP3，支持更丰富的类型与推送消息。 */
    RESP3

}
