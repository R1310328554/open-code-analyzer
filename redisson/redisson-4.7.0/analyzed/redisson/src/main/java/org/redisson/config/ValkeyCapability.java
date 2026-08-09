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
 * Valkey 客户端能力声明选项，通过 CLIENT SETINFO 告知服务端。
 * <p>
 * 用于协商 Valkey 特有协议特性（如集群重定向）。
 *
 * @author Nikita Koksharov
 *
 */
public enum ValkeyCapability {

    /** 声明客户端可处理 Valkey 重定向（REDIRECT）消息。 */
    REDIRECT

}
