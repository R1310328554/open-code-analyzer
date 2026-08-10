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

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * A skeletal implementation of {@link InternalLogger}.  This class implements
 * all methods that have a {@link InternalLogLevel} parameter by default to call
 * specific logger methods such as {@link #info(String)} or {@link #isInfoEnabled()}.
 *
 * <p>InternalLogger 骨架实现：按 InternalLogLevel 分派到 trace/debug/info/warn/error。</p>
 */
public abstract class AbstractInternalLogger implements InternalLogger, Serializable {

    private static final long serialVersionUID = -6382972526573193470L;

    /** 仅传 Throwable 时使用的默认消息。 */
    static final String EXCEPTION_MESSAGE = "Unexpected exception:";

    /** 日志器名称。 */
    private final String name;

    /**
     * Creates a new instance.
     *
     * <p>构造并保存日志器名称。</p>
     */
    protected AbstractInternalLogger(String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
    }

    @Override
    public String name() {
        return name;
    }

    /** 判断指定 InternalLogLevel 是否启用。 */
    @Override
    public boolean isEnabled(InternalLogLevel level) {
        switch (level) {
        case TRACE:
            return isTraceEnabled();
        case DEBUG:
            return isDebugEnabled();
        case INFO:
            return isInfoEnabled();
        case WARN:
            return isWarnEnabled();
        case ERROR:
            return isErrorEnabled();
        default:
            throw new Error("Unexpected log level: " + level);
        }
    }

    /** 以默认消息记录 TRACE 级别异常。 */
    @Override
    public void trace(Throwable t) {
        trace(EXCEPTION_MESSAGE, t);
    }

    /** 以默认消息记录 DEBUG 级别异常。 */
    @Override
    public void debug(Throwable t) {
        debug(EXCEPTION_MESSAGE, t);
    }

    /** 以默认消息记录 INFO 级别异常。 */
    @Override
    public void info(Throwable t) {
        info(EXCEPTION_MESSAGE, t);
    }

    /** 以默认消息记录 WARN 级别异常。 */
    @Override
    public void warn(Throwable t) {
        warn(EXCEPTION_MESSAGE, t);
    }

    /** 以默认消息记录 ERROR 级别异常。 */
    @Override
    public void error(Throwable t) {
        error(EXCEPTION_MESSAGE, t);
    }

    /** 按级别记录带异常的消息。 */
    @Override
    public void log(InternalLogLevel level, String msg, Throwable cause) {
        switch (level) {
        case TRACE:
            trace(msg, cause);
            break;
        case DEBUG:
            debug(msg, cause);
            break;
        case INFO:
            info(msg, cause);
            break;
        case WARN:
            warn(msg, cause);
            break;
        case ERROR:
            error(msg, cause);
            break;
        default:
            throw new Error("Unexpected log level: " + level);
        }
    }

    /** 按级别仅记录异常。 */
    @Override
    public void log(InternalLogLevel level, Throwable cause) {
        switch (level) {
            case TRACE:
                trace(cause);
                break;
            case DEBUG:
                debug(cause);
                break;
            case INFO:
                info(cause);
                break;
            case WARN:
                warn(cause);
                break;
            case ERROR:
                error(cause);
                break;
            default:
                throw new Error("Unexpected log level: " + level);
        }
    }

    /** 按级别记录消息。 */
    @Override
    public void log(InternalLogLevel level, String msg) {
        switch (level) {
        case TRACE:
            trace(msg);
            break;
        case DEBUG:
            debug(msg);
            break;
        case INFO:
            info(msg);
            break;
        case WARN:
            warn(msg);
            break;
        case ERROR:
            error(msg);
            break;
        default:
            throw new Error("Unexpected log level: " + level);
        }
    }

    /** 按级别记录带一个占位参数的消息。 */
    @Override
    public void log(InternalLogLevel level, String format, Object arg) {
        switch (level) {
        case TRACE:
            trace(format, arg);
            break;
        case DEBUG:
            debug(format, arg);
            break;
        case INFO:
            info(format, arg);
            break;
        case WARN:
            warn(format, arg);
            break;
        case ERROR:
            error(format, arg);
            break;
        default:
            throw new Error("Unexpected log level: " + level);
        }
    }

    /** 按级别记录带两个占位参数的消息。 */
    @Override
    public void log(InternalLogLevel level, String format, Object argA, Object argB) {
        switch (level) {
        case TRACE:
            trace(format, argA, argB);
            break;
        case DEBUG:
            debug(format, argA, argB);
            break;
        case INFO:
            info(format, argA, argB);
            break;
        case WARN:
            warn(format, argA, argB);
            break;
        case ERROR:
            error(format, argA, argB);
            break;
        default:
            throw new Error("Unexpected log level: " + level);
        }
    }

    /** 按级别记录带可变参数的消息。 */
    @Override
    public void log(InternalLogLevel level, String format, Object... arguments) {
        switch (level) {
        case TRACE:
            trace(format, arguments);
            break;
        case DEBUG:
            debug(format, arguments);
            break;
        case INFO:
            info(format, arguments);
            break;
        case WARN:
            warn(format, arguments);
            break;
        case ERROR:
            error(format, arguments);
            break;
        default:
            throw new Error("Unexpected log level: " + level);
        }
    }

    /** 反序列化时通过工厂恢复单例 logger。 */
    protected Object readResolve() throws ObjectStreamException {
        return InternalLoggerFactory.getInstance(name());
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + '(' + name() + ')';
    }
}
