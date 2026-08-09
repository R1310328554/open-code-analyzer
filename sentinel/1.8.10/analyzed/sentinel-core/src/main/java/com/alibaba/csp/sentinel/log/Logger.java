/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.log;

/**
 * <p>Sentinel 通用 Logger SPI 接口。</p>
 * <p>注意：占位符仅支持最常见的 slf4j 约定（{@code {}}）。
 * 若未使用 slf4j，应实现兼容 {@code {}} 占位符的适配器。</p>
 *
 * @author xue8
 * @since 1.7.2
 */
public interface Logger {

    /**
     * 按指定格式与参数记录 INFO 级别日志。
     *
     * @param format    格式字符串
     * @param arguments 参数列表
     */
    void info(String format, Object... arguments);

    /**
     * 记录 INFO 级别日志，附带异常信息。
     *
     * @param msg 伴随异常的消息
     * @param e   要记录的异常（Throwable）
     */
    void info(String msg, Throwable e);

    /**
     * 按指定格式与参数记录 WARN 级别日志。
     *
     * @param format    格式字符串
     * @param arguments 参数列表
     */
    void warn(String format, Object... arguments);

    /**
     * 记录 WARN 级别日志，附带异常信息。
     *
     * @param msg 伴随异常的消息
     * @param e   要记录的异常（Throwable）
     */
    void warn(String msg, Throwable e);

    /**
     * 按指定格式与参数记录 TRACE 级别日志。
     *
     * @param format    格式字符串
     * @param arguments 参数列表
     */
    void trace(String format, Object... arguments);

    /**
     * 记录 TRACE 级别日志，附带异常信息。
     *
     * @param msg 伴随异常的消息
     * @param e   要记录的异常（Throwable）
     */
    void trace(String msg, Throwable e);

    /**
     * 按指定格式与参数记录 DEBUG 级别日志。
     *
     * @param format    格式字符串
     * @param arguments 参数列表
     */
    void debug(String format, Object... arguments);

    /**
     * 记录 DEBUG 级别日志，附带异常信息。
     *
     * @param msg 伴随异常的消息
     * @param e   要记录的异常（Throwable）
     */
    void debug(String msg, Throwable e);

    /**
     * 按指定格式与参数记录 ERROR 级别日志。
     *
     * @param format    格式字符串
     * @param arguments 参数列表
     */
    void error(String format, Object... arguments);

    /**
     * 记录 ERROR 级别日志，附带异常信息。
     *
     * @param msg 伴随异常的消息
     * @param e   要记录的异常（Throwable）
     */
    void error(String msg, Throwable e);

}
