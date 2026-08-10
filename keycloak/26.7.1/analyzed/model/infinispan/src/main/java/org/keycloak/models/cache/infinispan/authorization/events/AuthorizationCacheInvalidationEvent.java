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
package org.keycloak.models.cache.infinispan.authorization.events;

import java.util.Set;

import org.keycloak.models.cache.infinispan.authorization.StoreFactoryCacheManager;

/**
 * 授权缓存集群失效事件的通用契约。
 *
 * <p>各具体事件（策略、资源、作用域等变更）实现此接口，
 * 在集群广播到达时将受影响的缓存键 ID 写入 {@code invalidations} 集合。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface AuthorizationCacheInvalidationEvent {
    /** 根据事件内容向失效集合追加应刷新的授权缓存键 ID。 */
    void addInvalidations(StoreFactoryCacheManager realmCache, Set<String> invalidations);
}
