/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.adapters.saml;

import java.util.Properties;
import java.util.Set;

import org.keycloak.adapters.saml.config.parsers.ResourceLoader;

/**
 * 将 SAML 角色映射为 SP 应用环境中角色的 SPI 接口。
 *
 * <p>外部 IdP 返回的角色名未必与应用内定义一致，适配器在从 SAML 断言提取角色后、
 * 设置容器安全上下文前，通过本 SPI 完成角色转换。</p>
 * <p/>
 * SPI 不限制映射方式：实现既可做角色到角色的转换，也可增删角色。
 * <p/>
 * 安装自定义提供者：在 WAR 或 JAR 中添加
 * {@code META-INF/services/org.keycloak.adapters.saml.RoleMappingsProvider}，
 * 内容为实现类的 FQN，并在 {@code keycloak-saml.xml} 中通过 id 引用。
 * <p/>
 * 示例（LDAP 提供者）：
 *
 * <pre>
 *     ...
 *     <RoleIdentifiers>
 *         ...
 *     </RoleIdentifiers>
 *     <RoleMappingsProvider id="ldap-based-role-mapper">
 *         <Property name="connection.url" value="some.url"/>
 *         <Property name="username" value="some.user"/>
 *         ...
 *     </RoleMappingsProvider>
 * </pre>
 *
 * <p>注意：SPI 尚未定稿，方法签名在未来版本可能变更。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface RoleMappingsProvider {

    /**
     * 返回提供者在 {@code keycloak-saml.xml} 中引用的标识符。
     *
     * @return 提供者 id
     */
    String getId();

    /**
     * 初始化提供者。适配器在部署时解析 {@code keycloak-saml.xml} 后、
     * 匹配到对应 id 的实现类时调用。
     *
     * @param deployment 已构建的 {@link SamlDeployment} 引用
     * @param loader 用于从 WAR 加载额外资源的 {@link ResourceLoader}
     * @param config 来自 {@code keycloak-saml.xml} 的提供者配置
     */
    void init(final SamlDeployment deployment, final ResourceLoader loader, final Properties config);

    /**
     * 生成应分配给指定主体的最终角色集。
     *
     * <p>将断言中的主体名与角色集交给实现，由实现应用特定映射逻辑。
     * 简单实现可用 properties 文件映射；复杂实现可连接数据库或 LDAP
     * 获取额外角色并合并到断言角色中。</p>
     *
     * @param principalName 从 SAML 断言提取的主体名
     * @param roles 从 SAML 断言提取的角色集
     * @return 最终应分配给主体的角色集
     */
    Set<String> map(final String principalName, final Set<String> roles);
}
