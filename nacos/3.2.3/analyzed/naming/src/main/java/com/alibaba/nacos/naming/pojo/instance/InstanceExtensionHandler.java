/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.pojo.instance;

import com.alibaba.nacos.api.naming.pojo.Instance;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 实例扩展信息 SPI 处理器接口。
 *
 * <p>供 1.x 客户端注册/心跳请求携带的额外字段扩展；由 {@link HttpRequestInstanceBuilder} 与 {@link BeatInfoInstanceBuilder} 链式调用。</p>
 *
 * @author xiweng.yy
 */
public interface InstanceExtensionHandler {
    
    /**
     * 从 HTTP 请求读取并缓存扩展配置信息。
     *
     * @param request http request
     */
    void configExtensionInfoFromRequest(HttpServletRequest request);
    
    /**
     * 将扩展信息写入待构建的 {@link Instance} 对象。
     *
     * @param needHandleInstance instance needed to be handled.
     */
    void handleExtensionInfo(Instance needHandleInstance);
}
