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

package org.keycloak.keys.infinispan;

import org.keycloak.crypto.PublicKeysWrapper;

/**
 * Infinispan 公钥缓存中的条目快照。
 * <p>
 * 记录最近一次拉取时间与当前持有的公钥集合，供 InfinispanPublicKeyStorageProvider 做 TTL 与刷新判定。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PublicKeysEntry {

    /** 最近一次请求/刷新公钥的 Unix 时间戳（秒）。 */
    private final int lastRequestTime;

    /** 当前缓存的公钥包装对象。 */
    private final PublicKeysWrapper currentKeys;

    /** 构造公钥缓存条目。 */
    public PublicKeysEntry(int lastRequestTime, PublicKeysWrapper currentKeys) {
        this.lastRequestTime = lastRequestTime;
        this.currentKeys = currentKeys;
    }

    /** 返回最近一次公钥拉取时间。 */
    public int getLastRequestTime() {
        return lastRequestTime;
    }

    /** 返回当前缓存的公钥集合。 */
    public PublicKeysWrapper getCurrentKeys() {
        return currentKeys;
    }
}
