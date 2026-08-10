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

package org.keycloak.models.cache.infinispan.entities;

import java.util.Set;

/**
 * 客户端作用域查询缓存条目的标记接口。
 * <p>
 * 继承 {@link InClient}，提供查询命中的作用域 ID 集合访问能力。
 */
public interface ClientScopeQuery extends InClient {
    /** 返回查询命中的客户端作用域 ID 集合。 */
    Set<String> getClientScopes();
}
