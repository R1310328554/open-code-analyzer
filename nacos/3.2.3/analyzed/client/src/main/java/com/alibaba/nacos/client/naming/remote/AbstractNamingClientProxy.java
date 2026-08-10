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

package com.alibaba.nacos.client.naming.remote;

import com.alibaba.nacos.plugin.auth.api.RequestResource;
import com.alibaba.nacos.client.address.ServerListChangeEvent;
import com.alibaba.nacos.client.security.SecurityProxy;
import com.alibaba.nacos.client.utils.AppNameUtils;
import com.alibaba.nacos.common.notify.listener.Subscriber;

import java.util.HashMap;
import java.util.Map;

/**
 * 命名客户端远程代理抽象基类。
 *
 * <p>封装鉴权请求头与应用名头，并订阅 {@link ServerListChangeEvent} 以响应服务端列表变更。具体 RPC 由 HTTP/gRPC 子类实现。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractNamingClientProxy extends Subscriber<ServerListChangeEvent>
    implements NamingClientProxy {
    
    /** HTTP 头中应用名字段键。 */
    private static final String APP_FILED = "app";
    
    /** 安全代理，提供身份上下文与重新登录。 */
    private final SecurityProxy securityProxy;
    
    protected AbstractNamingClientProxy(SecurityProxy securityProxy) {
        this.securityProxy = securityProxy;
    }
    
    /** 构建含命名空间/分组/服务资源的鉴权头，并附加应用名。 */
    protected Map<String, String> getSecurityHeaders(String namespace, String group,
        String serviceName) {
        RequestResource resource =
            RequestResource.namingBuilder().setNamespace(namespace).setGroup(group)
                .setResource(serviceName).build();
        Map<String, String> result = this.securityProxy.getIdentityContext(resource);
        result.putAll(getAppHeaders());
        return result;
    }
    
    /** 返回仅含客户端应用名的请求头。 */
    protected Map<String, String> getAppHeaders() {
        Map<String, String> result = new HashMap<>(1);
        result.put(APP_FILED, AppNameUtils.getAppName());
        return result;
    }
    
    /** 触发安全代理重新登录（凭证过期时）。 */
    protected void reLogin() {
        securityProxy.reLogin();
    }
}
