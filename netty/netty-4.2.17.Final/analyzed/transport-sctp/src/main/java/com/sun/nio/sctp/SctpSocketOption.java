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
 * SCTP 通道可配置选项的类型安全描述符。
 * <p>与 {@link java.net.SocketOption} 类似，由 {@link SctpStandardSocketOptions}  提供标准常量；{@link #name()} 与 {@link #type()} 供反射与选项映射使用。</p>
 * @param <T> 选项值 Java 类型
 */
public interface SctpSocketOption<T> {
    /** 选项名称字符串（通常与内核/SCTP 栈一致） */
    String name();
    /** 选项值的 {@link Class} 类型 */
    Class<T> type();
}
