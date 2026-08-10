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
package org.keycloak.models.cache.infinispan.entities;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 可附带关联缓存条目的 revision 实体抽象基类。
 * <p>
 * 在 {@link AbstractRevisioned} 基础上提供 {@code cachedWith} 映射，
 * 允许将查询结果等附属对象与主缓存条目一并存储。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractExtendableRevisioned extends AbstractRevisioned {
    /** 与主条目一并缓存的附属对象映射。 */
    protected ConcurrentHashMap cachedWith = new ConcurrentHashMap();

    /** 以指定 revision 与 ID 构造可扩展缓存实体。 */
    public AbstractExtendableRevisioned(long revision, String id) {
        super(revision, id);
    }

    /**
     * 返回与主缓存对象关联存储的附属条目映射。
     *
     * @return 附属缓存对象映射
     */
    public ConcurrentHashMap getCachedWith() {
        return cachedWith;
    }
}
