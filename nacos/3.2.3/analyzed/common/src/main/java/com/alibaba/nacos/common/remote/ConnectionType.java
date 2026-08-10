/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.remote;

/**
 * 远程连接类型枚举：标识 Nacos 客户端与服务器之间采用的 RPC 传输协议。
 * 当前仅支持 gRPC；工厂类 {@link com.alibaba.nacos.common.remote.client.RpcClientFactory}
 * 根据此类型创建对应 {@link com.alibaba.nacos.common.remote.client.RpcClient} 实现。
 * ConnectionType.
 *
 * @author liuzunfei
 * @version $Id: ConnectionType.java, v 0.1 2020年07月13日 7:15 PM liuzunfei Exp $
 */
public enum ConnectionType {
    
    /**
     * gRPC 连接：基于 HTTP/2 的高性能双向流 RPC。
     * gRPC connection.
     */
    GRPC("GRPC", "Grpc Connection");
    
    /** 连接类型短标识，如 {@code GRPC} */
    final String type;
    
    /** 连接类型可读名称 */
    final String name;
    
    /** 按 type 字符串查找枚举，未匹配返回 null */
    public static ConnectionType getByType(String type) {
        ConnectionType[] values = ConnectionType.values();
        for (ConnectionType connectionType : values) {
            if (connectionType.getType().equals(type)) {
                return connectionType;
            }
        }
        return null;
    }
    
    ConnectionType(String type, String name) {
        this.type = type;
        this.name = name;
    }
    
    /**
     * Getter method for property <tt>type</tt>.
     *
     * @return property value of type
      * <p>RPC 连接类型枚举；详见类级说明。</p>
     */
    public String getType() {
        return type;
    }
    
    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
      * <p>RPC 连接类型枚举；详见类级说明。</p>
     */
    public String getName() {
        return name;
    }
}
