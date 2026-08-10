/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.saml.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * SAML 角色名称映射器。
 * <p>将已分配的领域或客户端角色映射为 SAML 断言中的新名称，供 {@link RoleListMapper} 调用。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RoleNameMapper implements SAMLRoleNameMapper, ProtocolMapper {

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    /** 配置键：源角色（领域或 clientId.role 格式） */
    public static final String ROLE_CONFIG = "role";
    /** 配置键：映射后的新角色名 */
    public static String NEW_ROLE_NAME = "new.role.name";

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ROLE_CONFIG);
        property.setLabel("Role");
        property.setHelpText("Role name you want changed.  Click 'Select Role' button to browse roles, or just type it in the textbox.  To reference a client role the syntax is clientname.clientrole, i.e. myclient.myrole");
        property.setType(ProviderConfigProperty.ROLE_TYPE);
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(NEW_ROLE_NAME);
        property.setLabel("New Role Name");
        property.setHelpText("The new role name.");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
    }

    /** 提供方标识 */
    public static final String PROVIDER_ID = "saml-role-name-mapper";


    /** @return 配置属性列表 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** @return 映射器标识 {@link #PROVIDER_ID} */
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Role Name Mapper";
    }

    /** @return 映射器分类 */
    public String getDisplayCategory() {
        return "Role Mapper";

    }

    /** @return 映射器说明文本 */
    public String getHelpText() {
        return "Map an assigned role to a new name";
    }

    /**
     * 若角色匹配配置则返回新名称。
     * @param model 映射配置
     * @param roleModel 待映射角色
     * @return 新角色名，不匹配时 null
     */
    @Override
    public String mapName(ProtocolMapperModel model, RoleModel roleModel) {
        RoleContainerModel container = roleModel.getContainer();
        ClientModel app = null;
        if (container instanceof ClientModel) {
            app = (ClientModel) container;
        }
        String role = model.getConfig().get(ROLE_CONFIG);
        String newName = model.getConfig().get(NEW_ROLE_NAME);
        int scopeIndex = role.indexOf('.');
        if (scopeIndex > -1 && app != null) {
            final String clientId = app.getClientId();
            if (! role.startsWith(clientId + ".")) return null;
            role = role.substring(clientId.length() + 1);
        } else {
            if (app != null) return null;
        }
        if (roleModel.getName().equals(role)) return newName;
        return null;
   }

    /** 创建角色名称映射器 @param name 名称 @param role 源角色 @param newName 新名称 @return 协议映射器模型 */
    public static ProtocolMapperModel create(String name,
                                             String role,
                                             String newName) {
        String mapperId = PROVIDER_ID;
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(mapperId);
        mapper.setProtocol(SamlProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<String, String>();
        config.put(ROLE_CONFIG, role);
        config.put(NEW_ROLE_NAME, newName);
        mapper.setConfig(config);
        return mapper;

    }

    /** @return SAML 登录协议标识 */
    @Override
    public String getProtocol() {
        return SamlProtocol.LOGIN_PROTOCOL;
    }

    /** 关闭资源（无操作） */
    @Override
    public void close() {
    }

    /** 初始化（无操作） @param config 配置作用域 */
    @Override
    public void init(Config.Scope config) {
    }

    /** 不支持工厂创建 @throws RuntimeException 始终抛出 */
    @Override
    public final ProtocolMapper create(KeycloakSession session) {
        throw new RuntimeException("UNSUPPORTED METHOD");
    }

    /** 工厂初始化后回调（无操作） @param factory 会话工厂 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }
}
