/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.oidc;

import com.alibaba.nacos.plugin.auth.api.AuthResult;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.api.Resource;

/**
 * 身份校验提供者接口。
 *
 * <p>负责验证请求中的身份上下文是否有效，由具体 OIDC 实现（如 {@code OidcIdentityProvider}）完成校验逻辑。</p>
 *
 * @author WangzJi
 */
public interface IdentityProvider {
    
    /**
     * 校验身份上下文是否有效。
     *
     * @param identityContext 请求的身份上下文
     * @param resource        请求访问的资源
     * @return {@link AuthResult} 身份校验结果
     */
    AuthResult validateIdentity(IdentityContext identityContext, Resource resource);
}
