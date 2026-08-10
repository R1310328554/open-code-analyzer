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

package org.keycloak.services.clientpolicy;

import java.io.Serializable;
import java.util.List;

import org.keycloak.services.clientpolicy.condition.ClientPolicyConditionProvider;

/**
 * 客户端策略运行时模型：名称、启用状态、条件模式、条件 Provider 列表与关联 Profile 名。
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
class ClientPolicy implements Serializable {

    protected String name;
    protected String description;
    protected boolean enable;
    protected ClientPolicyMode mode;
    protected List<ClientPolicyConditionProvider> conditions;
    protected List<String> profiles;

    /** @return 策略名称 */
    public String getName() {
        return name;
    }

    /** @param name 策略名称 */
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 是否启用 */
    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /** @return 条件求值模式 {@link ClientPolicyMode} */
    public ClientPolicyMode getMode() {
        return mode;
    }

    public void setMode(ClientPolicyMode mode) {
        this.mode = mode;
    }

    /** @return 已解析的条件 Provider 列表 */
    public List<ClientPolicyConditionProvider> getConditions() {
        return conditions;
    }

    public void setConditions(List<ClientPolicyConditionProvider> conditions) {
        this.conditions = conditions;
    }

    /** @return 关联的 Client Profile 名称列表 */
    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }
}
