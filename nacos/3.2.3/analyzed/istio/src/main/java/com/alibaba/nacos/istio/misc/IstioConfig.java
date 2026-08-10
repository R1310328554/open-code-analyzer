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

package com.alibaba.nacos.istio.misc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Istio 集成配置项：MCP 服务开关/端口、全量推送、防抖参数及域名后缀等。
 *
 * <p>值来自 Spring {@code @Value} 注入的 {@code nacos.istio.*} 属性。</p>
 *
 * @author nkorange
 * @since 1.1.4
 */
@Component
public class IstioConfig {
    
    /** 是否启用 MCP gRPC 服务端。 */
    @Value("${nacos.istio.mcp.server.enabled:false}")
    private boolean serverEnabled = false;
    /** MCP gRPC 监听端口（默认 18848）。 */
    @Value("${nacos.istio.mcp.server.port:18848}")
    private int serverPort = 18848;
    
    /** 是否默认以全量方式推送 Istio 资源。 */
    @Value("${nacos.istio.server.full:true}")
    private boolean fullEnabled = true;
    
    /** 推送防抖最大等待毫秒数（超时强制推送）。 */
    @Value("${nacos.istio.debounce.max:5000}")
    private long debounceMax;
    
    /** 防抖静默窗口毫秒数（无新事件后延迟推送）。 */
    @Value("${nacos.istio.debounce.after:100}")
    private long debounceAfter;
    
    /** 生成 ServiceEntry 主机名时使用的域名后缀。 */
    @Value("${nacos.istio.domain.suffix:nacos}")
    private String domainSuffix;
    
    public boolean isServerEnabled() {
        return serverEnabled;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    public String getDomainSuffix() {
        return domainSuffix;
    }
    
    public boolean isFullEnabled() {
        return fullEnabled;
    }
    
    public long getDebounceMax() {
        return debounceMax;
    }
    
    public long getDebounceAfter() {
        return debounceAfter;
    }
}
