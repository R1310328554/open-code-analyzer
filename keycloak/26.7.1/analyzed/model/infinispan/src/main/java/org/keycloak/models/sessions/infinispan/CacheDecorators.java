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

package org.keycloak.models.sessions.infinispan;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;

/**
 * Infinispan 会话缓存操作的标志位装饰工具。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CacheDecorators {

    /**
     * 为缓存添加 {@link Flag#CACHE_MODE_LOCAL}，使操作仅在本地节点生效、不跨集群复制。
     * @param cache 原始缓存
     * @return 带本地模式标志的高级缓存
     */
    public static <K, V> AdvancedCache<K, V> localCache(Cache<K, V> cache) {
        return cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL);
    }

    /**
     * 为缓存添加 {@link Flag#IGNORE_RETURN_VALUES}，写入时忽略返回值以提升性能。
     * @param cache 原始缓存
     * @return 带忽略返回值标志的高级缓存
     */
    public static <K, V> AdvancedCache<K, V> ignoreReturnValues(Cache<K, V> cache) {
        return cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
    }

}
