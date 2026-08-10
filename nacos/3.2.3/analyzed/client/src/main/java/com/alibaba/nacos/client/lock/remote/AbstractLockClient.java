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

package com.alibaba.nacos.client.lock.remote;

import com.alibaba.nacos.client.security.SecurityProxy;
import com.alibaba.nacos.client.utils.AppNameUtils;
import com.alibaba.nacos.plugin.auth.api.RequestResource;

import java.util.HashMap;
import java.util.Map;

/**
 * 锁远程客户端抽象基类。
 *
 * <p>封装 {@link SecurityProxy} 鉴权头与应用名头的组装，供 gRPC 等具体实现复用。</p>
 *
 * @author 985492783@qq.com
 * @description AbstractLockClient
 * @date 2023/6/28 17:19
 */
public abstract class AbstractLockClient implements LockClient {
    
    /** 鉴权代理，提供身份上下文请求头。 */
    private final SecurityProxy securityProxy;
    
    /** 应用名字段键。 */
    private static final String APP_FILED = "app";
    
    /**
     * 注入安全代理。
     *
     * @param securityProxy 鉴权代理实例
     */
    protected AbstractLockClient(SecurityProxy securityProxy) {
        this.securityProxy = securityProxy;
    }
    
    /**
     * 组装 RPC 请求所需的鉴权与应用请求头。
     *
     * @return 合并后的请求头映射
     */
    protected Map<String, String> getSecurityHeaders() {
        RequestResource resource = RequestResource.lockBuilder().build();
        Map<String, String> result = this.securityProxy.getIdentityContext(resource);
        result.putAll(getAppHeaders());
        return result;
    }
    
    /**
     * 构建仅含应用名的请求头。
     *
     * @return 含 {@code app} 字段的单元素映射
     */
    protected Map<String, String> getAppHeaders() {
        Map<String, String> result = new HashMap<>(1);
        result.put(APP_FILED, AppNameUtils.getAppName());
        return result;
    }
}
