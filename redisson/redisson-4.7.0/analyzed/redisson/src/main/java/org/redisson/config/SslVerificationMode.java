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
 * SSL/TLS 证书校验模式，用于 {@link BaseConfig#setSslVerificationMode(SslVerificationMode)}。
 * <p>
 * 控制 Redisson 与 Redis 建立 TLS 连接时对服务端证书的验证严格程度。
 *
 * @author Nikita Koksharov
 *
 */
public enum SslVerificationMode {

    /** 不进行 SSL 证书校验（仅加密传输，不验证身份）。 */
    NONE,

    /** 校验证书链有效性，但跳过主机名（hostname）匹配检查。 */
    CA_ONLY,

    /** 完整校验证书链与主机名，生产环境推荐。 */
    STRICT

}
