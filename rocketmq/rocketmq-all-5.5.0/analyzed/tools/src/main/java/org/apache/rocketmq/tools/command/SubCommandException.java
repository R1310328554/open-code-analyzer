/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.tools.command;

/**
 * mqadmin 子命令执行异常：封装命令失败时的错误信息。
 */
public class SubCommandException extends Exception {
    private static final long serialVersionUID = 0L;

    /**
     * @param msg 错误消息。
     */
    public SubCommandException(String msg) {
        super(msg);
    }

    /** 使用 {@link String#format} 构造格式化错误消息。 */
    public SubCommandException(String format, Object... args) {
        super(String.format(format, args));
    }

    /**
     * @param msg 错误消息。
     * @param cause 根因异常。
     */
    public SubCommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
