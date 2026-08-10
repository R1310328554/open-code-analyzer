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
package org.keycloak.testsuite.federation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.keycloak.Config;
import org.keycloak.common.util.EnvUtil;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.user.ImportSynchronization;
import org.keycloak.storage.user.SynchronizationResult;

/**
 * 属性文件用户存储工厂，从配置的 Properties 文件加载用户并支持导入同步接口。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserPropertyFileStorageFactory implements UserStorageProviderFactory<UserPropertyFileStorage>, ImportSynchronization {

    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "user-password-props-arq";
    /** 配置项：属性文件路径。 */
    public static final String PROPERTY_FILE = "propertyFile";

    /** 校验失败：未配置属性文件。 */
    public static final String VALIDATION_PROP_FILE_NOT_CONFIGURED = "user property file is not configured";
    /** 校验失败：属性文件不存在。 */
    public static final String VALIDATION_PROP_FILE_DOESNT_EXIST = "user property file does not exist";

    /** 静态配置属性列表。 */
    protected static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    // 初始化属性文件路径与联邦存储开关配置
    static {
        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                .property().name(PROPERTY_FILE)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Property File")
                .helpText("File that contains name value pairs")
                .defaultValue(null)
                .add()
                .property().name("federatedStorage")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .label("User Federated Storage")
                .helpText("User Federated Storage")
                .defaultValue(null)
                .add()
                .build();
    }

    /** {@inheritDoc} 校验属性文件路径存在且可访问。 */
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        String fp = config.getConfig().getFirst(PROPERTY_FILE);
        if (fp == null) {
            throw new ComponentValidationException(VALIDATION_PROP_FILE_NOT_CONFIGURED);
        }
        fp = EnvUtil.replace(fp);
        File file = new File(fp);
        if (!file.exists()) {
            throw new ComponentValidationException(VALIDATION_PROP_FILE_DOESNT_EXIST);
        }
    }

    @Override
    public UserPropertyFileStorage create(KeycloakSession session, ComponentModel model) {
        String path = model.getConfig().getFirst(PROPERTY_FILE);
        path = EnvUtil.replace(path);

        Properties props = new Properties();
        try (InputStream is = new FileInputStream(path)) {
            props.load(is);
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new UserPropertyFileStorage(session, model, props);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    /** {@inheritDoc} 同步始终忽略（测试桩）。 */
    @Override
    public SynchronizationResult sync(KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        return SynchronizationResult.ignored();
    }

    @Override
    public SynchronizationResult syncSince(Date lastSync, KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        return SynchronizationResult.ignored();
    }

}
