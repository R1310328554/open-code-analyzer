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

package org.keycloak.testsuite.domainextension;


import org.keycloak.testsuite.domainextension.jpa.Company;

/**
 * 公司实体的 REST 传输对象，用于域扩展示例 API 的序列化与反序列化。
 */
public class CompanyRepresentation {

    /** 公司唯一标识。 */
    private String id;
    /** 公司名称。 */
    private String name;

    /** 默认无参构造器。 */
    public CompanyRepresentation() {
    }

    /**
     * 从 JPA 实体构建表示对象。
     *
     * @param company 持久化公司实体
     */
    public CompanyRepresentation(Company company) {
        id = company.getId();
        name = company.getName();
    }
    
    /** @return 公司标识 */
    public String getId() {
		return id;
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
}
