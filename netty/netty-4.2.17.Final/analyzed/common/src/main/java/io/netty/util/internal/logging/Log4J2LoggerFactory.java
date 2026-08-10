/*
 * Copyright 2016 The Netty Project
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

import org.apache.logging.log4j.LogManager;

/**
 * 创建 Apache Log4J2 日志器的工厂。
 * <p>Netty 在 classpath 探测链中优先于 Log4J 1.x 选用 Log4J2。</p>
 */
public final class Log4J2LoggerFactory extends InternalLoggerFactory {

        /** 全局单例，供 {@link InternalLoggerFactory} 自动选择。 */
    public static final InternalLoggerFactory INSTANCE = new Log4J2LoggerFactory();

        /**
     * 已弃用，请使用 {@link #INSTANCE}。
     * @deprecated Use {@link #INSTANCE} instead.
     */
    @Deprecated
    public Log4J2LoggerFactory() {
    }

        /** 通过 LogManager 获取 Log4J2 Logger 并包装为 {@link Log4J2Logger}。 */
    @Override
    public InternalLogger newInstance(String name) {
        return new Log4J2Logger(LogManager.getLogger(name));
    }
}
