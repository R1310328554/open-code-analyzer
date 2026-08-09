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
package com.alibaba.csp.sentinel.command;

/**
 * 命令中心响应模型：封装成功标志、结果对象与可选异常。
 * 工厂方法 {@link #ofSuccess} 与 {@link #ofFailure} 区分正常与失败路径。
 *
 * @param <R> 结果类型
 * @author Eric Zhao
 */
public class CommandResponse<R> {

    private final boolean success;
    private final R result;
    private final Throwable exception;

    private CommandResponse(R result) {
        this(result, true, null);
    }

    private CommandResponse(R result, boolean success, Throwable exception) {
        this.success = success;
        this.result = result;
        this.exception = exception;
    }

    /**
     * 构造成功响应。
     *
     * @param result 结果对象
     * @param <T>    结果类型
     * @return 成功响应
     */
    public static <T> CommandResponse<T> ofSuccess(T result) {
        return new CommandResponse<T>(result);
    }

    /**
     * 构造失败响应（无附加结果）。
     *
     * @param ex 失败原因
     * @return 失败响应
     */
    public static <T> CommandResponse<T> ofFailure(Throwable ex) {
        return new CommandResponse<T>(null, false, ex);
    }

    /**
     * 构造失败响应，并附带额外结果（如错误提示文本）。
     *
     * @param ex     失败原因
     * @param result 附加结果
     * @return 失败响应
     */
    public static <T> CommandResponse<T> ofFailure(Throwable ex, T result) {
        return new CommandResponse<T>(result, false, ex);
    }

    public boolean isSuccess() {
        return success;
    }

    public R getResult() {
        return result;
    }

    public Throwable getException() {
        return exception;
    }
}
