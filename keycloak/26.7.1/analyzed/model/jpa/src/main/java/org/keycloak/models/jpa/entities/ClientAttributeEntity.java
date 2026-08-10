/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Nationalized;


/**
 * 客户端自定义属性 JPA 实体，映射 CLIENT_ATTRIBUTES 表。
 * <p>
 * 复合主键 (client, name)；支持同名多行实现多值属性（由适配器层去重/合并）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Table(name="CLIENT_ATTRIBUTES")
@Entity
@IdClass(ClientAttributeEntity.Key.class)
public class ClientAttributeEntity {

    /** 所属客户端（复合主键之一）。 */
    @Id
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "CLIENT_ID")
    protected ClientEntity client;

    /** 属性名（复合主键之一）。 */
    @Id
    @Column(name="NAME")
    protected String name;

    /** 属性值（支持 Unicode）。 */
    @Nationalized
    @Column(name = "VALUE")
    protected String value;

    public ClientEntity getClient() {
        return client;
    }

    public void setClient(ClientEntity client) {
        this.client = client;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    /** 复合主键类：client + name。 */
    public static class Key implements Serializable {

        protected ClientEntity client;

        protected String name;

        public Key() {
        }

        public Key(ClientEntity client, String name) {
            this.client = client;
            this.name = name;
        }

        public ClientEntity getClient() {
            return client;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClientAttributeEntity.Key key = (ClientAttributeEntity.Key) o;

            if (client != null ? !client.getId().equals(key.client != null ? key.client.getId() : null) : key.client != null) return false;
            if (name != null ? !name.equals(key.name != null ? key.name : null) : key.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = client != null ? client.getId().hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!(o instanceof ClientAttributeEntity)) return false;

        ClientAttributeEntity key = (ClientAttributeEntity) o;

        if (client != null ? !client.getId().equals(key.client != null ? key.client.getId() : null) : key.client != null) return false;
        if (name != null ? !name.equals(key.name != null ? key.name : null) : key.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = client != null ? client.getId().hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
