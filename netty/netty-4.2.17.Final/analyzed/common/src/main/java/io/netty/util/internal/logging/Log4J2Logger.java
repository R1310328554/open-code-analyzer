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


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;

import java.security.AccessController;
import java.security.PrivilegedAction;

import static io.netty.util.internal.logging.AbstractInternalLogger.EXCEPTION_MESSAGE;

/**
 * Log4J2 {@link ExtendedLoggerWrapper} 适配器，实现 {@link InternalLogger}。
 * 启动时检测 API 版本，过旧则拒绝使用。
 */
class Log4J2Logger extends ExtendedLoggerWrapper implements InternalLogger {

    private static final long serialVersionUID = 5485418394879791397L;
        /** true 表示 Log4J2 过旧，仅支持 format+varargs 签名。 */
    private static final boolean VARARGS_ONLY;

    static {
                // 旧版 Log4J2 仅有 format+varargs 的 log 方法 So we should not use
        // Log4J2 if the version is too old.
        // See https://github.com/netty/netty/issues/8217
        VARARGS_ONLY = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                try {
                    Logger.class.getMethod("debug", String.class, Object.class);
                    return false;
                } catch (NoSuchMethodException ignore) {
                                        // Log4J2 版本过旧
                    return true;
                } catch (SecurityException ignore) {
                                        // 无法检测版本时仍尝试使用 classpath 上的 Log4J2
                    return false;
                }
            }
        });
    }

        /** 包装 Log4J2 Logger；版本不匹配则抛异常。 */
    Log4J2Logger(Logger logger) {
        super((ExtendedLogger) logger, logger.getName(), logger.getMessageFactory());
        if (VARARGS_ONLY) {
            throw new UnsupportedOperationException("Log4J2 version mismatch");
        }
    }

        /** 返回 Log4J2 logger 名称。 */
    @Override
    public String name() {
        return getName();
    }

        // --- 各级别仅记录异常（使用默认消息） ---
    @Override
    public void trace(Throwable t) {
        log(Level.TRACE, EXCEPTION_MESSAGE, t);
    }

    @Override
    public void debug(Throwable t) {
        log(Level.DEBUG, EXCEPTION_MESSAGE, t);
    }

    @Override
    public void info(Throwable t) {
        log(Level.INFO, EXCEPTION_MESSAGE, t);
    }

    @Override
    public void warn(Throwable t) {
        log(Level.WARN, EXCEPTION_MESSAGE, t);
    }

    @Override
    public void error(Throwable t) {
        log(Level.ERROR, EXCEPTION_MESSAGE, t);
    }

        /** 将 {@link InternalLogLevel} 转为 Log4J2 {@link Level} 并检查是否启用。 */
    @Override
    public boolean isEnabled(InternalLogLevel level) {
        return isEnabled(toLevel(level));
    }

        /** 指定内部级别记录纯文本消息。 */
    @Override
    public void log(InternalLogLevel level, String msg) {
        log(toLevel(level), msg);
    }

    @Override
    public void log(InternalLogLevel level, String format, Object arg) {
        log(toLevel(level), format, arg);
    }

    @Override
    public void log(InternalLogLevel level, String format, Object argA, Object argB) {
        log(toLevel(level), format, argA, argB);
    }

    @Override
    public void log(InternalLogLevel level, String format, Object... arguments) {
        log(toLevel(level), format, arguments);
    }

    @Override
    public void log(InternalLogLevel level, String msg, Throwable t) {
        log(toLevel(level), msg, t);
    }

    @Override
    public void log(InternalLogLevel level, Throwable t) {
        log(toLevel(level), EXCEPTION_MESSAGE, t);
    }

        /** {@link InternalLogLevel} → Log4J2 {@link Level} 映射。 */
    private static Level toLevel(InternalLogLevel level) {
        switch (level) {
            case INFO:
                return Level.INFO;
            case DEBUG:
                return Level.DEBUG;
            case WARN:
                return Level.WARN;
            case ERROR:
                return Level.ERROR;
            case TRACE:
                return Level.TRACE;
            default:
                throw new Error("Unexpected log level: " + level);
        }
    }
}
