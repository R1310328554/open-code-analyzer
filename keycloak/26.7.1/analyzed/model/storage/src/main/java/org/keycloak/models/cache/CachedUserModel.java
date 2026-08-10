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

import java.util.concurrent.ConcurrentMap;

import org.keycloak.models.UserModel;

/**
 * 缓存用户模型接口：封装用户数据并支持失效与委托更新。
 * <p>
 * 实现此接口的对象表示已缓存的用户，底层数据由 delegate 提供。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface CachedUserModel extends UserModel {

    /**
     * 使该用户缓存失效，并返回代表真实数据源的委托 {@link UserModel}。
     *
     * @return 可写委托用户模型
     */
    UserModel getDelegateForUpdate();

    /** 是否已标记待驱逐出缓存。 */
    boolean isMarkedForEviction();

    /**
     * 使该用户模型的缓存条目失效。
     */
    void invalidate();

    /**
     * 返回模型从数据库加载时的时间戳。
     *
     * @return 缓存时间戳（毫秒）
     */
    long getCacheTimestamp();

    /**
     * 返回与该用户一并缓存的自定义附加数据映射（可写入）。
     *
     * @return 并发映射，键值由缓存实现定义
     */
    ConcurrentMap getCachedWith();
}
