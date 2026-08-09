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

package org.apache.rocketmq.proxy.common;

/**
 * Proxy 请求上下文变量名常量，用于 gRPC/Remoting 链路传递。
 */
public class ContextVariable {
    /** 客户端远程地址。 */
    public static final String REMOTE_ADDRESS = "remote-address";
    /** Proxy 本地监听地址。 */
    public static final String LOCAL_ADDRESS = "local-address";
    /** 客户端唯一标识。 */
    public static final String CLIENT_ID = "client-id";
    /** 底层网络通道标识。 */
    public static final String CHANNEL = "channel";
    /** 客户端语言/SDK 标识。 */
    public static final String LANGUAGE = "language";
    /** 客户端版本号。 */
    public static final String CLIENT_VERSION = "client-version";
    /** 请求剩余超时毫秒数。 */
    public static final String REMAINING_MS = "remaining-ms";
    /** 当前 RPC 动作名称。 */
    public static final String ACTION = "action";
    /** 协议类型（gRPC/Remoting 等）。 */
    public static final String PROTOCOL_TYPE = "protocol-type";
    /** 多租户命名空间标识。 */
    public static final String NAMESPACE = "namespace";
}
