/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.context;

import com.alibaba.nacos.core.context.addition.AuthContext;
import com.alibaba.nacos.core.context.addition.BasicContext;
import com.alibaba.nacos.core.context.addition.EngineContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Nacos 请求上下文：聚合单次请求的基础信息、引擎环境、鉴权结果及扩展槽位，供 HTTP/gRPC 链路中的过滤器、拦截器与日志追踪共享。
 * Nacos request context.
 *
 * @author xiweng.yy
 */
public class RequestContext {
    
    /**
     * 可选请求标识。
     * <ul>
     *     <li>HTTP 请求通常未携带，将自动生成 UUID。</li>
     *     <li>gRPC 请求与真实 request id 保持一致。</li>
     * </ul>
     */
    private String requestId;
    
    /** 请求进入上下文时的时间戳（毫秒）。 */
    private final long requestTimestamp;
    
    /** 协议、地址、UA 等基础请求信息。 */
    private final BasicContext basicContext;
    
    /** 服务端版本与引擎级扩展键值。 */
    private final EngineContext engineContext;
    
    /** 鉴权插件所需的身份、资源与校验结果。 */
    private final AuthContext authContext;
    
    /** 业务模块可挂载的扩展上下文映射。 */
    private final Map<String, Object> extensionContexts;
    
    /** 包内构造：初始化各子上下文并生成默认 requestId。 */
    RequestContext(long requestTimestamp) {
        this.requestId = UUID.randomUUID().toString();
        this.requestTimestamp = requestTimestamp;
        this.basicContext = new BasicContext();
        this.engineContext = new EngineContext();
        this.authContext = new AuthContext();
        this.extensionContexts = new HashMap<>(1);
    }
    
    /** 覆盖请求标识（如 gRPC 传入真实 id）。 */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    /** 返回当前请求标识。 */
    public String getRequestId() {
        return requestId;
    }
    
    /** 返回请求起始时间戳。 */
    public long getRequestTimestamp() {
        return requestTimestamp;
    }
    
    /** 获取基础请求上下文。 */
    public BasicContext getBasicContext() {
        return basicContext;
    }
    
    /** 获取引擎/环境上下文。 */
    public EngineContext getEngineContext() {
        return engineContext;
    }
    
    /** 获取鉴权上下文。 */
    public AuthContext getAuthContext() {
        return authContext;
    }
    
    /** 按 key 读取扩展上下文对象。 */
    public Object getExtensionContext(String key) {
        return extensionContexts.get(key);
    }
    
    /** 写入或覆盖扩展上下文条目。 */
    public void addExtensionContext(String key, Object value) {
        extensionContexts.put(key, value);
    }
}
