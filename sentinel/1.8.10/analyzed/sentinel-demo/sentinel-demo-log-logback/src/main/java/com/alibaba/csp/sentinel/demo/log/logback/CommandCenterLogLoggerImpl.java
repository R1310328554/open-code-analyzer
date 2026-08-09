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
package com.alibaba.csp.sentinel.demo.log.logback;

import com.alibaba.csp.sentinel.transport.log.CommandCenterLog;
import com.alibaba.csp.sentinel.log.LogTarget;
import com.alibaba.csp.sentinel.log.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义 {@link CommandCenterLog} 日志实现，将 Sentinel 命令中心日志委托给 Logback。
 *
 * <ul>
 * <li>1. 实现 {@link Logger} SPI 接口</li>
 * <li>2. 用 {@link LogTarget} 指定日志类型</li>
 * <li>3. 实现各级别日志方法</li>
 * <li>4. 在 {@code resources/META-INF/services/com.alibaba.csp.sentinel.log.Logger} 中注册</li>
 * </ul>
 *
 * @author xue8
 */
@LogTarget(value = CommandCenterLog.LOGGER_NAME)
public class CommandCenterLogLoggerImpl implements Logger {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(CommandCenterLog.LOGGER_NAME);

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable e) {
        logger.info(msg, e);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    @Override
    public void warn(String msg, Throwable e) {
        logger.warn(msg, e);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable e) {
        logger.trace(msg, e);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable e) {
        logger.debug(msg, e);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable e) {
        logger.error(msg, e);
    }

}
