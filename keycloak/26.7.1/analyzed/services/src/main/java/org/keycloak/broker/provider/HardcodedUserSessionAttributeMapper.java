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

package org.keycloak.broker.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 硬编码用户会话属性映射器：为联邦用户会话 note 写入固定键值。
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HardcodedUserSessionAttributeMapper extends AbstractIdentityProviderMapper {
    /** 配置键：会话 note 属性名。 */
    public static final String ATTRIBUTE = "attribute";
    /** 配置键：硬编码会话属性值。 */
    public static final String ATTRIBUTE_VALUE = "attribute.value";
    protected static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();
    private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ATTRIBUTE);
        property.setLabel("User Session Attribute");
        property.setHelpText("Name of user session attribute you want to hardcode");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(ATTRIBUTE_VALUE);
        property.setLabel("User Session Attribute Value");
        property.setHelpText("Value you want to hardcode");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
    }

    @Override
    public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
        return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode);
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getDisplayCategory() {
        return "Attribute Importer";
    }

    @Override
    public String getDisplayType() {
        return "Hardcoded User Session Attribute";
    }

    public static final String[] COMPATIBLE_PROVIDERS = {ANY_PROVIDER};


    /** 映射器 provider id。 */
    public static final String PROVIDER_ID = "hardcoded-user-session-attribute-idp-mapper";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    /** 预处理：写入硬编码会话 note。 */
    @Override
    public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        setHardcodedUserSessionAttribute(mapperModel, context);
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        setHardcodedUserSessionAttribute(mapperModel, context);
    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        setHardcodedUserSessionAttribute(mapperModel, context);
    }

    /** @return 导入用户时为会话 note 写入固定值 */
    @Override
    public String getHelpText() {
        return "When user is imported from provider, hardcode a value to a specific user session attribute.";
    }

    /** 将配置的键值对写入 {@link BrokeredIdentityContext#setSessionNote}。 */
    private void setHardcodedUserSessionAttribute(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        String attribute = mapperModel.getConfig().get(ATTRIBUTE);
        String attributeValue = mapperModel.getConfig().get(ATTRIBUTE_VALUE);
        context.setSessionNote(attribute, attributeValue);
    }
}
