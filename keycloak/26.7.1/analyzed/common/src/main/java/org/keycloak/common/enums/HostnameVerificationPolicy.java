/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.common.enums;

/**
 * TLS 连接时对服务端证书的主机名校验策略。
 */
public enum HostnameVerificationPolicy {

    /**
     * 不校验服务端证书中的主机名。
     */
    ANY,

    /**
     * 允许子域名通配符（如 {@code *.foo.com}）匹配任意层级（如 {@code a.b.foo.com}）。
     */
    @Deprecated
    WILDCARD,

    /**
     * 证书的 CN 必须与连接目标主机名一致。
     */
    @Deprecated
    STRICT,

    /**
     * 类似 STRICT，但使用更完整的公共后缀匹配器进行校验。
     */
    DEFAULT
}