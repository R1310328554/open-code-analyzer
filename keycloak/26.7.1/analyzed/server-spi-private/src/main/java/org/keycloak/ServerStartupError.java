/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak;

/**
 * 服务器启动期间抛出的不可恢复错误。
 *
 * Non-recoverable error thrown during server startup
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ServerStartupError extends Error {

    /** 是否在 fillInStackTrace 时填充完整堆栈。 */
    private final boolean fillStackTrace;

    /** 构造启动错误，默认填充堆栈跟踪。 */
    public ServerStartupError(String message) {
        super(message);
        fillStackTrace = true;
    }

    /**
     * 构造启动错误。
     * @param message 错误消息
     * @param fillStackTrace 是否填充堆栈
     */
    public ServerStartupError(String message, boolean fillStackTrace) {
        super(message);
        this.fillStackTrace = fillStackTrace;
    }

    /** 按配置决定是否生成堆栈，用于抑制无意义的启动失败栈。 */
    @Override
    public synchronized Throwable fillInStackTrace() {
        if (fillStackTrace) {
            return super.fillInStackTrace();
        } else {
            return this;
        }
    }

}
