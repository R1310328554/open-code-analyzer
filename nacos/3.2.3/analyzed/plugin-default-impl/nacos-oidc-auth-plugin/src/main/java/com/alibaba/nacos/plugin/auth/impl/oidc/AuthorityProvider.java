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
import com.alibaba.nacos.plugin.auth.api.Permission;

/**
 * OIDC 权限校验提供者接口。
 *
 * <p>根据请求身份上下文与目标权限判定是否允许访问，由具体 OIDC 实现（如 {@code OidcAuthorityProvider}）完成策略逻辑。</p>
 *
 * @author WangzJi
 */
public interface AuthorityProvider {
    
    /**
     * 校验调用方是否具备指定权限。
     *
     * @param identityContext 请求身份上下文
     * @param permission      待校验的权限对象
     * @return {@link AuthResult} 鉴权结果（允许或拒绝及原因）
     */
    AuthResult validateAuthority(IdentityContext identityContext, Permission permission);
}
