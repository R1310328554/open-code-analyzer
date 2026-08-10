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
package org.keycloak.authorization.model;

/**
 * 缓存型授权模型需实现的接口，支持失效与获取可写委托。
 *
 * Cached authorization model classes will implement this interface.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface CachedModel<Model> {
    /**
     * 使缓存失效并返回实际数据提供者的可写委托对象。
     *
     * Invalidates the cache for this model and returns a delegate that represents the actual data provider
     *
     * @return
     */
    Model getDelegateForUpdate();

    /**
     * 使本模型的缓存失效。
     *
     * Invalidate the cache for this model
     *
     */
    void invalidate();

    /**
     * 返回模型从数据库加载时的时间戳。
     *
     * When was the model was loaded from database.
     *
     * @return
     */
    long getCacheTimestamp();
}
