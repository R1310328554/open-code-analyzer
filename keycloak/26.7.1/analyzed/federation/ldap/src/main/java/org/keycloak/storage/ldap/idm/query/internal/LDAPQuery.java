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

package org.keycloak.storage.ldap.idm.query.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ModelException;
import org.keycloak.storage.StorageUnavailableException;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPDn;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.Sort;
import org.keycloak.storage.ldap.idm.store.ldap.LDAPContextManager;
import org.keycloak.storage.ldap.mappers.LDAPMappersComparator;
import org.keycloak.storage.ldap.mappers.LDAPStorageMapper;

import org.jboss.logging.Logger;

import static java.util.Collections.unmodifiableSet;

import static org.keycloak.common.util.Throwables.isCausedBy;

/**
 * 默认 {@code IdentityQuery} 实现，封装 LDAP 目录搜索的构建与执行。
 *
 * <p>若启用了分页（调用 {@link #initPagination()}），使用后必须 {@link #close()} 释放
 * {@link LDAPContextManager}；当上下文持有 Vault 密钥时尤其重要。</p>
 *
 * @author Shane Bryzak
 */
public class LDAPQuery implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(LDAPQuery.class);

    private final LDAPStorageProvider ldapFedProvider;

    private int limit;
    private PaginationContext paginationContext;
    private LDAPContextManager ldapContextManager;
    private LdapName searchDn;
    private final Set<Condition> conditions = new LinkedHashSet<>();
    private final Set<Sort> ordering = new LinkedHashSet<>();

    private final Set<String> returningLdapAttributes = new LinkedHashSet<>();

    // 只读返回属性子集；对应 LDAPObject 实例也会标记为只读
    // 属性名统一小写，避免 LDAP 大小写不敏感带来的匹配问题
    private final Set<String> returningReadOnlyLdapAttributes = new LinkedHashSet<>();
    private final Set<String> objectClasses = new LinkedHashSet<>();

    private final List<ComponentModel> mappers = new ArrayList<>();

    private int searchScope = SearchControls.SUBTREE_SCOPE;

    /** @param ldapProvider 所属 LDAP 联邦提供器 */
    public LDAPQuery(LDAPStorageProvider ldapProvider) {
        this.ldapFedProvider = ldapProvider;
    }

    /** 追加 WHERE 条件。 */
    public LDAPQuery addWhereCondition(Condition... condition) {
        this.conditions.addAll(Arrays.asList(condition));
        return this;
    }

    /** 追加排序字段。 */
    public LDAPQuery sortBy(Sort... sorts) {
        this.ordering.addAll(Arrays.asList(sorts));
        return this;
    }

    /** 以字符串形式设置搜索基准 DN。 */
    public LDAPQuery setSearchDn(String searchDn) {
        this.searchDn = LDAPDn.fromString(searchDn).getLdapName();
        return this;
    }

    /** 设置搜索基准 DN。 */
    public LDAPQuery setSearchDn(LdapName searchDn) {
        this.searchDn = searchDn;
        return this;
    }

    /** 追加目标 objectClass 约束。 */
    public LDAPQuery addObjectClasses(Collection<String> objectClasses) {
        this.objectClasses.addAll(objectClasses);
        return this;
    }

    /** 追加需返回的 LDAP 属性名。 */
    public LDAPQuery addReturningLdapAttribute(String ldapAttributeName) {
        this.returningLdapAttributes.add(ldapAttributeName);
        return this;
    }

    /** 追加需返回且标记为只读的 LDAP 属性名。 */
    public LDAPQuery addReturningReadOnlyLdapAttribute(String ldapAttributeName) {
        this.returningReadOnlyLdapAttributes.add(ldapAttributeName.toLowerCase());
        return this;
    }

    /** 追加参与查询前处理的 LDAP 映射器组件。 */
    public LDAPQuery addMappers(Collection<ComponentModel> mappers) {
        this.mappers.addAll(mappers);
        return this;
    }

    /** 设置 JNDI 搜索范围（如 {@link SearchControls#SUBTREE_SCOPE}）。 */
    public LDAPQuery setSearchScope(int searchScope) {
        this.searchScope = searchScope;
        return this;
    }

    /** 返回不可变排序集合。 */
    public Set<Sort> getSorting() {
        return unmodifiableSet(this.ordering);
    }

    /** 返回搜索基准 DN。 */
    public LdapName getSearchDn() {
        return this.searchDn;
    }

    /** 返回不可变 objectClass 集合。 */
    public Set<String> getObjectClasses() {
        return unmodifiableSet(this.objectClasses);
    }

    /** 返回不可变返回属性集合。 */
    public Set<String> getReturningLdapAttributes() {
        return unmodifiableSet(this.returningLdapAttributes);
    }

    /** 返回不可变只读返回属性集合。 */
    public Set<String> getReturningReadOnlyLdapAttributes() {
        return unmodifiableSet(this.returningReadOnlyLdapAttributes);
    }

    /** 返回关联的 LDAP 映射器组件列表。 */
    public List<ComponentModel> getMappers() {
        return mappers;
    }

    /** 返回 JNDI 搜索范围。 */
    public int getSearchScope() {
        return searchScope;
    }

    /** 返回结果数量上限。 */
    public int getLimit() {
        return limit;
    }

    /** 返回分页上下文；未启用分页时为 {@code null}。 */
    public PaginationContext getPaginationContext() {
        return paginationContext;
    }


    /**
     * 执行查询并返回 LDAP 条目列表。
     *
     * <p>执行前按配置顺序调用各映射器的 {@code beforeLDAPQuery} 钩子。</p>
     */
    public List<LDAPObject> getResultList() {

        // 按 LDAP 映射器优先级排序后依次预处理
        LDAPMappersComparator ldapMappersComparator = new LDAPMappersComparator(ldapFedProvider.getLdapIdentityStore().getConfig());
        Collections.sort(mappers, ldapMappersComparator.sortAsc());

        for (ComponentModel mapperModel : mappers) {
            LDAPStorageMapper fedMapper = ldapFedProvider.getMapperManager().getMapper(mapperModel);
            fedMapper.beforeLDAPQuery(this);
        }

        List<LDAPObject> result = new ArrayList<>();

        try {
            for (LDAPObject ldapObject : ldapFedProvider.getLdapIdentityStore().fetchQueryResults(this)) {
                result.add(ldapObject);
            }
        } catch (Exception e) {
            if (isCausedBy(e, NameNotFoundException.class, CommunicationException.class, AuthenticationException.class)) {
                throw new StorageUnavailableException("LDAP server is unavailable for provider [" + ldapFedProvider.getModel().getName() + "]", e);
            }
            throw new ModelException("Failed to fetch results from the LDAP [" + ldapFedProvider.getModel().getName() + "] provider", e);
        }

        return result;
    }

    /**
     * 返回唯一匹配条目；无结果时返回 {@code null}，多条时抛出 {@link ModelDuplicateException}。
     */
    public LDAPObject getFirstResult() {
        List<LDAPObject> results = getResultList();

        if (results.isEmpty()) {
            return null;
        } else if (results.size() == 1) {
            return results.get(0);
        } else {
            throw new ModelDuplicateException("Error - multiple LDAP objects found but expected just one");
        }
    }

    /** 返回匹配条目总数（不拉取完整结果集）。 */
    public int getResultCount() {
        return ldapFedProvider.getLdapIdentityStore().countQueryResults(this);
    }

    /** 设置结果数量上限。 */
    public LDAPQuery setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * 初始化分页上下文并创建持久 LDAP 连接。
     *
     * @throws NamingException 建立 JNDI 上下文失败时
     */
    public LDAPQuery initPagination() throws NamingException {
        this.ldapContextManager = LDAPContextManager.create(ldapFedProvider.getSession(),
                ldapFedProvider.getLdapIdentityStore().getConfig());
        this.paginationContext = new PaginationContext(ldapContextManager.getLdapContext());
        return this;
    }

    /** 返回可变 WHERE 条件集合。 */
    public Set<Condition> getConditions() {
        return this.conditions;
    }

    /** 返回所属 LDAP 联邦提供器。 */
    public LDAPStorageProvider getLdapProvider() {
        return ldapFedProvider;
    }


    /** {@inheritDoc} 释放分页场景下创建的 {@link LDAPContextManager}。 */
    @Override
    public void close() {
        if (ldapContextManager != null) {
            ldapContextManager.close();
        }
    }


    /**
     * LDAP 分页搜索上下文，持有 JNDI 连接与 Simple Paged Results 控制 cookie。
     */
    public static class PaginationContext {

        private final LdapContext ldapContext;
        private byte[] cookie;

        private PaginationContext(LdapContext ldapContext) {
            if (ldapContext == null) {
                throw new IllegalArgumentException("Bad usage. Ldap context must be not null");
            }
            this.ldapContext = ldapContext;
        }


        /** 返回分页搜索使用的 JNDI 上下文。 */
        public LdapContext getLdapContext() {
            return ldapContext;
        }

        /** 返回 Simple Paged Results 控制 cookie。 */
        public byte[] getCookie() {
            return cookie;
        }

        /** 设置 Simple Paged Results 控制 cookie。 */
        public void setCookie(byte[] cookie) {
            this.cookie = cookie;
        }

        /** 是否还有下一页（cookie 非空）。 */
        public boolean hasNextPage() {
            return this.cookie != null;
        }
    }

}
