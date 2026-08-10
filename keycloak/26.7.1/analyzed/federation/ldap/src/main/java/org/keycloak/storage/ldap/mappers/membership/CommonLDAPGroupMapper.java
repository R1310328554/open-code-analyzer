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

import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.mappers.LDAPStorageMapper;

/**
 * 通用 LDAP 组映射器接口：将 LDAP 组映射到 Keycloak 模型对象（角色或组）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface CommonLDAPGroupMapper extends LDAPStorageMapper {

    /** 创建用于检索 LDAP 组/角色的查询对象。 */
    LDAPQuery createLDAPGroupQuery();

    /** 返回本映射器的配置封装。 */
    CommonLDAPGroupMapperConfig getConfig();
}
