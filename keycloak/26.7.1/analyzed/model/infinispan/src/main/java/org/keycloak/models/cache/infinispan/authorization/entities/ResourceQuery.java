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
 * 资源查询缓存键的通用契约。
 *
 * <p>扩展 {@link Revisioned}，描述资源列表类缓存条目的版本号及失效判定逻辑。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface ResourceQuery extends Revisioned {

    /** 返回缓存的资源 ID 集合。 */
    Set<String> getResources();

    /** 根据集群广播的失效 ID 集合判断本查询键是否应失效。 */
    boolean isInvalid(Set<String> invalidations);
}
