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

package org.keycloak.models.jpa.entities;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * 数据库迁移版本 JPA 实体，映射 {@code MIGRATION_MODEL} 表。
 * <p>单行记录当前 schema 版本与更新时间，供 Liquibase 变更日志协调。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Table(name="MIGRATION_MODEL")
@Entity
@NamedQueries({
        @NamedQuery(name = "getLatest", query = "select m from MigrationModelEntity m ORDER BY m.updatedTime DESC")
})
public class MigrationModelEntity {
    /** 单例主键常量，全库仅一条迁移记录。 */
    public static final String SINGLETON_ID = "SINGLETON";
    /** 固定为 {@link #SINGLETON_ID}；PROPERTY 访问避免额外 SQL。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    private String id;

    /** 当前 Liquibase/迁移版本号。 */
    @Column(name="VERSION", length = 36)
    protected String version;

    /** 最后一次迁移更新时间（毫秒）。 */
    @Column(name="UPDATE_TIME")
    protected long updatedTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getUpdateTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof MigrationModelEntity)) return false;

        MigrationModelEntity that = (MigrationModelEntity) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
