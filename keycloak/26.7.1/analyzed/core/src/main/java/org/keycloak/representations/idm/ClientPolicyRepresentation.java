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

package org.keycloak.representations.idm;

import java.util.List;
import java.util.Objects;

/**
 * Client Policy 的外部 REST 表示，定义策略名称、启用状态、条件与关联 Profile。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientPolicyRepresentation {

    /** 策略名称。 */
    protected String name;
    /** 策略描述。 */
    protected String description;
    /** 是否启用该策略。 */
    protected Boolean enabled;
    /** 策略运行模式（如 PERMISSIVE / ENFORCING）。 */
    protected String mode;
    /** 触发策略评估的条件列表。 */
    protected List<ClientPolicyConditionRepresentation> conditions;
    /** 关联的 Client Profile 名称列表。 */
    protected List<String> profiles;

    /** @return 策略名称 */
    public String getName() {
        return name;
    }

    /** @param name 策略名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 策略描述 */
    public String getDescription() {
        return description;
    }

    /** @param description 策略描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 是否启用 */
    public Boolean isEnabled() {
        return enabled;
    }

    /** @param enabled 是否启用 */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /** @return 运行模式 */
    public String getMode() {
        return mode;
    }

    /** @param mode 运行模式 */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /** @return 条件列表 */
    public List<ClientPolicyConditionRepresentation> getConditions() {
        return conditions;
    }

    /** @param conditions 条件列表 */
    public void setConditions(List<ClientPolicyConditionRepresentation> conditions) {
        this.conditions = conditions;
    }

    /** @return 关联 Profile 名称列表 */
    public List<String> getProfiles() {
        return profiles;
    }

    /** @param profiles 关联 Profile 名称列表 */
    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    /** 基于名称、描述、启用状态、条件与 Profile 比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientPolicyRepresentation that = (ClientPolicyRepresentation) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(enabled, that.enabled) && Objects.equals(conditions, that.conditions) && Objects.equals(profiles, that.profiles);
    }

    /** 基于核心字段计算哈希。 */
    @Override
    public int hashCode() {
        return Objects.hash(name, description, enabled, conditions, profiles);
    }
}
