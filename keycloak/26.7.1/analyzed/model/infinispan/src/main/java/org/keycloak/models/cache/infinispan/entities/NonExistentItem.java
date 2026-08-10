/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

/**
 * 表示缓存中「不存在」条目的占位实体。
 * <p>
 * 实现 {@link Revisioned}，用于负缓存（negative cache）场景：
 * 当某 ID 在持久层不存在时仍写入缓存，避免重复穿透查询。
 *
 * @author hmlnarik
 */
public class NonExistentItem implements Revisioned {

    /** 被标记为不存在的实体 ID。 */
    private final String id;

    /** 占位条目的 revision 版本号。 */
    private long revision;

    /** 以 ID 构造 revision 为 0 的不存在占位条目。 */
    public NonExistentItem(String id) {
        this.id = id;
    }

    /** 以 ID 与指定 revision 构造不存在占位条目。 */
    public NonExistentItem(String id, long revision) {
        this.id = id;
        this.revision = revision;
    }

    /** 返回被标记为不存在的实体 ID。 */
    @Override
    public String getId() {
        return this.id;
    }

    /** 返回占位条目的 revision 版本号。 */
    @Override
    public long getRevision() {
        return this.revision;
    }

    /** 更新占位条目的 revision 版本号。 */
    @Override
    public void setRevision(long revision) {
        this.revision = revision;
    }

}
