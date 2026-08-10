/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.securityprofile;

import java.util.List;

import org.keycloak.provider.Provider;
import org.keycloak.representations.idm.ClientPolicyRepresentation;
import org.keycloak.representations.idm.ClientProfileRepresentation;

/**
 * 安全配置文件提供者：在 Keycloak 环境中强制执行最低安全基线。
 * <p>当前主要用于客户端策略（Client Policy），未来可扩展至密码策略等安全配置。</p>
 *
 * @author rmartinc
 */
public interface SecurityProfileProvider extends Provider {

    /** @return 安全配置文件名称 */

    String getName();

    /** @return 安全配置文件包含的默认客户端 Profile 列表 */

    List<ClientProfileRepresentation> getDefaultClientProfiles();

    /** @return 安全配置文件定义的默认客户端 Policy 列表 */

    List<ClientPolicyRepresentation> getDefaultClientPolicies();
}
