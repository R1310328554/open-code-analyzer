/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.storage.configuration.jpa.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * 服务器键值配置 JPA 实体，映射 SERVER_CONFIG 表。
 * <p>
 * 存储 Keycloak 服务器级配置项；{@link #version} 字段用于乐观锁并发控制。
 */
@NamedQueries({
        @NamedQuery(
                name = "findServerConfigKeys",
                query = "SELECT c.key FROM ServerConfigEntity c"
        ),
})
@Table(name = "SERVER_CONFIG")
@Entity
public class ServerConfigEntity {

    /** 配置键（主键）。 */
    @Id
    @Column(name = "SERVER_CONFIG_KEY")
    private String key;

    /** 配置值。 */
    @Column(name = "VALUE")
    private String value;

    /** 乐观锁版本号。 */
    @Version
    @Column(name = "VERSION")
    private int version;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ServerConfigEntity that = (ServerConfigEntity) o;
        return version == that.version && Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(key);
        result = 31 * result + Objects.hashCode(value);
        result = 31 * result + version;
        return result;
    }
}
