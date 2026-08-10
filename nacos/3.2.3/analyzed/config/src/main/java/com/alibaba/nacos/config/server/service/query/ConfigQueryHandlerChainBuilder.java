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

/**
 * 配置查询责任链构建器 SPI：按业务场景组装 Handler 顺序，
 * 由 {@code nacos.config.query.chain.builder} 选择具体实现。
 * ConfigQueryHandlerChainBuilder.
 *
 * @author Nacos
 */
public interface ConfigQueryHandlerChainBuilder {
    
    /**
     * 构建并返回完整的配置查询 Handler 责任链。
     *
     * @return the configuration query handler chain
     */
    ConfigQueryHandlerChain build();
    
    /**
     * 返回构建器 SPI 名称，供 {@link ConfigQueryChainService} 过滤加载。
     *
     * @return the name of the builder
     */
    String getName();
}
