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

package org.keycloak.testsuite.domainextension.jpa;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * 域扩展示例中的公司 JPA 实体，映射至 {@code EXAMPLE_COMPANY} 表。
 */
@Entity
@Table(name = "EXAMPLE_COMPANY")
@NamedQueries({ @NamedQuery(name = "findByRealm", query = "from Company where realmId = :realmId") })
public class Company {

    /** 主键标识。 */
    @Id
    @Column(name = "ID")
    private String id;

    /** 公司名称，不可为空。 */
    @Column(name = "NAME", nullable = false)
    private String name;

    /** 所属 Realm 标识，不可为空。 */
    @Column(name = "REALM_ID", nullable = false)
    private String realmId;

    /** @return 公司标识 */
    public String getId() {
		return id;
	}
    
    /** @return 所属 Realm 标识 */
    public String getRealmId() {
        return realmId;
    }
    
    /** @return 公司名称 */
    public String getName() {
		return name;
	}

    /** @param id 公司标识 */
    public void setId(String id) {
        this.id = id;
    }

    /** @param name 公司名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @param realmId 所属 Realm 标识 */
    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }
}
