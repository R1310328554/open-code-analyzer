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
package org.keycloak.admin.ui.rest.model;

import org.keycloak.representations.idm.RequiredActionProviderRepresentation;

/**
 * 扩展 {@link RequiredActionProviderRepresentation}，标明该必需操作是否可在 UI 中配置。
 */
public class ConfigurableRequiredActionProviderRepresentation extends RequiredActionProviderRepresentation {

    /** 若为 true，管理控制台允许编辑此必需操作的配置。 */
    private boolean configurable;

    public boolean isConfigurable() {
        return configurable;
    }

    public void setConfigurable(boolean configurable) {
        this.configurable = configurable;
    }
}
