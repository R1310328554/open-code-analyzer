/*
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
 */

package com.alibaba.nacos.auth.parser.grpc;

import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.auth.parser.AbstractResourceParser;
import com.alibaba.nacos.plugin.auth.constant.Constants;

import java.util.Properties;

/**
 * gRPC 资源解析器抽象基类。
 *
 * <p>面向 {@link Request} 类型的远程调用请求，在扩展属性中记录请求类的简单类名，
 * 便于授权插件区分不同 RPC 操作类型。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractGrpcResourceParser extends AbstractResourceParser<Request> {
    
    /**
     * 构建 gRPC 请求的扩展属性，写入请求类名。
     *
     * @param request gRPC 远程请求
     * @return 包含 {@code REQUEST_CLASS} 的扩展属性
     */
    @Override
    protected Properties getProperties(Request request) {
        Properties properties = new Properties();
        // 记录请求类型，供授权插件按 RPC 类名做细粒度策略
        properties.setProperty(Constants.Resource.REQUEST_CLASS,
            request.getClass().getSimpleName());
        return properties;
    }
}
