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

package org.apache.rocketmq.proxy.processor.channel;

/**
 * 客户端通道协议类型：标识 RemoteChannel 所代表的接入协议。
 */
public enum ChannelProtocolType {
    /** 未知或未识别的协议。 */
    UNKNOWN("unknown"),
    /** gRPC v2 协议客户端。 */
    GRPC_V2("grpc_v2"),
    /** gRPC v1 协议客户端。 */
    GRPC_V1("grpc_v1"),
    /** 经典 Remoting 协议客户端。 */
    REMOTING("remoting");

    private final String name;

    /** 绑定协议类型的字符串标识。 */
    ChannelProtocolType(String name) {
        this.name = name;
    }

    /** 返回协议名称字符串。 */
    public String getName() {
        return name;
    }
}
