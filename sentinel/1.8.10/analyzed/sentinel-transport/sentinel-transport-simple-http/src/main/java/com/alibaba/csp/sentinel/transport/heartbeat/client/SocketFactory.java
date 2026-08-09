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
package com.alibaba.csp.sentinel.transport.heartbeat.client;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import com.alibaba.csp.sentinel.transport.endpoint.Protocol;
import com.alibaba.csp.sentinel.transport.ssl.SslFactory;

/**
 * Socket 工厂：按协议创建普通 TCP 或 SSL {@link Socket}。
 *
 * @author Leo Li
 */
public class SocketFactory {

    /** 懒加载 SSLSocketFactory 的静态内部类。 */
    private static class SSLSocketFactoryInstance {
        private static final SSLSocketFactory SSL_SOCKET_FACTORY = SslFactory.getSslConnectionSocketFactory().getSocketFactory();
    }

    /** HTTP 返回普通 Socket，HTTPS 返回 {@link SslFactory} 创建的 SSL Socket。 */
    public static Socket getSocket(Protocol protocol) throws IOException {
        return protocol == Protocol.HTTP ? new Socket() : SSLSocketFactoryInstance.SSL_SOCKET_FACTORY.createSocket();
    }
}
