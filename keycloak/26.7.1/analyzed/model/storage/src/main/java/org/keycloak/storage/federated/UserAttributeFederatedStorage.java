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
package org.keycloak.storage.federated;

import java.util.List;
import java.util.stream.Stream;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.RealmModel;

/**
 * 联邦用户属性存储接口：读写外部用户存储无法直接承载的自定义属性。
 * <p>
 * 由 {@link UserFederatedStorageProvider} 组合实现，供 {@link org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage}
 * 等适配器委托调用。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserAttributeFederatedStorage {

    /** 为联邦用户设置单值属性。 */
    void setSingleAttribute(RealmModel realm, String userId, String name, String value);

    /** 为联邦用户设置多值属性。 */
    void setAttribute(RealmModel realm, String userId, String name, List<String> values);

    /** 移除联邦用户的指定属性。 */
    void removeAttribute(RealmModel realm, String userId, String name);

    /**
     * 获取联邦用户的全部属性。
     *
     * @param realm  所属 realm
     * @param userId 联邦用户 ID
     * @return 属性名到值列表的映射
     */
    MultivaluedHashMap<String, String> getAttributes(RealmModel realm, String userId);

    /**
     * 按属性名与值搜索拥有匹配属性的联邦用户。
     *
     * @param realm a reference to the realm.
     * @param name the attribute name.
     * @param value the attribute value.
     * @return a non-null {@link Stream} of user IDs that match the search criteria.
     */
    Stream<String> getUsersByUserAttributeStream(RealmModel realm, String name, String value);

    /**
     * @deprecated 父接口已移除基于集合的方法，可直接使用本接口，无需再继承此 Streams 子接口。
     */
    @Deprecated
    interface Streams extends UserAttributeFederatedStorage {
    }
}
