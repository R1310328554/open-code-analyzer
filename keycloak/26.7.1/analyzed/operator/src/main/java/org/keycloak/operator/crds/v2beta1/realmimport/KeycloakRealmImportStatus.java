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

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static org.keycloak.operator.crds.v2beta1.realmimport.KeycloakRealmImportStatusCondition.DONE;

/**
 * {@link KeycloakRealmImport} 的观测状态，以条件列表反映导入进度与错误。
 */
public class KeycloakRealmImportStatus {
    /** 导入相关的状态条件集合（Started、Done、HasErrors 等）。 */
    private List<KeycloakRealmImportStatusCondition> conditions;

    public List<KeycloakRealmImportStatusCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<KeycloakRealmImportStatusCondition> conditions) {
        this.conditions = conditions;
    }

    /**
     * 判断导入是否已完成：存在类型为 {@link KeycloakRealmImportStatusCondition#DONE}
     * 且 status 为 True 的条件。
     */
    @JsonIgnore
    public boolean isDone() {
        return conditions
                .stream()
                .anyMatch(c -> Boolean.TRUE.equals(c.getStatus()) && c.getType().equals(DONE));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeycloakRealmImportStatus status = (KeycloakRealmImportStatus) o;
        return Objects.equals(getConditions(), status.getConditions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConditions());
    }
}
