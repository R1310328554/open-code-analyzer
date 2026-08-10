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
package org.keycloak.operator.crds.v2beta1.deployment.spec;

import org.keycloak.operator.Constants;
import org.keycloak.operator.crds.v2beta1.CRDUtils;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.KeycloakSpec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.sundr.builder.annotations.Buildable;

/**
 * Keycloak HTTP 管理接口（指标、健康检查等）的端口配置。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class HttpManagementSpec {

    /** 管理接口监听端口，默认为 {@link Constants#KEYCLOAK_MANAGEMENT_PORT}。 */
    @JsonPropertyDescription("Port of the management interface.")
    private Integer port = Constants.KEYCLOAK_MANAGEMENT_PORT;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /** 从 Keycloak CR 解析管理端口，未配置时使用默认值。 */
    public static int managementPort(Keycloak keycloak) {
        return CRDUtils.keycloakSpecOf(keycloak)
                .map(KeycloakSpec::getHttpManagementSpec)
                .map(HttpManagementSpec::getPort)
                .orElse(Constants.KEYCLOAK_MANAGEMENT_PORT);
    }
}
