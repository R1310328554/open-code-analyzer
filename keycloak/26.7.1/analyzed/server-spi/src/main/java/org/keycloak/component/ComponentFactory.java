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
package org.keycloak.component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderFactory;

/**
 * 组件工厂接口：基于 {@link ComponentModel} 创建组件实例并管理生命周期回调。
 * <p>扩展 {@link ProviderFactory} 与 {@link ConfiguredProvider}，支持配置校验与元数据声明。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ComponentFactory<CreatedType, ProviderType extends Provider> extends ProviderFactory<ProviderType>, ConfiguredProvider {
    /** @param model 组件配置模型
     * @return 创建的组件实例 */
    CreatedType create(KeycloakSession session, ComponentModel model);

    @Override
    default ProviderType create(KeycloakSession session) {
        return null;
    }

    /**
     * 组件创建或更新前调用，用于校验配置。
     * Called before a component is created or updated.  Allows you to validate the configuration
     *
     * @param session
     * @param realm
     * @param model
     * @throws ComponentValidationException
     */
    default
    void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException
    {

    }

    /**
     * 组件创建完成后调用。
     * Called after a component is created
     *
     * @param session
     * @param realm
     * @param model
     */
    default
    void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {

    }


    /**
     * 组件更新完成后调用。
     * Called after the component is updated.
     *
     * @param session
     * @param realm
     * @param oldModel old saved model
     * @param newModel new configuration
     */
    default
    void onUpdate(KeycloakSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {

    }

    /**
     * 组件删除前调用。
     * Called before the component is removed.
     *
     * @param session
     * @param realm
     * @param model model of the component, which is going to be removed
     */
    default
    void preRemove(KeycloakSession session, RealmModel realm, ComponentModel model) {

    }

    /**
     * 该组件类型所有实现共用的配置属性定义。
     * These are config properties that are common across all implementation of this component type
     *
     * @return
     */
    default
    List<ProviderConfigProperty> getCommonProviderConfigProperties() {
        return Collections.emptyList();
    }

    /**
     * 组件类型级元数据（非单个实例配置）。
     * This is metadata about this component type.  Its really configuration information about the component type and not
     * an individual instance
     *
     * @return
     */
    default
    Map<String, Object> getTypeMetadata() {
        return Collections.emptyMap();

    }

    /**
     * 标识该组件工厂是否为内部管理，不应通过通用组件 REST API 暴露。
     * Indicates whether this component factory is managed internally and should not be exposed
     * through the generic component REST API.
     *
     * @return {@code true} if the component is internal, {@code false} otherwise.
     */
    default boolean isInternal() {
        return false;
    }

}
