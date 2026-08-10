/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.service.query.handler;

import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainRequest;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainResponse;

import java.io.IOException;

/**
 * 配置查询责任链处理器接口：定义名称、处理逻辑及链式传递方法。
 * Configuration Query Handler Interface.
 * This interface defines the standard methods for handling configuration query requests.
 *
 * @author Nacos
 */
public interface ConfigQueryHandler {
    
    /**
     * 获取处理器唯一名称，用于日志与链路追踪。
     * Gets the name of the handler.
     * @return The name of the handler.
     */
    String getName();
    
    /**
     * 处理配置查询请求；无法处理时应委托 {@link #getNextHandler()} 或返回对应状态。
     * Handles the configuration query request.
     * If the current handler cannot process the request, it should throw an IOException.
     * @param request The configuration query request.
     * @return The response to the configuration query.
     * @throws IOException If an I/O error occurs.
     */
    ConfigQueryChainResponse handle(ConfigQueryChainRequest request) throws IOException;
    
    /**
     * 设置责任链中的下一个处理器。
     * Sets the next handler in the chain.
     * @param nextHandler The next handler to which the request can be passed if the current handler cannot process it.
     */
    void setNextHandler(ConfigQueryHandler nextHandler);
    
    /**
     * 获取责任链中的下一个处理器。
     * Gets the next handler in the chain.
     * @return The next handler.
     */
    ConfigQueryHandler getNextHandler();
}
