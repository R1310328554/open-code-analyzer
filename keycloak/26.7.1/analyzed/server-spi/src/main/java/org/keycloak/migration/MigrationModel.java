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

package org.keycloak.migration;


/**
 * 数据库迁移模型：记录 Keycloak 已存储的 schema/资源版本，供启动时迁移逻辑查询。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface MigrationModel {
    /** @return 已持久化的 Keycloak 版本号 */
    String getStoredVersion();
    /** @return 已弃用的资源标签
     * @deprecated 无替代方案 */
    @Deprecated
    String getResourcesTag();
    /** @param version 要持久化的版本号 */
    void setStoredVersion(String version);
}
