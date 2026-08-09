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
 * 命令处理器拦截器：在指定命令执行前后插入自定义逻辑，可通过 SPI 扩展。
 * 多个拦截器按 SPI 排序后组成责任链，由 {@link InterceptingCommandHandler} 驱动。
 *
 * @author icodening
 * @since 1.8.4
 * @see com.alibaba.csp.sentinel.spi.SpiLoader
 * @see com.alibaba.csp.sentinel.spi.Spi
 */
public interface CommandHandlerInterceptor<R> {

    /**
     * 判断是否拦截指定命令。
     *
     * @param commandName 命令名称，例如 getRules
     * @return true 表示参与拦截，false 表示跳过
     */
    boolean shouldIntercept(String commandName);

    /**
     * 拦截命令请求：可调用 {@code execution.execute(request)} 继续责任链，或直接返回响应。
     *
     * @param request   命令请求
     * @param execution 拦截器链执行器
     * @return 命令响应
     */
    CommandResponse<R> intercept(CommandRequest request, CommandRequestExecution<R> execution);

}
