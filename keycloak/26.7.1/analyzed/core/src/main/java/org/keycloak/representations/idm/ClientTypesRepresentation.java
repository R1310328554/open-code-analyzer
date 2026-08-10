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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 客户端类型集合的 REST 表示，区分 realm 级与全局内置类型。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientTypesRepresentation {

    /** realm 级客户端类型列表。 */
    @JsonProperty("client-types")
    private List<ClientTypeRepresentation> realmClientTypes;

    /** Keycloak 全局内置客户端类型列表。 */
    @JsonProperty("global-client-types")
    private List<ClientTypeRepresentation> globalClientTypes;

    /** 无参构造器。 */
    public ClientTypesRepresentation() {
    }

    /** @param realmClientTypes realm 级类型列表
     *  @param globalClientTypes 全局类型列表
     */
    public ClientTypesRepresentation(List<ClientTypeRepresentation> realmClientTypes, List<ClientTypeRepresentation> globalClientTypes) {
        this.realmClientTypes = realmClientTypes;
        this.globalClientTypes = globalClientTypes;
    }

    /** @return realm 级客户端类型 */
    public List<ClientTypeRepresentation> getRealmClientTypes() {
        return realmClientTypes;
    }

    /** @param realmClientTypes realm 级客户端类型 */
    public void setRealmClientTypes(List<ClientTypeRepresentation> realmClientTypes) {
        this.realmClientTypes = realmClientTypes;
    }

    /** @return 全局内置客户端类型 */
    public List<ClientTypeRepresentation> getGlobalClientTypes() {
        return globalClientTypes;
    }

    /** @param globalClientTypes 全局内置客户端类型 */
    public void setGlobalClientTypes(List<ClientTypeRepresentation> globalClientTypes) {
        this.globalClientTypes = globalClientTypes;
    }
}