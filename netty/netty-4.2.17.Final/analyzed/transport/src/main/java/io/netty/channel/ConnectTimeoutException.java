/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel;

import java.net.ConnectException;

/**
 * {@link ConnectException} which will be thrown if a connection could
 * not be established because of a connection timeout.
 * <p>连接超时异常：在 {@link ChannelOption#CONNECT_TIMEOUT_MILLIS} 内未能建立连接时抛出，
 * 继承自 {@link ConnectException}。</p>
 */
public class ConnectTimeoutException extends ConnectException {
    private static final long serialVersionUID = 2317065249988317463L;

    /** 使用指定消息创建异常。 */
    public ConnectTimeoutException(String msg) {
        super(msg);
    }

    /** 创建无消息的连接超时异常。 */
    public ConnectTimeoutException() {
    }
}
