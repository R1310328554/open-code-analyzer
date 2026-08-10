/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

/**
 * 用户联合映射器同步能力的 REST 表示，描述联合源与 Keycloak 之间双向同步是否受支持及相关提示信息。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserFederationMapperSyncConfigRepresentation {

    /** 是否支持从联合源同步到 Keycloak。 */
    private Boolean fedToKeycloakSyncSupported;
    /** 联合源到 Keycloak 同步的说明信息；仅在 fedToKeycloakSyncSupported 为 true 时适用。 */
    private String fedToKeycloakSyncMessage; // applicable just if fedToKeycloakSyncSupported is true

    /** 是否支持从 Keycloak 同步到联合源。 */
    private Boolean keycloakToFedSyncSupported;
    /** Keycloak 到联合源同步的说明信息；仅在 keycloakToFedSyncSupported 为 true 时适用。 */
    private String keycloakToFedSyncMessage; // applicable just if keycloakToFedSyncSupported is true

    /** 默认构造函数。 */
    public UserFederationMapperSyncConfigRepresentation() {
    }

    /**
     * 全参构造函数。
     *
     * @param fedToKeycloakSyncSupported 是否支持联合源到 Keycloak 同步
     * @param fedToKeycloakSyncMessage 联合源到 Keycloak 同步说明
     * @param keycloakToFedSyncSupported 是否支持 Keycloak 到联合源同步
     * @param keycloakToFedSyncMessage Keycloak 到联合源同步说明
     */
    public UserFederationMapperSyncConfigRepresentation(boolean fedToKeycloakSyncSupported, String fedToKeycloakSyncMessage,
                                                        boolean keycloakToFedSyncSupported, String keycloakToFedSyncMessage) {
        this.fedToKeycloakSyncSupported = fedToKeycloakSyncSupported;
        this.fedToKeycloakSyncMessage = fedToKeycloakSyncMessage;
        this.keycloakToFedSyncSupported = keycloakToFedSyncSupported;
        this.keycloakToFedSyncMessage = keycloakToFedSyncMessage;
    }

    /** @return 是否支持联合源到 Keycloak 同步 */
    public Boolean isFedToKeycloakSyncSupported() {
        return fedToKeycloakSyncSupported;
    }

    /** @param fedToKeycloakSyncSupported 是否支持联合源到 Keycloak 同步 */
    public void setFedToKeycloakSyncSupported(Boolean fedToKeycloakSyncSupported) {
        this.fedToKeycloakSyncSupported = fedToKeycloakSyncSupported;
    }

    /** @return 联合源到 Keycloak 同步说明 */
    public String getFedToKeycloakSyncMessage() {
        return fedToKeycloakSyncMessage;
    }

    /** @param fedToKeycloakSyncMessage 联合源到 Keycloak 同步说明 */
    public void setFedToKeycloakSyncMessage(String fedToKeycloakSyncMessage) {
        this.fedToKeycloakSyncMessage = fedToKeycloakSyncMessage;
    }

    /** @return 是否支持 Keycloak 到联合源同步 */
    public Boolean isKeycloakToFedSyncSupported() {
        return keycloakToFedSyncSupported;
    }

    /** @param keycloakToFedSyncSupported 是否支持 Keycloak 到联合源同步 */
    public void setKeycloakToFedSyncSupported(Boolean keycloakToFedSyncSupported) {
        this.keycloakToFedSyncSupported = keycloakToFedSyncSupported;
    }

    /** @return Keycloak 到联合源同步说明 */
    public String getKeycloakToFedSyncMessage() {
        return keycloakToFedSyncMessage;
    }

    /** @param keycloakToFedSyncMessage Keycloak 到联合源同步说明 */
    public void setKeycloakToFedSyncMessage(String keycloakToFedSyncMessage) {
        this.keycloakToFedSyncMessage = keycloakToFedSyncMessage;
    }
}
