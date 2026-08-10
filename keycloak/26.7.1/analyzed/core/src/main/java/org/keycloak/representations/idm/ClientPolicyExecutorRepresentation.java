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

package org.keycloak.representations.idm;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Client Profile 中单条执行器的 REST 表示，包含执行器提供方 ID 与 JSON 配置。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientPolicyExecutorRepresentation {

    /** 执行器 SPI 提供方标识（JSON 字段 {@code executor}）。 */
    @JsonProperty("executor")
    private String executorProviderId;

    /** 执行器提供方的配置参数（JSON 对象）。 */
    @JsonProperty("configuration")
    @Schema(type=SchemaType.OBJECT,
            description = "Configuration settings as a JSON object",
            additionalProperties = Schema.True.class)
    private JsonNode configuration;

    /** @return 执行器提供方 ID */
    public String getExecutorProviderId() {
        return executorProviderId;
    }

    /** @param providerId 执行器提供方 ID */
    public void setExecutorProviderId(String providerId) {
        this.executorProviderId = providerId;
    }

    /** @return 执行器配置 JSON */
    public JsonNode getConfiguration() {
        return configuration;
    }

    /** @param configuration 执行器配置 JSON */
    public void setConfiguration(JsonNode configuration) {
        this.configuration = configuration;
    }

    /** 基于提供方 ID 与配置比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientPolicyExecutorRepresentation that = (ClientPolicyExecutorRepresentation) o;
        return Objects.equals(executorProviderId, that.executorProviderId) && Objects.equals(configuration, that.configuration);
    }

    /** 基于提供方 ID 与配置计算哈希。 */
    @Override
    public int hashCode() {
        return Objects.hash(executorProviderId, configuration);
    }
}
