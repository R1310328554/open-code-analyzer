/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.context.addition;

/**
 * 请求地址上下文：区分客户端声明地址（source）与 TCP 连接对端地址（remote），并记录 Host 头，供审计、限流与链路追踪使用。
 * Nacos request address information context.
 *
 * @author xiweng.yy
 */
public class AddressContext {
    
    /** 客户端源 IP（通常经代理头解析，多数场景与 remoteIp 一致）。 */
    private String sourceIp;
    
    /** 客户端源端口（多数场景与 remotePort 一致）。 */
    private int sourcePort;
    
    /** 连接对端 IP（Socket 可见地址，即服务端视角）。 */
    private String remoteIp;
    
    /** 连接对端端口（Socket 可见端口）。 */
    private int remotePort;
    
    /** HTTP Host 头或等价主机名，无法获取时为 null。 */
    private String host;
    
    /** 返回客户端源 IP。 */
    public String getSourceIp() {
        return sourceIp;
    }
    
    /** 设置客户端源 IP。 */
    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }
    
    /** 返回客户端源端口。 */
    public int getSourcePort() {
        return sourcePort;
    }
    
    /** 设置客户端源端口。 */
    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }
    
    /** 返回连接对端 IP。 */
    public String getRemoteIp() {
        return remoteIp;
    }
    
    /** 设置连接对端 IP。 */
    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }
    
    /** 返回连接对端端口。 */
    public int getRemotePort() {
        return remotePort;
    }
    
    /** 设置连接对端端口。 */
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
    
    /** 返回 Host 头值。 */
    public String getHost() {
        return host;
    }
    
    /** 设置 Host 头值。 */
    public void setHost(String host) {
        this.host = host;
    }
}
