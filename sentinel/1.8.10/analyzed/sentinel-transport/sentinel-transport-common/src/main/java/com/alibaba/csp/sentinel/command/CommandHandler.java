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
 * 命令处理器接口：将 {@link CommandRequest} 转换为 {@link CommandResponse}。
 * 各具体命令通过 SPI 注册并由 {@link CommandHandlerProvider} 统一发现。
 *
 * @author Eric Zhao
 */
public interface CommandHandler<R> {

    /**
     * 处理命令中心传入的请求并返回响应。
     *
     * @param request 待处理的命令请求
     * @return 成功或失败的命令响应
     */
    CommandResponse<R> handle(CommandRequest request);
}
