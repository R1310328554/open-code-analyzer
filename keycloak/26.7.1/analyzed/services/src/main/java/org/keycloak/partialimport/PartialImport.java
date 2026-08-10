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

package org.keycloak.partialimport;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.PartialImportRepresentation;

/**
 * 部分导入处理器主接口：定义 prepare、removeOverwrites、doImport 三阶段生命周期。
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public interface PartialImport<T> {

    /**
     * 预处理阶段：识别需跳过或覆盖的资源，并进行初步错误检查。
     * @param rep 部分导入请求中的全部内容
     * @param realm 导入目标 Realm
     * @param session Keycloak 会话
     */
    void prepare(PartialImportRepresentation rep,
                 RealmModel realm,
                 KeycloakSession session);

    /**
     * 删除将被覆盖的资源。各资源类型统一先删后建，避免级联删除导致的顺序错误。
     * @param realm 导入目标 Realm
     * @param session Keycloak 会话
     */
    void removeOverwrites(RealmModel realm, KeycloakSession session);

    /**
     * 创建或重建所有导入资源。
     * @param rep 部分导入请求
     * @param realm 导入目标 Realm
     * @param session Keycloak 会话
     * @return 部分导入最终结果
     */
    PartialImportResults doImport(PartialImportRepresentation rep,
                                  RealmModel realm,
                                  KeycloakSession session);
}
