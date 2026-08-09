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

package org.apache.rocketmq.common.constant;

/**
 * HAProxy PROXY Protocol 相关属性键名常量。
 */
public class HAProxyConstants {

    /** Netty Channel 属性中的通道 ID 键。 */
    public static final String CHANNEL_ID = "channel_id";
    /** PROXY Protocol 属性键前缀。 */
    public static final String PROXY_PROTOCOL_PREFIX = "proxy_protocol_";
    /** 客户端源地址（PROXY Protocol 解析结果）。 */
    public static final String PROXY_PROTOCOL_ADDR = PROXY_PROTOCOL_PREFIX + "addr";
    /** 客户端源端口。 */
    public static final String PROXY_PROTOCOL_PORT = PROXY_PROTOCOL_PREFIX + "port";
    /** 服务端本地地址。 */
    public static final String PROXY_PROTOCOL_SERVER_ADDR = PROXY_PROTOCOL_PREFIX + "server_addr";
    /** 服务端本地端口。 */
    public static final String PROXY_PROTOCOL_SERVER_PORT = PROXY_PROTOCOL_PREFIX + "server_port";
    /** PROXY Protocol TLV 扩展字段键前缀（十六进制类型码）。 */
    public static final String PROXY_PROTOCOL_TLV_PREFIX = PROXY_PROTOCOL_PREFIX + "tlv_0x";
}
