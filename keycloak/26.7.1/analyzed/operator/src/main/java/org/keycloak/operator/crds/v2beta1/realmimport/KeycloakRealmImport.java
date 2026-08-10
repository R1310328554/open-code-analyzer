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

import org.keycloak.operator.Constants;
import org.keycloak.representations.idm.ComponentExportRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fabric8.crd.generator.annotation.SchemaSwap;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.quarkiverse.operatorsdk.annotations.CSVMetadata;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

/**
 * Keycloak 领域导入自定义资源（CR），用于将 {@link RealmRepresentation} 导入已部署的 Keycloak 实例。
 *
 * <p>命名空间级资源，通过 {@link KeycloakRealmImportSpec} 指定目标 Keycloak CR 与领域配置。
 */
@CSVMetadata(
    description="Represents a Keycloak Realm Import",
    displayName="KeycloakRealmImport"
)
@Group(Constants.CRDS_GROUP)
@Version(Constants.CRDS_VERSION)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder",
        lazyCollectionInitEnabled = false, refs = {
        @BuildableReference(io.fabric8.kubernetes.api.model.ObjectMeta.class),
        @BuildableReference(io.fabric8.kubernetes.client.CustomResource.class),
        @BuildableReference(KeycloakRealmImportSpec.class)
})
// 递归嵌套结构在 OpenAPI 中替换为占位类型，避免 CRD 生成器无限展开
@SchemaSwap(originalType = GroupRepresentation.class, fieldName = "subGroups", depth = 10)
@SchemaSwap(originalType = ComponentExportRepresentation.class, fieldName = "subComponents", depth = 10)
@SchemaSwap(originalType = ScopeRepresentation.class, fieldName = "policies")
@SchemaSwap(originalType = ScopeRepresentation.class, fieldName = "resources")
public class KeycloakRealmImport extends CustomResource<KeycloakRealmImportSpec, KeycloakRealmImportStatus> implements Namespaced {

    /** 从 spec 中读取领域名称，不参与 JSON 序列化。 */
    @JsonIgnore
    public String getRealmName() {
        return this.getSpec().getRealm().getRealm();
    }

}
