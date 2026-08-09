/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.remoting.common;

/**
 * 服务端 TLS/SSL 工作模式枚举，支持三种策略：
 * <ol>
 *     <li><strong>disabled：</strong> 禁用 SSL；任何 SSL 握手将被拒绝并关闭连接。</li>
 *     <li><strong>permissive：</strong> 可选 SSL；服务端同时接受明文与 TLS 连接。</li>
 *     <li><strong>enforcing：</strong> 强制 SSL；非 TLS 连接将被拒绝。</li>
 * </ol>
 */
public enum TlsMode {

    /** 禁用 TLS。 */
    DISABLED("disabled"),
    /** 可选 TLS（兼容明文）。 */
    PERMISSIVE("permissive"),
    /** 强制 TLS。 */
    ENFORCING("enforcing");

    private String name;

    TlsMode(String name) {
        this.name = name;
    }

    /** 按配置字符串解析模式，无法识别时默认 {@link #PERMISSIVE}。 */
    public static TlsMode parse(String mode) {
        for (TlsMode tlsMode : TlsMode.values()) {
            if (tlsMode.name.equals(mode)) {
                return tlsMode;
            }
        }

        return PERMISSIVE;
    }

    /** 返回模式对应的配置名称字符串。 */
    public String getName() {
        return name;
    }
}
