/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.singleobject.jpa;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import org.keycloak.connections.jpa.AsynchronousCommitAllowed;

@NamedQueries({
        @NamedQuery(
                name = "insertOrOverwriteSingleUseObject",
                query = "insert into SingleUseObjectEntity (id, notes, expire) values (:id, :notes, :expire)" +
                        " on conflict (id) do update set notes = :notes, expire = :expire"
        ),
        @NamedQuery(
                name = "insertIfAbsentOrExpiredSingleUseObject",
                query = "insert into SingleUseObjectEntity (id, notes, expire) values (:id, :notes, :expire)" +
                        " on conflict (id) do update set notes = :notes, expire = :expire" +
                        " where expire <= :currentTime"
        ),
        @NamedQuery(
                name = "updateIfNotExpiredSingleUseObject",
                query = "update SingleUseObjectEntity set notes = :notes" +
                        " where id = :id and expire > :currentTime"
        ),
        @NamedQuery(
                name = "findExpiredSingleUseObjectIds",
                query = "select e.id from SingleUseObjectEntity e where e.expire <= :currentTime"
        ),
        @NamedQuery(
                name = "deleteExpiredSingleUseObjectByIds",
                query = "delete from SingleUseObjectEntity e where e.id in :ids and e.expire <= :currentTime"
        ),
        @NamedQuery(
                name = "findSingleUseObjectExpireTime",
                query = "select e.expire from SingleUseObjectEntity e where e.id = :id"
        ),
        @NamedQuery(
                name = "findSingleUseObjectNotes",
                query = "select e.notes from SingleUseObjectEntity e where e.id = :id and e.expire > :currentTime"
        ),
})
/**
 * 映射 {@code SINGLE_USE_OBJECT} 表的一次性对象 JPA 实体。
 * <p>
 * 包含命名查询：插入/覆盖、条件插入、过期更新、过期清理及按 ID 查询。
 */
@Entity
@Table(name = "SINGLE_USE_OBJECT")
public class SingleUseObjectEntity implements AsynchronousCommitAllowed {

    @Override
    public boolean isAsyncCommitAllowed(EntityOperationType operationType) {
        // 删除一次性令牌必须同步提交，否则可能产生安全漏洞
        return operationType != EntityOperationType.DELETE;
    }

    /** 对象主键（通常为令牌或 action token ID）。 */
    @Id
    @Column(name = "ID", length = 255)
    private String id;

    /** 附加元数据 JSON 文本（由 {@link SingleUseObjectSerialization} 序列化）。 */
    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    /** 过期时间戳（秒）。 */
    @Column(name = "EXPIRE")
    private long expire;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SingleUseObjectEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("SingleUseObjectEntity [ id=%s, expire=%d ]", id, expire);
    }
}
