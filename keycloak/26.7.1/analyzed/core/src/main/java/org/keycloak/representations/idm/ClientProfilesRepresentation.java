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

import java.util.ArrayList;
import java.util.List;

import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Client Profile 集合的外部 REST 表示，包含 realm 级 Profile 与全局内置 Profile。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientProfilesRepresentation {

    /** realm 级 Client Profile 列表。 */
    private List<ClientProfileRepresentation> profiles = new ArrayList<>();

    /** Keycloak 内置的全局 Client Profile 列表。 */
    @JsonProperty("globalProfiles")
    private List<ClientProfileRepresentation> globalProfiles;

    /** @return realm 级 Profile 列表 */
    public List<ClientProfileRepresentation> getProfiles() {
        return profiles;
    }

    /** @param profiles realm 级 Profile 列表 */
    public void setProfiles(List<ClientProfileRepresentation> profiles) {
        this.profiles = profiles;
    }

    /** @return 全局内置 Profile 列表 */
    public List<ClientProfileRepresentation> getGlobalProfiles() {
        return globalProfiles;
    }

    /** @param globalProfiles 全局内置 Profile 列表 */
    public void setGlobalProfiles(List<ClientProfileRepresentation> globalProfiles) {
        this.globalProfiles = globalProfiles;
    }

    /** 基于 JSON 树结构计算哈希值，忽略字段顺序差异。 */
    @Override
    public int hashCode() {
        return JsonSerialization.mapper.convertValue(this, JsonNode.class).hashCode();
    }

    /** 基于 JSON 树结构比较相等性。 */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClientProfilesRepresentation)) return false;
        JsonNode jsonNode = JsonSerialization.mapper.convertValue(this, JsonNode.class);
        JsonNode jsonNodeThat = JsonSerialization.mapper.convertValue(obj, JsonNode.class);
        return jsonNode.equals(jsonNodeThat);
    }
}
