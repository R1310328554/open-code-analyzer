/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.component;

import org.keycloak.provider.Provider;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 基于 JSON 配置的组件模型：适用于依赖 JSON 而非数据库持久化 {@link ComponentModel} 的提供者。
 * Component model backed by JSON configuration. Useful for providers, which rely on JSON configuration rather than on ComponentModel, which is directly
 * persisted as entity in the DB (store).
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JsonConfigComponentModel extends ComponentModel {

    private final String providerType;
    private final String providerId;
    private final String componentId;
    private final JsonNode configNode;

    /**
     * 从 JSON 节点构建虚拟组件模型。
     * @param providerType 提供者类型
     * @param realmId 领域 ID
     * @param providerId 提供者 ID
     * @param configNode JSON configuration of this provider. For example if node corresponds to JSON like "{\"foo\":\"bar\"}", then
     *                   component configuration is supposed to have one configuration option "foo" with value "bar"
     */
    public JsonConfigComponentModel(Class<? extends Provider> providerType, String realmId, String providerId, JsonNode configNode) {
        checkNotNull(providerType, "providerType must be not null");
        checkNotNull(realmId, "realmId must be not null");
        checkNotNull(providerId, "providerId must be not null");
        checkNotNull(configNode, "configNode must be not null for provider " + providerId);
        this.providerType = providerType.getName();
        this.providerId = providerId;
        this.configNode = configNode;

        // 无真实组件 ID，基于 realmId、providerType、providerId 与配置 hash 合成
        // We don't have realm model ID of the component, so componentId based on the realmId, providerType, providerId and hashCode of configurations.
        this.componentId = realmId + "::" + providerType + "::" + this.providerId + "::" + configNode.hashCode();
    }

    private void checkNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }


    /** @return 提供者类型全限定名 */
    @Override
    public String getProviderType() {
        return providerType;
    }

    /** @return 提供者 ID */
    @Override
    public String getProviderId() {
        return providerId;
    }

    /** @return 合成组件名称（componentId + "-config"） */
    @Override
    public String getName() {
        return componentId + "-config";
    }

    /** @return 合成组件 ID */
    @Override
    public String getId() {
        return componentId;
    }

    /** 从 JSON 节点读取布尔配置。 */
    @Override
    public boolean get(String key, boolean defaultValue) {
        JsonNode sub = configNode.get(key);
        return sub == null ? defaultValue : sub.asBoolean();
    }

    /** 从 JSON 节点读取长整型配置。 */
    @Override
    public long get(String key, long defaultValue) {
        JsonNode sub = configNode.get(key);
        return sub == null ? defaultValue : sub.asLong();
    }

    /** 从 JSON 节点读取整型配置。 */
    @Override
    public int get(String key, int defaultValue) {
        JsonNode sub = configNode.get(key);
        return sub == null ? defaultValue : sub.asInt();
    }

    /** 从 JSON 节点读取字符串配置。 */
    @Override
    public String get(String key, String defaultValue) {
        JsonNode sub = configNode.get(key);
        return sub == null ? defaultValue : sub.asText();
    }

    @Override
    public String get(String key) {
        return get(key, null);
    }

}
