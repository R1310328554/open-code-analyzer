/*
 * Copyright 2012 The Netty Project
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
package io.netty.util.internal.logging;

/**
 * {@link InternalLogger} 支持的内部日志级别枚举。
 * <p>The log level that {@link InternalLogger} can log at.</p>
 */
public enum InternalLogLevel {
    /** 最细粒度追踪，通常仅用于诊断。（'TRACE' 级别）。 */
    TRACE,
    /** 调试信息，开发阶段常用。（'DEBUG' 级别）。 */
    DEBUG,
    /** 一般性运行信息。（'INFO' 级别）。 */
    INFO,
    /** 潜在问题或降级提示。（'WARN' 级别）。 */
    WARN,
    /** 错误事件，通常需关注。（'ERROR' 级别）。 */
    ERROR
}
