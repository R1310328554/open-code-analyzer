/*
 *
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.core.control.remote;

import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;

/**
 * 远程 RPC TPS 校验请求解析器抽象基类：构造时自动注册到 {@link RemoteTpsCheckRequestParserRegistry}，子类实现从 {@link Request} 提取限流维度。
 * remote tps check request parser.
 *
 * @author shiyiyue
 */
public abstract class RemoteTpsCheckRequestParser {
    
    /** 构造并自动向注册表登记本解析器。 */
    public RemoteTpsCheckRequestParser() {
        RemoteTpsCheckRequestParserRegistry.register(this);
    }
    
    /**
     * 将 RPC 请求解析为 TPS 校验请求。
     *
     * @param request RPC 请求体
     * @param meta 请求元数据（连接、来源等）
     * @return 限流校验请求，无法解析时可返回 null
     */
    public abstract TpsCheckRequest parse(Request request, RequestMeta meta);
    
    /**
     * 返回 TPS 切点名称，对应 {@link TpsControl#pointName()}。
     *
     * @return 切点名称
     */
    public abstract String getPointName();
    
    /**
     * 返回解析器注册名，对应 {@link TpsControl#name()} 或切点名。
     *
     * @return 解析器唯一名称
     */
    public abstract String getName();
}
