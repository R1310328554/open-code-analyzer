/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.keycloak.operator.crds.v2beta1.CRDUtils;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.KeycloakSpec;
import org.keycloak.operator.update.UpdateStrategy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.generator.annotation.Default;
import io.fabric8.generator.annotation.ValidationRule;
import io.sundr.builder.annotations.Buildable;

/**
 * Keycloak 滚动/重建更新策略配置，包括更新 Job 调度与版本修订号。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
@ValidationRule(
        value = "self.strategy != 'Explicit' || has(self.revision)",
        message = "The 'revision' field is required when 'Explicit' strategy is used"
)
public class UpdateSpec {

    // 默认更新策略常量，需与 DEFAULT_JSON 保持同步
    private static final UpdateStrategy DEFAULT = UpdateStrategy.RECREATE_ON_IMAGE_CHANGE;
    private static final String DEFAULT_JSON = "RecreateOnImageChange";

    /** 更新 Job 的 Pod 调度策略。 */
    @JsonProperty("scheduling")
    @JsonPropertyDescription("In this section you can configure the update job's scheduling")
    private SchedulingSpec schedulingSpec;

    /** 更新策略类型，默认为镜像变更时重建。 */
    @JsonPropertyDescription("Sets the update strategy to use.")
    @Default(DEFAULT_JSON)
    private UpdateStrategy strategy;

    /** 使用 Explicit 策略时的修订号，用于判断是否可执行滚动更新。 */
    @JsonPropertyDescription("When use the Explicit strategy, the revision signals if a rolling update can be used or not.")
    private String revision;

    /** 追加到更新 Job 的额外标签。 */
    @JsonProperty("labels")
    @JsonPropertyDescription("Optionally set to add additional labels to the Job created for the update.")
    Map<String, String> labels = new LinkedHashMap<String, String>();

    public UpdateStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(UpdateStrategy strategy) {
        this.strategy = strategy;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public SchedulingSpec getSchedulingSpec() {
        return schedulingSpec;
    }

    public void setSchedulingSpec(SchedulingSpec schedulingSpec) {
        this.schedulingSpec = schedulingSpec;
    }

    /** 从 Keycloak CR 解析更新策略，未配置时使用 {@link UpdateStrategy#RECREATE_ON_IMAGE_CHANGE}。 */
    public static UpdateStrategy getUpdateStrategy(Keycloak keycloak) {
        return CRDUtils.keycloakSpecOf(keycloak)
                .map(KeycloakSpec::getUpdateSpec)
                .map(UpdateSpec::getStrategy)
                .orElse(DEFAULT);
    }

    /** 从 Keycloak CR 解析 Explicit 策略所需的修订号。 */
    public static Optional<String> getRevision(Keycloak keycloak) {
        return CRDUtils.keycloakSpecOf(keycloak)
                .map(KeycloakSpec::getUpdateSpec)
                .map(UpdateSpec::getRevision);
    }
    
    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}
