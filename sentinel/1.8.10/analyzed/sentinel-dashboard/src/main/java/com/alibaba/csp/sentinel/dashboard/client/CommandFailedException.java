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
 * 控制台向 Sentinel 客户端发送命令失败时抛出的运行时异常。
 * <p>重写 {@link #fillInStackTrace()} 以抑制堆栈填充，降低高频失败时的开销。</p>
 *
 * @author Eric Zhao
 */
public class CommandFailedException extends RuntimeException {

    /** 构造无消息的命令失败异常。 */
    public CommandFailedException() {}

    /**
     * 构造带消息的命令失败异常。
     *
     * @param message 失败原因描述
     */
    public CommandFailedException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
