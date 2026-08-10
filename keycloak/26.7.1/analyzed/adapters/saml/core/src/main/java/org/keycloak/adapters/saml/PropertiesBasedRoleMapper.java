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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.keycloak.adapters.saml.config.parsers.ResourceLoader;

import org.jboss.logging.Logger;

/**
 * 基于 {@code properties} 文件的角色映射 {@link RoleMappingsProvider} 实现。
 *
 * <p>在 {@code keycloak-saml.xml} 中通过 id {@code properties-based-role-mapper} 引用。
 * 支持 SAML 角色到应用角色的映射，以及按主体名追加额外角色。</p>
 * <p/>
 * 加载顺序：优先 {@code properties.file.location}（文件系统），其次
 * {@code properties.resource.location}（WAR 资源），最后默认
 * {@code /WEB-INF/role-mappings.properties}。
 * <p/>
 * properties 文件中键可为角色名或主体名，值为逗号分隔的目标角色列表。
 * 映射为空字符串的角色将被丢弃；无映射的角色原样保留。
 * <p/>
 * 示例 properties 文件：
 *
 * <pre>
 *     # 角色到角色的映射
 *     samlRoleA=jeeRoleX,jeeRoleY
 *     samlRoleB=
 *
 *     # 主体到角色的映射
 *     kc-user=jeeRoleZ
 * </pre>
 *
 * 若 {@link #map(String, Set)} 以 {@code kc-user} 为主体、角色集
 * {@code samlRoleA,samlRoleB,samlRoleC} 调用，结果集为
 * {@code jeeRoleX,jeeRoleY,samlRoleC,jeeRoleZ}。
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class PropertiesBasedRoleMapper implements RoleMappingsProvider {

    private static final Logger logger = Logger.getLogger(PropertiesBasedRoleMapper.class);

    /** 提供者在 keycloak-saml.xml 中的 id */
    public static final String PROVIDER_ID = "properties-based-role-mapper";

    /** 文件系统 properties 路径配置键 */
    private static final String PROPERTIES_FILE_LOCATION = "properties.file.location";

    /** WAR 资源 properties 路径配置键 */
    private static final String PROPERTIES_RESOURCE_LOCATION = "properties.resource.location";

    /** 默认资源路径 */
    private static final String DEFAULT_RESOURCE_LOCATION = "/WEB-INF/role-mappings.properties";

    /** 已加载的角色映射 properties */
    private Properties roleMappings;

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /**
     * 从文件系统或 WAR 资源加载 properties 映射文件。
     *
     * @param deployment SAML 部署配置
     * @param loader WAR 资源加载器
     * @param config 来自 keycloak-saml.xml 的提供者配置
     */
    @Override
    public void init(final SamlDeployment deployment, final ResourceLoader loader, final Properties config) {

        this.roleMappings = new Properties();
        // 优先从文件系统加载 properties
        String path = config.getProperty(PROPERTIES_FILE_LOCATION);
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                try (FileInputStream is = new FileInputStream(file)){
                    this.roleMappings.load(is);
                    logger.debugf("Successfully loaded role mappings from %s", path);
                } catch (Exception e) {
                    logger.debugv(e, "Unable to load role mappings from %s", path);
                }
            } else {
                throw new RuntimeException("Unable to load role mappings from " + path + ": file does not exist in filesystem");
            }
        } else {
            // 从 WAR 资源加载 properties
            path = config.getProperty(PROPERTIES_RESOURCE_LOCATION, DEFAULT_RESOURCE_LOCATION);
            InputStream is = loader.getResourceAsStream(path);
            if (is != null) {
                try {
                    this.roleMappings.load(is);
                    logger.debugf("Resource loader successfully loaded role mappings from %s", path);
                } catch (Exception e) {
                    logger.debugv(e, "Resource loader unable to load role mappings from %s", path);
                }
            } else {
                throw new RuntimeException("Unable to load role mappings from " + path + ": file does not exist in the resource");
            }
        }
    }

    /**
     * 将 SAML 断言中的角色映射为应用最终角色集。
     *
     * @param principalName 断言中的主体名
     * @param roles 从断言提取的原始角色集
     * @return 映射后的最终角色集
     */
    @Override
    public Set<String> map(final String principalName, final Set<String> roles) {
        if (this.roleMappings == null || this.roleMappings.isEmpty())
            return roles;

        Set<String> resolvedRoles = new HashSet<>();
        // 先处理角色 -> 角色(s) 映射
        for (String role : roles) {
            if (this.roleMappings.containsKey(role)) {
                // 映射为空字符串的角色从结果集中丢弃
                this.extractRolesIntoSet(role, resolvedRoles);
            } else {
                // 无映射的角色原样保留
                resolvedRoles.add(role);
            }
        }

        // 再检查主体 -> 角色(s) 映射，追加额外角色
        if (this.roleMappings.containsKey(principalName)) {
            this.extractRolesIntoSet(principalName, resolvedRoles);
        }
        return resolvedRoles;
    }

    /**
     * 从 properties 条目中提取逗号分隔的角色列表，去空白后写入目标集合。
     *
     * @param entry properties 文件中的键
     * @param roles 目标角色集合
     */
    private void extractRolesIntoSet(final String entry, final Set<String> roles) {
        String value = this.roleMappings.getProperty(entry);
        if (!value.isEmpty()) {
            String[] mappedRoles = value.split(",");
            for (String mappedRole : mappedRoles) {
                String trimmedRole = mappedRole.trim();
                if (!trimmedRole.isEmpty()) {
                    roles.add(trimmedRole);
                }
            }
        }
    }
}
