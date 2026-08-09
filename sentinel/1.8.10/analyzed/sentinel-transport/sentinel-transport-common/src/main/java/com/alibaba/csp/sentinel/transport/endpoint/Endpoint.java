/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.transport.endpoint;

import java.net.InetSocketAddress;

/**
 * 传输层端点：封装协议、主机名与端口，用于 Dashboard 地址解析。
 *
 * @author Leo Li
 */
public class Endpoint {
    /** 通信协议（HTTP/HTTPS）。 */
    private Protocol protocol;

    /** 主机名或 IP。 */
    private String host;

    /** 端口号。 */
    private int port;

    /** @param protocol 协议
     * @param host 主机
     * @param port 端口 */
    public Endpoint(Protocol protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    /** @return 通信协议。 */
    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    /** @return 主机名或 IP。 */
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /** @return 端口号。 */
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Endpoint{" + "protocol=" + protocol + ", host='" + host + '\'' + ", port=" + port + '}';
    }
}
