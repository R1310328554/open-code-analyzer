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
package org.keycloak.models.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.StorageProviderRealmModel;
import org.keycloak.provider.ProviderEvent;

/**
 * 已缓存的 Realm 模型应实现此接口，用于在缓存层与底层存储之间切换访问。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface CachedRealmModel extends StorageProviderRealmModel {

    /** Realm 缓存相关事件，携带缓存模型与当前 {@link KeycloakSession}。 */
    interface RealmCachedEvent extends ProviderEvent {
        CachedRealmModel getRealm();
        KeycloakSession getKeycloakSession();
    }

    /**
     * 使本模型的缓存失效，并返回代表实际数据 Provider 的委托对象以便更新。
     *
     * @return 可写操作的底层 {@link RealmModel} 委托
     */
    RealmModel getDelegateForUpdate();

    /** 使本模型的缓存失效。 */
    void invalidate();

    /**
     * 返回模型从数据库加载时的时间戳。
     *
     * @return 缓存加载时刻（毫秒）
     */
    long getCacheTimestamp();

    /**
     * 返回与本模型一同缓存的自定义附加数据映射；调用方可向该映射写入条目。
     *
     * @return 可写的附加缓存数据映射
     */
    ConcurrentHashMap getCachedWith();
}
