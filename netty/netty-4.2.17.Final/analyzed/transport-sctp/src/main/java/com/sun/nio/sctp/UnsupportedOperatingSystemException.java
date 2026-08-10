/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.sun.nio.sctp;

/**
 * 当前操作系统或 JDK 不支持 SCTP 时抛出的运行时异常。
 * <p>Netty transport-sctp 模块在非 SCTP 平台提供 {@code com.sun.nio.sctp} stub； 各类静态初始化块调用 {@link #raise()} 使误加载时快速失败， 避免静默使用空实现。</p>
 */
public class UnsupportedOperatingSystemException extends RuntimeException {

    /** 序列化版本号 */
    private static final long serialVersionUID = -221782446524784377L;

    /** 抛出无消息实例，供 stub 类静态块调用 */
    public static void raise() {
        throw new UnsupportedOperatingSystemException();
    }

    /** 默认构造 */
    public UnsupportedOperatingSystemException() {
    }

    /** @param message 描述不支持的平台或原因 */
    public UnsupportedOperatingSystemException(String message) {
        super(message);
    }

    /** @param message 错误描述 @param cause 根因 */
    public UnsupportedOperatingSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    /** @param cause 包装的下层异常 */
    public UnsupportedOperatingSystemException(Throwable cause) {
        super(cause);
    }
}
