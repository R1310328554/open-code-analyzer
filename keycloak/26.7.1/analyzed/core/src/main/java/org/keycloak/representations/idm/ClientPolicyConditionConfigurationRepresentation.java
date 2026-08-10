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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 为 Client Policy 条件配置提供类型安全的 REST 表示，支持动态 JSON 属性与否定逻辑标志。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientPolicyConditionConfigurationRepresentation {

    /** 条件提供方特定的扩展配置项（键值对）。 */
    private Map<String, Object> configAsMap = new HashMap<>();

    /** 是否对条件结果取反（否定逻辑）。 */
    @JsonProperty("is-negative-logic")
    private Boolean negativeLogic;

    /** @return 是否启用否定逻辑 */
    public Boolean isNegativeLogic() {
        return negativeLogic;
    }

    /** @param negativeLogic 是否启用否定逻辑 */
    public void setNegativeLogic(Boolean negativeLogic) {
        this.negativeLogic = negativeLogic;
    }

    /** @return 扩展配置映射 */
    @JsonAnyGetter
    public Map<String, Object> getConfigAsMap() {
        return configAsMap;
    }

    /** @param name 配置键
     *  @param value 配置值
     */
    @JsonAnySetter
    public void setConfigAsMap(String name, Object value) {
        this.configAsMap.put(name, value);
    }
}
