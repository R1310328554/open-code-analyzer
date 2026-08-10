/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 用户写入缓存时的回调接口，供 Provider 在缓存加载后执行附加初始化。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface OnUserCache {
    /**
     * 用户被放入缓存时调用。
     *
     * @param realm 所属 realm
     * @param user 缓存用户包装
     * @param delegate 底层真实用户模型
     */
    void onCache(RealmModel realm, CachedUserModel user, UserModel delegate);
}
