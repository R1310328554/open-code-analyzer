/*
 * Copyright 2020 The Netty Project
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
package io.netty.resolver.dns;

import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.logging.LogLevel;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 为 {@link DnsNameResolver} 提供详细 DNS 查询生命周期日志的 {@link DnsQueryLifecycleObserverFactory}。
 * <p>
 * 在 {@linkplain DnsNameResolverBuilder#dnsQueryLifecycleObserverFactory(DnsQueryLifecycleObserverFactory)
 * 解析器上配置} 后，将输出跟踪信息以便排查解析失败原因。
 */
public final class LoggingDnsQueryLifeCycleObserverFactory implements DnsQueryLifecycleObserverFactory {
    private static final InternalLogger DEFAULT_LOGGER =
            InternalLoggerFactory.getInstance(LoggingDnsQueryLifeCycleObserverFactory.class);
    private final InternalLogger logger;
    private final InternalLogLevel level;

    /**
     * 创建以默认 {@link LogLevel#DEBUG} 级别记录事件的 {@link DnsQueryLifecycleObserver}。
     */
    public LoggingDnsQueryLifeCycleObserverFactory() {
        this(LogLevel.DEBUG);
    }

    /**
     * 创建以指定日志级别记录事件的 {@link DnsQueryLifecycleObserver}。
     * @param level The log level to use for logging resolver events.
     */
    public LoggingDnsQueryLifeCycleObserverFactory(LogLevel level) {
        this.level = checkAndConvertLevel(level);
        logger = DEFAULT_LOGGER;
    }

    /**
     * 使用指定类上下文 logger、给定日志级别创建 {@link DnsQueryLifecycleObserver}。
     * @param classContext The class context for the logger to use.
     * @param level The log level to use for logging resolver events.
     */
    public LoggingDnsQueryLifeCycleObserverFactory(Class<?> classContext, LogLevel level) {
        this.level = checkAndConvertLevel(level);
        logger = InternalLoggerFactory.getInstance(checkNotNull(classContext, "classContext"));
    }

    /**
     * 使用指定名称 logger、给定日志级别创建 {@link DnsQueryLifecycleObserver}。
     * @param name The name for the logger to use.
     * @param level The log level to use for logging resolver events.
     */
    public LoggingDnsQueryLifeCycleObserverFactory(String name, LogLevel level) {
        this.level = checkAndConvertLevel(level);
        logger = InternalLoggerFactory.getInstance(checkNotNull(name, "name"));
    }

    private static InternalLogLevel checkAndConvertLevel(LogLevel level) {
        return checkNotNull(level, "level").toInternalLevel();
    }

    @Override
    public DnsQueryLifecycleObserver newDnsQueryLifecycleObserver(DnsQuestion question) {
        return new LoggingDnsQueryLifecycleObserver(question, logger, level);
    }
}
