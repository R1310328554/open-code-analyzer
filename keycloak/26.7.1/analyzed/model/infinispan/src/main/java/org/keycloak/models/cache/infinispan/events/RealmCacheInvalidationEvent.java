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

package org.keycloak.models.cache.infinispan.events;

import java.util.Set;

import org.keycloak.models.cache.infinispan.RealmCacheManager;

/**
 * 领域缓存失效事件接口。
 * <p>
 * 由领域、客户端、组、角色等缓存变更事件实现，
 * 通过 {@link RealmCacheManager} 将需失效的缓存键写入集合。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface RealmCacheInvalidationEvent {

    /** 根据事件内容向失效集合追加需刷新的领域缓存键。 */
    void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations);

}
