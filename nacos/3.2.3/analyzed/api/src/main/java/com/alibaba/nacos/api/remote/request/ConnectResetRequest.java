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

package com.alibaba.nacos.api.remote.request;

import static com.alibaba.nacos.api.common.Constants.Remote.INTERNAL_MODULE;

/**
 * 服务端通知客户端重置连接的请求。
 *
 * <p>当服务端需要客户端断开并重连时（如负载均衡切换），携带目标 {@link #serverIp}、{@link #serverPort} 与 {@link #connectionId}。</p>
 *
 * @author liuzunfei
 * @version $Id: ConnectResetRequest.java, v 0.1 2020年07月15日 11:11 AM liuzunfei Exp $
 */
public class ConnectResetRequest extends ServerRequest {
    
    /** 建议重连的服务端 IP。 */
    String serverIp;
    
    /** 建议重连的服务端端口。 */
    String serverPort;
    
    /** 待重置的连接 ID。 */
    String connectionId;
    
    /** {@inheritDoc} 返回内部模块标识。 */
    @Override
    public String getModule() {
        return INTERNAL_MODULE;
    }
    
    /** 返回待重置的连接 ID。 */
    public String getConnectionId() {
        return connectionId;
    }
    
    /**
     * 设置待重置的连接 ID。
     *
     * @param connectionId 连接标识
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    /** 返回建议重连的服务端 IP。 */
    public String getServerIp() {
        return serverIp;
    }
    
    /**
     * 设置建议重连的服务端 IP。
     *
     * @param serverIp 服务端 IP
     */
    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
    
    /** 返回建议重连的服务端端口。 */
    public String getServerPort() {
        return serverPort;
    }
    
    /**
     * 设置建议重连的服务端端口。
     *
     * @param serverPort 服务端端口
     */
    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }
}
