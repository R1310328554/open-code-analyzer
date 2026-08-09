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
package org.apache.rocketmq.remoting.netty;


import io.netty.util.AttributeKey;
import org.apache.rocketmq.common.constant.HAProxyConstants;
import org.apache.rocketmq.remoting.protocol.LanguageCode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty {@link AttributeKey} 常量：在 Channel 上缓存远端地址、客户端 ID、版本与 HAProxy 信息。
 */
public class AttributeKeys {

    /** 对端 Remoting 地址字符串。 */
    public static final AttributeKey<String> REMOTE_ADDR_KEY = AttributeKey.valueOf("RemoteAddr");

    /** 客户端实例标识。 */
    public static final AttributeKey<String> CLIENT_ID_KEY = AttributeKey.valueOf("ClientId");

    /** Remoting 协议版本号。 */
    public static final AttributeKey<Integer> VERSION_KEY = AttributeKey.valueOf("Version");

    /** 客户端语言/实现类型。 */
    public static final AttributeKey<LanguageCode> LANGUAGE_CODE_KEY = AttributeKey.valueOf("LanguageCode");

    /** HAProxy PROXY 协议解析出的客户端地址。 */
    public static final AttributeKey<String> PROXY_PROTOCOL_ADDR =
            AttributeKey.valueOf(HAProxyConstants.PROXY_PROTOCOL_ADDR);

    /** HAProxy PROXY 协议解析出的客户端端口。 */
    public static final AttributeKey<String> PROXY_PROTOCOL_PORT =
            AttributeKey.valueOf(HAProxyConstants.PROXY_PROTOCOL_PORT);

    /** HAProxy PROXY 协议解析出的服务端地址。 */
    public static final AttributeKey<String> PROXY_PROTOCOL_SERVER_ADDR =
            AttributeKey.valueOf(HAProxyConstants.PROXY_PROTOCOL_SERVER_ADDR);

    /** HAProxy PROXY 协议解析出的服务端端口。 */
    public static final AttributeKey<String> PROXY_PROTOCOL_SERVER_PORT =
            AttributeKey.valueOf(HAProxyConstants.PROXY_PROTOCOL_SERVER_PORT);

    private static final Map<String, AttributeKey<String>> ATTRIBUTE_KEY_MAP = new ConcurrentHashMap<>();

    /** 按名称获取或创建并缓存字符串型 {@link AttributeKey}。 */
    public static AttributeKey<String> valueOf(String name) {
        return ATTRIBUTE_KEY_MAP.computeIfAbsent(name, AttributeKey::valueOf);
    }
}
