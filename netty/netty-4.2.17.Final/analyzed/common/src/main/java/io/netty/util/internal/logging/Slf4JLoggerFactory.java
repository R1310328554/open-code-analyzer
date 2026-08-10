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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * 创建 <a href="https://www.slf4j.org/">SLF4J</a> 日志器的工厂；
 * 若底层实现支持 {@link LocationAwareLogger} 则选用位置感知包装。
 * <p>Logger factory which creates a SLF4J logger.</p>
 */
public class Slf4JLoggerFactory extends InternalLoggerFactory {

    @SuppressWarnings("deprecation")
        /** 默认单例（允许 NOP 实现）。 */
    public static final InternalLoggerFactory INSTANCE = new Slf4JLoggerFactory();

    /**
     * @deprecated Use {@link #INSTANCE} instead.
     */
    @Deprecated
    public Slf4JLoggerFactory() {
    }

        /** failIfNOP 为 true 时拒绝 SLF4J NOP 空实现。 */
    Slf4JLoggerFactory(boolean failIfNOP) {
        assert failIfNOP; // Should be always called with true.
        if (LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory) {
            throw new NoClassDefFoundError("NOPLoggerFactory not supported");
        }
    }

        /** 获取 SLF4J Logger 并选择合适包装器。 */
    @Override
    public InternalLogger newInstance(String name) {
        return wrapLogger(LoggerFactory.getLogger(name));
    }

        /** 测试可见：LocationAware 则用 {@link LocationAwareSlf4JLogger}，否则 {@link Slf4JLogger}。 */
    static InternalLogger wrapLogger(Logger logger) {
        return logger instanceof LocationAwareLogger ?
                new LocationAwareSlf4JLogger((LocationAwareLogger) logger) : new Slf4JLogger(logger);
    }

        /** 供工厂探测使用：classpath 有 SLF4J 但拒绝 NOP 时返回实例。 */
    static InternalLoggerFactory getInstanceWithNopCheck() {
        return NopInstanceHolder.INSTANCE_WITH_NOP_CHECK;
    }

        /** 延迟持有 failIfNOP 单例，避免类加载顺序问题。 */
    private static final class NopInstanceHolder {
        private static final InternalLoggerFactory INSTANCE_WITH_NOP_CHECK = new Slf4JLoggerFactory(true);
    }
}
