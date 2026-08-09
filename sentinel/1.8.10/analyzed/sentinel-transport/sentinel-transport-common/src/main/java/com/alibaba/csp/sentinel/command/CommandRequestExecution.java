/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.command;

/**
 * 命令请求执行器：拦截器链中用于将控制权传递给下一环或最终处理器。
 *
 * @author icodening
 * @since 1.8.4
 */
public interface CommandRequestExecution<R> {

    /**
     * 继续执行拦截器链或底层 {@link CommandHandler}，返回最终响应。
     *
     * @param request 命令请求
     * @return 命令响应
     */
    CommandResponse<R> execute(CommandRequest request);
}
