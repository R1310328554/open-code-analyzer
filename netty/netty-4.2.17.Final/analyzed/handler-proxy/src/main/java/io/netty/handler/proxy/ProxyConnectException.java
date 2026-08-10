/*
 * Copyright 2014 The Netty Project
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

package io.netty.handler.proxy;

import java.net.ConnectException;

/**
 * 经代理建立连接失败时抛出的 {@link ConnectException} 子类。
 * <p>表示 TCP 已连上代理，但代理握手/CONNECT 未成功。</p>
 */
public class ProxyConnectException extends ConnectException {
    private static final long serialVersionUID = 5211364632246265538L;

    /** 无消息构造。 */
    public ProxyConnectException() { }

    /** @param msg 异常消息 */
    public ProxyConnectException(String msg) {
        super(msg);
    }

    /** @param cause 根因 */
    public ProxyConnectException(Throwable cause) {
        initCause(cause);
    }

    /** @param msg 异常消息；@param cause 根因 */
    public ProxyConnectException(String msg, Throwable cause) {
        super(msg);
        initCause(cause);
    }
}
