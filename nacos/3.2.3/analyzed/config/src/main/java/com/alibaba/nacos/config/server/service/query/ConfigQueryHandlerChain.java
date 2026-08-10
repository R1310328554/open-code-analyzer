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

package com.alibaba.nacos.config.server.service.query;

import com.alibaba.nacos.config.server.service.query.handler.ConfigQueryHandler;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainRequest;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * 配置查询责任链：按添加顺序串联多个 {@link ConfigQueryHandler}，
 * 由头节点依次处理 {@link ConfigQueryChainRequest} 并返回响应。
 * ConfigQueryHandlerChain.
 * @author Nacos
 */
public class ConfigQueryHandlerChain {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigQueryHandlerChain.class);
    
    private ConfigQueryHandler head;
    
    private ConfigQueryHandler tail;
    
    public ConfigQueryHandlerChain() {
    }
    
    /**
     * 向链尾追加处理器，支持链式调用；null 处理器将被忽略并打 warn 日志。
     *
     * @param handler the configuration query handler to be added
     * @return the current configuration query handler chain object, supporting method chaining
     */
    public ConfigQueryHandlerChain addHandler(ConfigQueryHandler handler) {
        if (Objects.isNull(handler)) {
            LOGGER.warn("Attempted to add a null config query handler");
            return this;
        }
        
        if (head == null) {
            head = handler;
            tail = handler;
        } else {
            tail.setNextHandler(handler);
            tail = handler;
        }
        
        return this;
    }
    
    /**
     * 从头节点开始执行责任链处理逻辑。
     *
     * @param request 统一查询请求
     * @return 链处理结果
     * @throws IOException IO 异常向上抛出
     */
    public ConfigQueryChainResponse handle(ConfigQueryChainRequest request) throws IOException {
        return head.handle(request);
    }
    
}
