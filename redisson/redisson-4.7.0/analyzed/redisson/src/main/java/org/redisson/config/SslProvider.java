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
 * Redisson TLS 连接的 SSL 实现提供方。
 * <p>在 {@link org.redisson.config.Config} 或 {@link BaseConfig} 的 SSL 相关
 * 设置中与 truststore/keystore 等配合使用。
 *
 * @author Nikita Koksharov
 *
 */
public enum SslProvider {

    /** 使用 JDK 默认 SSLEngine/JSSE 处理 TLS（无需额外 native 库）。 */
    JDK,
    
    /** 使用 Netty OpenSSL（需 classpath 中有 netty-tcnative），性能通常优于纯 JDK。 */
    OPENSSL
    
}
