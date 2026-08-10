/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models.cache.infinispan.authorization.entities;

import java.util.Set;

import org.keycloak.models.cache.infinispan.entities.Revisioned;

/**
 * 权限票据查询结果的通用接口。
 * <p>
 * 继承 {@link InResourceServer} 与 {@link Revisioned}，定义权限 ID 集合访问与失效判定。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface PermissionTicketQuery extends InResourceServer, Revisioned {

    /** 返回查询命中的权限票据 ID 集合。 */
    Set<String> getPermissions();
    /** 判断当前查询缓存是否因失效集合而过期。 */
    boolean isInvalid(Set<String> invalidations);
}
