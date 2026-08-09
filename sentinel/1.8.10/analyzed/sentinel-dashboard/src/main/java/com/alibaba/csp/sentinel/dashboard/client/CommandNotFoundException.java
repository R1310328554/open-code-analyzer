/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.client;

/**
 * 客户端命令未找到异常。
 * <p>当 Sentinel 客户端不支持或未注册某 API 命令时抛出；
 * {@link #fillInStackTrace()} 被重写为空操作以降低热路径开销。
 *
 * @author Eric Zhao
 * @since 0.2.1
 */
public class CommandNotFoundException extends Exception {

    /** 无参构造。 */
    public CommandNotFoundException() { }

    /** @param message 异常描述 */
    public CommandNotFoundException(String message) {
        super(message);
    }

    /** 不填充堆栈，避免频繁创建异常时的性能损耗。 */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
