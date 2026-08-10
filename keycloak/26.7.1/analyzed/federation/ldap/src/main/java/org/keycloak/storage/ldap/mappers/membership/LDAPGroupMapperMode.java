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

package org.keycloak.storage.ldap.mappers.membership;

/**
 * LDAP 组映射器同步模式：控制组成员关系在 LDAP 与 Keycloak 之间的读写策略。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum LDAPGroupMapperMode {

    /**
     * 全部组成员关系从 LDAP 读取并写回 LDAP。
     */
    LDAP_ONLY,

    /**
     * 只读 LDAP：用户导入时从 LDAP 拉取组成员关系并写入 Keycloak 本地库，之后始终从 Keycloak 读取。
     * 性能较好，但 LDAP 中直接变更的成员关系不会反映到 Keycloak。
     */
    IMPORT,

    /**
     * 只读 LDAP：合并 LDAP 与数据库中的组成员关系；新建成员关系仅写入数据库。
     * 删除映射到 LDAP 的成员关系会抛出错误。
     */
    READ_ONLY

}
