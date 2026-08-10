/*
 * Copyright 2026 The Netty Project
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
package io.netty.handler.ssl.ocsp;

/**
 * 当待校验证书未指定任何 OCSP 响应器时，由 {@link OcspClient} 内部抛出。
 * <p>
 * 为向后兼容，本异常继承 {@link NullPointerException}。
 */
public final class NoOcspResponderException extends NullPointerException {
    /**
     * 使用给定消息创建新实例。
     * @param message 错误消息
     */
    public NoOcspResponderException(String message) {
        super(message);
    }
}
