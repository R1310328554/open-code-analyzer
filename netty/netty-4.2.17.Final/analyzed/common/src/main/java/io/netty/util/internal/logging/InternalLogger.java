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
/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package io.netty.util.internal.logging;

/**
 * Netty 内部专用日志接口，<strong>禁止</strong>在 Netty 外部直接使用。
 * <p><em>Internal-use-only</em> logger used by Netty.  <strong>DO NOT</strong>
 * access this class outside of Netty.
 */
public interface InternalLogger {

    /**
     * Return the name of this {@link InternalLogger} instance.
     * <p>返回此日志器实例的名称。</p>
     *
     * @return name of this logger instance
     */
    String name();

    /**
     * Is the logger instance enabled for the TRACE level?
     * <p>是否启用 TRACE 级别。</p>
     *
     * @return True if this Logger is enabled for the TRACE level,
     *         false otherwise.
     */
    boolean isTraceEnabled();

    /**
     * Log a message at the TRACE level.
     * <p>以 TRACE 级别记录消息。</p>
     *
     * @param msg the message string to be logged
     */
    void trace(String msg);

    /**
     * Log a message at the TRACE level according to the specified format
     * and argument.
     * <p>TRACE 级别按格式与单个参数记录，禁用时避免多余对象创建。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     */
    void trace(String format, Object arg);

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     * <p>TRACE 级别双参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level. </p>
     *
     * @param format the format string
     * @param argA   the first argument
     * @param argB   the second argument
     */
    void trace(String format, Object argA, Object argB);

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     * <p>TRACE 级别可变参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the TRACE level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an {@code Object[]} before invoking the method,
     * even if this logger is disabled for TRACE. The variants taking {@link #trace(String, Object) one} and
     * {@link #trace(String, Object, Object) two} arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    void trace(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the TRACE level with an
     * accompanying message.
     * <p>TRACE 级别记录带消息的异常。</p>
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    void trace(String msg, Throwable t);

    /**
     * Log an exception (throwable) at the TRACE level.
     * <p>TRACE 级别仅记录异常。</p>
     *
     * @param t   the exception (throwable) to log
     */
    void trace(Throwable t);

    /**
     * Is the logger instance enabled for the DEBUG level?
     * <p>是否启用 DEBUG 级别。</p>
     *
     * @return True if this Logger is enabled for the DEBUG level,
     *         false otherwise.
     */
    boolean isDebugEnabled();

    /**
     * Log a message at the DEBUG level.
     * <p>以 DEBUG 级别记录消息。</p>
     *
     * @param msg the message string to be logged
     */
    void debug(String msg);

    /**
     * Log a message at the DEBUG level according to the specified format
     * and argument.
     * <p>DEBUG 级别单参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     */
    void debug(String format, Object arg);

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     * <p>DEBUG 级别双参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level. </p>
     *
     * @param format the format string
     * @param argA   the first argument
     * @param argB   the second argument
     */
    void debug(String format, Object argA, Object argB);

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     * <p>DEBUG 级别可变参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the DEBUG level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an {@code Object[]} before invoking the method,
     * even if this logger is disabled for DEBUG. The variants taking
     * {@link #debug(String, Object) one} and {@link #debug(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    void debug(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     * <p>DEBUG 级别记录带消息的异常。</p>
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    void debug(String msg, Throwable t);

    /**
     * Log an exception (throwable) at the DEBUG level.
     * <p>DEBUG 级别仅记录异常。</p>
     *
     * @param t   the exception (throwable) to log
     */
    void debug(Throwable t);

    /**
     * Is the logger instance enabled for the INFO level?
     * <p>是否启用 INFO 级别。</p>
     *
     * @return True if this Logger is enabled for the INFO level,
     *         false otherwise.
     */
    boolean isInfoEnabled();

    /**
     * Log a message at the INFO level.
     * <p>以 INFO 级别记录消息。</p>
     *
     * @param msg the message string to be logged
     */
    void info(String msg);

    /**
     * Log a message at the INFO level according to the specified format
     * and argument.
     * <p>INFO 级别单参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the INFO level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     */
    void info(String format, Object arg);

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     * <p>INFO 级别双参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the INFO level. </p>
     *
     * @param format the format string
     * @param argA   the first argument
     * @param argB   the second argument
     */
    void info(String format, Object argA, Object argB);

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     * <p>INFO 级别可变参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the INFO level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an {@code Object[]} before invoking the method,
     * even if this logger is disabled for INFO. The variants taking
     * {@link #info(String, Object) one} and {@link #info(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    void info(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     * <p>INFO 级别记录带消息的异常。</p>
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    void info(String msg, Throwable t);

    /**
     * Log an exception (throwable) at the INFO level.
     * <p>INFO 级别仅记录异常。</p>
     *
     * @param t   the exception (throwable) to log
     */
    void info(Throwable t);

    /**
     * Is the logger instance enabled for the WARN level?
     * <p>是否启用 WARN 级别。</p>
     *
     * @return True if this Logger is enabled for the WARN level,
     *         false otherwise.
     */
    boolean isWarnEnabled();

    /**
     * Log a message at the WARN level.
     * <p>以 WARN 级别记录消息。</p>
     *
     * @param msg the message string to be logged
     */
    void warn(String msg);

    /**
     * Log a message at the WARN level according to the specified format
     * and argument.
     * <p>WARN 级别单参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the WARN level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     */
    void warn(String format, Object arg);

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     * <p>WARN 级别可变参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the WARN level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an {@code Object[]} before invoking the method,
     * even if this logger is disabled for WARN. The variants taking
     * {@link #warn(String, Object) one} and {@link #warn(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    void warn(String format, Object... arguments);

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     * <p>WARN 级别双参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the WARN level. </p>
     *
     * @param format the format string
     * @param argA   the first argument
     * @param argB   the second argument
     */
    void warn(String format, Object argA, Object argB);

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     * <p>WARN 级别记录带消息的异常。</p>
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    void warn(String msg, Throwable t);

    /**
     * Log an exception (throwable) at the WARN level.
     * <p>WARN 级别仅记录异常。</p>
     *
     * @param t   the exception (throwable) to log
     */
    void warn(Throwable t);

    /**
     * Is the logger instance enabled for the ERROR level?
     * <p>是否启用 ERROR 级别。</p>
     *
     * @return True if this Logger is enabled for the ERROR level,
     *         false otherwise.
     */
    boolean isErrorEnabled();

    /**
     * Log a message at the ERROR level.
     * <p>以 ERROR 级别记录消息。</p>
     *
     * @param msg the message string to be logged
     */
    void error(String msg);

    /**
     * Log a message at the ERROR level according to the specified format
     * and argument.
     * <p>ERROR 级别单参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     */
    void error(String format, Object arg);

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     * <p>ERROR 级别双参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level. </p>
     *
     * @param format the format string
     * @param argA   the first argument
     * @param argB   the second argument
     */
    void error(String format, Object argA, Object argB);

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     * <p>ERROR 级别可变参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the ERROR level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an {@code Object[]} before invoking the method,
     * even if this logger is disabled for ERROR. The variants taking
     * {@link #error(String, Object) one} and {@link #error(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    void error(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     * <p>ERROR 级别记录带消息的异常。</p>
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    void error(String msg, Throwable t);

    /**
     * Log an exception (throwable) at the ERROR level.
     * <p>ERROR 级别仅记录异常。</p>
     *
     * @param t   the exception (throwable) to log
     */
    void error(Throwable t);

    /**
     * Is the logger instance enabled for the specified {@code level}?
     * <p>检查指定 {@link InternalLogLevel} 是否启用。</p>
     *
     * @return True if this Logger is enabled for the specified {@code level},
     *         false otherwise.
     */
    boolean isEnabled(InternalLogLevel level);

    /**
     * Log a message at the specified {@code level}.
     * <p>按指定级别记录消息。</p>
     *
     * @param msg the message string to be logged
     */
    void log(InternalLogLevel level, String msg);

    /**
     * Log a message at the specified {@code level} according to the specified format
     * and argument.
     * <p>指定级别单参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the specified {@code level}. </p>
     *
     * @param format the format string
     * @param arg    the argument
     */
    void log(InternalLogLevel level, String format, Object arg);

    /**
     * Log a message at the specified {@code level} according to the specified format
     * and arguments.
     * <p>指定级别双参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the specified {@code level}. </p>
     *
     * @param format the format string
     * @param argA   the first argument
     * @param argB   the second argument
     */
    void log(InternalLogLevel level, String format, Object argA, Object argB);

    /**
     * Log a message at the specified {@code level} according to the specified format
     * and arguments.
     * <p>指定级别可变参数格式化记录。</p>
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the specified {@code level}. However, this variant incurs the hidden
     * (and relatively small) cost of creating an {@code Object[]} before invoking the method,
     * even if this logger is disabled for the specified {@code level}. The variants taking
     * {@link #log(InternalLogLevel, String, Object) one} and
     * {@link #log(InternalLogLevel, String, Object, Object) two} arguments exist solely
     * in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    void log(InternalLogLevel level, String format, Object... arguments);

    /**
     * Log an exception (throwable) at the specified {@code level} with an
     * accompanying message.
     * <p>指定级别记录带消息的异常。</p>
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    void log(InternalLogLevel level, String msg, Throwable t);

    /**
     * Log an exception (throwable) at the specified {@code level}.
     * <p>指定级别仅记录异常。</p>
     *
     * @param t   the exception (throwable) to log
     */
    void log(InternalLogLevel level, Throwable t);
}
