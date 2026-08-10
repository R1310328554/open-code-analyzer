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

package org.keycloak.testsuite.domainextension.spi.impl;

import java.util.LinkedList;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.testsuite.domainextension.CompanyRepresentation;
import org.keycloak.testsuite.domainextension.jpa.Company;
import org.keycloak.testsuite.domainextension.spi.ExampleService;

/**
 * {@link ExampleService} 的默认实现，通过 JPA 对 {@link Company} 实体执行 CRUD 操作。
 */
public class ExampleServiceImpl implements ExampleService {

    /** 当前 Keycloak 会话，用于获取 Realm 与 JPA 连接。 */
    private final KeycloakSession session;

    /**
     * @param session 必须包含 Realm 上下文的 Keycloak 会话
     */
    public ExampleServiceImpl(KeycloakSession session) {
        this.session = session;
        if (getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in it's context.");
        }
    }

    /** 获取当前会话绑定的 JPA {@link EntityManager}。 */
    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    /** 从会话上下文中读取当前 Realm。 */
    protected RealmModel getRealm() {
        return session.getContext().getRealm();
    }
    
    /** {@inheritDoc} 列出当前 Realm 下的全部公司。 */
    @Override
    public List<CompanyRepresentation> listCompanies() {
    	List<Company> companyEntities = getEntityManager().createNamedQuery("findByRealm", Company.class)
                .setParameter("realmId", getRealm().getId())
                .getResultList();

        List<CompanyRepresentation> result = new LinkedList<>();
        for (Company entity : companyEntities) {
            result.add(new CompanyRepresentation(entity));
        }
        return result;
    }
    
    /** {@inheritDoc} 按主键查找单个公司。 */
    @Override
    public CompanyRepresentation findCompany(String id) {
    	Company entity = getEntityManager().find(Company.class, id);
        return entity==null ? null : new CompanyRepresentation(entity);
    }
    
    /** {@inheritDoc} 持久化新公司并返回带生成 ID 的表示对象。 */
    @Override
    public CompanyRepresentation addCompany(CompanyRepresentation company) {
        Company entity = new Company();
        String id = company.getId()==null ?  KeycloakModelUtils.generateId() : company.getId();
        entity.setId(id);
        entity.setName(company.getName());
        entity.setRealmId(getRealm().getId());
        getEntityManager().persist(entity);

        company.setId(id);
        return company;
    }

    /** {@inheritDoc} 删除当前 Realm 下的全部公司记录。 */
    @Override
    public void deleteAllCompanies() {
        EntityManager em = getEntityManager();
        List<Company> companyEntities = em.createNamedQuery("findByRealm", Company.class)
                .setParameter("realmId", getRealm().getId())
                .getResultList();

        for (Company entity : companyEntities) {
            em.remove(entity);
        }
    }

    /** 释放资源；当前实现无需额外清理。 */
    public void close() {
        // 无需执行任何操作。
    }

}
