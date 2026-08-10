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
 */
package org.keycloak.operator.crds.v2beta1.realmimport;

import java.util.LinkedHashMap;
import java.util.Map;

import org.keycloak.representations.idm.RealmRepresentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.generator.annotation.Required;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.sundr.builder.annotations.Buildable;

/**
 * {@link KeycloakRealmImport} 的期望状态：指定导入目标、领域数据及可选 Job 配置。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class KeycloakRealmImportSpec {

    /** 同命名空间内目标 Keycloak CR 的名称。 */
    @Required
    @JsonPropertyDescription("The name of the Keycloak CR to reference, in the same namespace.")
    private String keycloakCRName;
    /** 要导入 Keycloak 的领域表示（RealmRepresentation）。 */
    @Required
    @JsonPropertyDescription("The RealmRepresentation to import into Keycloak.")
    private RealmRepresentation realm;

    /** 导入 Job 中 Keycloak 容器的计算资源；未设置时继承 Keycloak CR 配置。 */
    @JsonProperty("resources")
    @JsonPropertyDescription("Compute Resources required by Keycloak container. If not specified, the value is inherited from the Keycloak CR.")
    private ResourceRequirements resourceRequirements;

    /** 领域导入 JSON 中环境变量占位符的替换映射。 */
    @JsonPropertyDescription("Optionally set to replace ENV variable placeholders in the realm import.")
    private Map<String, Placeholder> placeholders;

    /** 追加到导入 Job 的额外 Kubernetes 标签。 */
    @JsonProperty("labels")
    @JsonPropertyDescription("Optionally set to add additional labels to the Job created for the import.")
    Map<String, String> labels = new LinkedHashMap<String, String>();

    public String getKeycloakCRName() {
        return keycloakCRName;
    }

    public void setKeycloakCRName(String keycloakCRName) {
        this.keycloakCRName = keycloakCRName;
    }

    public RealmRepresentation getRealm() {
        return realm;
    }

    public void setRealm(RealmRepresentation realm) {
        this.realm = realm;
    }

    public ResourceRequirements getResourceRequirements() {
        return resourceRequirements;
    }

    public void setResourceRequirements(ResourceRequirements resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }

    public Map<String, Placeholder> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(Map<String, Placeholder> placeholders) {
        this.placeholders = placeholders;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}
