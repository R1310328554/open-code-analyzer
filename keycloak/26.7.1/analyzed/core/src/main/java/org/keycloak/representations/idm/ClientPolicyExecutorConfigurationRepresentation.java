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

/**
 * 为 Client Policy 执行器配置提供类型安全的 REST 表示，支持动态 JSON 属性。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientPolicyExecutorConfigurationRepresentation {

    /** 执行器提供方特定的扩展配置项。 */
    private Map<String, Object> configAsMap = new HashMap<>();

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

    /** 校验配置是否有效（默认始终通过）。 */
    public boolean validateConfig(){
        return  true;
    }
}
